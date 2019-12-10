package com.just.example.repository.h2Impl;

import com.just.example.exception.ServiceException;
import com.just.example.model.Account;
import com.just.example.model.Transaction;
import com.just.example.model.TransactionPurpose;
import com.just.example.repository.TransactionRepository;
import com.just.example.repository.Repository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionRepositoryH2Impl implements TransactionRepository {

    private static final Logger logger = LoggerFactory.getLogger(TransactionRepositoryH2Impl.class);

    private final static String SQL_LOCK_ACC_BY_ID     = "SELECT * FROM Account WHERE AccountId = ? FOR UPDATE";
    private final static String SQL_UPDATE_ACC_BALANCE = "UPDATE Account SET Balance = ? WHERE AccountId = ? ";
    private final static String SQL_ALL_TRX_BY_ACC_ID  = "SELECT * FROM AccountTransaction WHERE FromAccountId = ? UNION SELECT * FROM AccountTransaction WHERE ToAccountId = ?";
    private final static String SQL_CREATE_TRX         = "INSERT INTO AccountTransaction (Amount,FromAccountId,ToAccountId,Purpose) VALUES (?,?,?,?)";

    @Override public List<Transaction> getAllAccountTransactions(final long accountId) throws ServiceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final List<Transaction> transactions = new ArrayList<>();
        try {
            conn = H2Repository.getConnection();
            stmt = conn.prepareStatement(SQL_ALL_TRX_BY_ACC_ID);
            stmt.setLong(1, accountId);
            stmt.setLong(2, accountId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Transaction acc = new Transaction(
                    rs.getLong("TransactionId"),
                    rs.getTimestamp("TrxTimeStamp"),
                    rs.getBigDecimal("Amount"),
                    rs.getLong("FromAccountId"),
                    rs.getLong("ToAccountId"),
                    rs.getString("Purpose")
                );
                logger.atDebug().log("getAllAccountTransactions(): Get AllAccountTransactions ");
                transactions.add(acc);
            }
            return transactions;
        }
        catch (SQLException e) {
            throw new ServiceException("getAllAccountTransactions(): Error reading account transaction data", e);
        }
        finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }

    }

    public int transferAccountBalance(final Transaction transaction) throws ServiceException {

        int result = -1;
        Connection conn = null;
        PreparedStatement lockStmt = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertTrx = null;
        ResultSet rs = null;
        Account fromAccount = null;
        Account toAccount = null;

        try {

            conn = H2Repository.getConnection();
            conn.setAutoCommit(false);

            // lock the credit and debit account for writing:
            lockStmt = conn.prepareStatement(SQL_LOCK_ACC_BY_ID);
            lockStmt.setLong(1, transaction.getFromAccountId());
            rs = lockStmt.executeQuery();
            if (rs.next()) {
                fromAccount = new Account(
                    rs.getLong("AccountId"),
                    rs.getBigDecimal("Balance")
                );
                logger.atDebug().log("transferAccountBalance from Account: " + fromAccount);
            }
            lockStmt = conn.prepareStatement(SQL_LOCK_ACC_BY_ID);
            lockStmt.setLong(1, transaction.getToAccountId());
            rs = lockStmt.executeQuery();
            if (rs.next()) {
                toAccount = new Account(
                    rs.getLong("AccountId"),
                    rs.getBigDecimal("Balance")
                );
                logger.atDebug().log("transferAccountBalance to Account: " + toAccount);
            }

            // check locking status
            if (fromAccount == null || toAccount == null) {
                throw new ServiceException("Fail to lock both accounts for write");
            }

            // check enough fund in source account
            final BigDecimal fromAccountLeftOver = fromAccount.getBalance().subtract(transaction.getAmount());
            if (fromAccountLeftOver.compareTo(Repository.zeroAmount) < 0) {
                throw new ServiceException("Not enough Fund from source Account ");
            }

            // proceed with update
            updateStmt = conn.prepareStatement(SQL_UPDATE_ACC_BALANCE);
            updateStmt.setBigDecimal(1, fromAccountLeftOver);
            updateStmt.setLong(2, transaction.getFromAccountId());
            updateStmt.addBatch();
            updateStmt.setBigDecimal(1, toAccount.getBalance().add(transaction.getAmount()));
            updateStmt.setLong(2, transaction.getToAccountId());
            updateStmt.addBatch();
            int[] rowsUpdated = updateStmt.executeBatch();
            result = rowsUpdated[0] + rowsUpdated[1];
            logger.atDebug()
                .addArgument(result)
                .log("Number of rows updated for the transfer : {}");

            // process with transaction record
            insertTrx = conn.prepareStatement(SQL_CREATE_TRX);
            insertTrx.setBigDecimal(1, transaction.getAmount());
            insertTrx.setLong(2, transaction.getFromAccountId());
            insertTrx.setLong(3, transaction.getToAccountId());
            insertTrx.setString(4, TransactionPurpose.TRANSFER.name());
            insertTrx.executeUpdate();
            result++;

            // If there is no error, commit the transaction
            conn.commit();
        }
        catch (SQLException se) {
            // rollback transaction if exception occurs
            logger
                .error(String.format("transferAccountBalance(): User Transaction Failed, rollback initiated for: %s", transaction), se);
            try {
                if (conn != null)
                    conn.rollback();
            }
            catch (SQLException re) {
                throw new ServiceException("Fail to rollback transaction", re);
            }
        }
        finally {
            DbUtils.closeQuietly(conn);
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(lockStmt);
            DbUtils.closeQuietly(updateStmt);
            DbUtils.closeQuietly(insertTrx);
        }
        return result;
    }

}
