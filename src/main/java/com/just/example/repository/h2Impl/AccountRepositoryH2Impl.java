package com.just.example.repository.h2Impl;

import com.just.example.exception.ServiceException;
import com.just.example.model.Account;
import com.just.example.model.TransactionPurpose;
import com.just.example.repository.AccountRepository;
import com.just.example.repository.Repository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountRepositoryH2Impl implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(AccountRepositoryH2Impl.class);

    private final static String SQL_GET_ACC_BY_ID      = "SELECT * FROM Account WHERE AccountId = ? ";
    private final static String SQL_LOCK_ACC_BY_ID     = "SELECT * FROM Account WHERE AccountId = ? FOR UPDATE";
    private final static String SQL_CREATE_ACC         = "INSERT INTO Account (Balance) VALUES (?)";
    private final static String SQL_UPDATE_ACC_BALANCE = "UPDATE Account SET Balance = ? WHERE AccountId = ? ";

    private final static String SQL_CREATE_TRX_FROM = "INSERT INTO AccountTransaction (Amount,FromAccountId,Purpose) VALUES (?,?,?)";
    private final static String SQL_CREATE_TRX_TO   = "INSERT INTO AccountTransaction (Amount,ToAccountId,Purpose) VALUES (?,?,?)";

    public Account getAccountById(final long accountId) throws ServiceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Account acc = null;
        try {
            conn = H2Repository.getConnection();
            stmt = conn.prepareStatement(SQL_GET_ACC_BY_ID);
            stmt.setLong(1, accountId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                acc = new Account(
                    rs.getLong("AccountId"),
                    rs.getBigDecimal("Balance")
                );
                logger.atDebug()
                    .addArgument(acc)
                    .log("Retrieve Account By Id: {}");
            }
            return acc;
        }
        catch (SQLException e) {
            throw new ServiceException("getAccountById(): Error reading account data", e);
        }
        finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }

    }

    public long createAccount(final Account account) throws ServiceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = H2Repository.getConnection();
            stmt = conn.prepareStatement(SQL_CREATE_ACC, Statement.RETURN_GENERATED_KEYS);
            stmt.setBigDecimal(1, account.getBalance());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.error("createAccount(): Creating account failed, no rows affected.");
                throw new ServiceException("Account Cannot be created");
            }
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                logger.error("Creating account failed, no ID obtained.");
                throw new ServiceException("Account Cannot be created");
            }
        }
        catch (SQLException e) {
            logger.error("Error Inserting Account  " + account);
            throw new ServiceException(String.format("createAccount(): Error creating user account %s", account), e);
        }
        finally {
            DbUtils.closeQuietly(conn, stmt, generatedKeys);
        }
    }

    public int updateAccountBalance(final long accountId, final BigDecimal amount) throws ServiceException {
        Connection conn = null;
        PreparedStatement lockStmt = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertTrx = null;
        ResultSet rs = null;
        Account targetAccount = null;
        int updateCount = -1;
        try {

            conn = H2Repository.getConnection();
            conn.setAutoCommit(false);

            // lock account for writing:
            lockStmt = conn.prepareStatement(SQL_LOCK_ACC_BY_ID);
            lockStmt.setLong(1, accountId);
            rs = lockStmt.executeQuery();
            if (rs.next()) {
                targetAccount = new Account(
                    rs.getLong("AccountId"),
                    rs.getBigDecimal("Balance")
                );
                logger.atDebug().log("updateAccountBalance from Account: " + targetAccount);
            }

            if (targetAccount == null) {
                throw new ServiceException("updateAccountBalance(): fail to lock account : " + accountId);
            }

            // update account upon success locking
            final BigDecimal balance = targetAccount.getBalance().add(amount);
            if (balance.compareTo(Repository.zeroAmount) < 0) {
                throw new ServiceException("Not sufficient Fund for account: " + accountId);
            }

            updateStmt = conn.prepareStatement(SQL_UPDATE_ACC_BALANCE);
            updateStmt.setBigDecimal(1, balance);
            updateStmt.setLong(2, accountId);
            updateCount = updateStmt.executeUpdate();

            // process with transaction record
            if (amount.compareTo(Repository.zeroAmount) > 0) {
                insertTrx = conn.prepareStatement(SQL_CREATE_TRX_TO);
                insertTrx.setBigDecimal(1, amount);
                insertTrx.setLong(2, accountId);
                insertTrx.setString(3, TransactionPurpose.DEPOSIT.name());
            } else {
                insertTrx = conn.prepareStatement(SQL_CREATE_TRX_FROM);
                insertTrx.setBigDecimal(1, amount.negate());
                insertTrx.setLong(2, accountId);
                insertTrx.setString(3, TransactionPurpose.WITHDRAWAL.name());
            }
            insertTrx.executeUpdate();

            conn.commit();
            logger.atDebug()
                .addArgument(targetAccount)
                .log("New Balance after Update: {}");
            return updateCount;
        }
        catch (SQLException se) {
            // rollback transaction if exception occurs
            logger.error("updateAccountBalance(): User Transaction Failed, rollback initiated for: " + accountId, se);
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
        return updateCount;
    }

}
