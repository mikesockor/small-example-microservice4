package com.just.example.repository.h2Impl;

import com.just.example.exception.ServiceException;
import com.just.example.model.Account;
import com.just.example.model.Transaction;
import com.just.example.model.TransactionPurpose;
import com.just.example.repository.AccountRepository;
import com.just.example.repository.Repository;
import com.just.example.repository.TransactionRepository;
import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionRepositoryH2ImplTest {

    private static final Logger logger = LoggerFactory.getLogger(TransactionRepositoryH2ImplTest.class);

    TransactionRepository transactionRepository;
    AccountRepository accountRepository;

    @Before
    public void setUp() throws Exception {
        Repository repository = Repository.getRepository(Repository.H2);
        repository.initDB();
        transactionRepository = repository.getTransactionRepository();
        accountRepository = repository.getAccountRepository();
    }

    @Test
    public void getAllAccountTransactions() throws ServiceException {

        assertEquals(0, transactionRepository.getAllAccountTransactions(3).size());
        int operation1 = accountRepository.updateAccountBalance(3, new BigDecimal(234.456));
        int operation2 = accountRepository.updateAccountBalance(3, new BigDecimal(234.456).negate());
        assertEquals(2, transactionRepository.getAllAccountTransactions(3).size());

    }

    @Test
    public void transferAccountBalance() throws InterruptedException, ServiceException {

        int THREADS_COUNT = 100;

        final CountDownLatch latch = new CountDownLatch(THREADS_COUNT);
        for (int i = 0; i < THREADS_COUNT; i++) {
            new Thread(() -> {
                try {
                    Transaction transaction = new Transaction(
                            0L,
                            Timestamp.from(Instant.now()),
                            new BigDecimal(2).setScale(4, RoundingMode.HALF_EVEN),
                            5L,
                            6L,
                            TransactionPurpose.TRANSFER.name()
                    );
                    transactionRepository.transferAccountBalance(transaction);
                } catch (Exception e) {
                    logger.error("Error occurred during transfer ", e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        Account accountFrom = accountRepository.getAccountById(5);
        logger.debug("Account From: " + accountFrom);
        Account accountTo = accountRepository.getAccountById(6);
        logger.debug("Account From: " + accountTo);

        assertEquals(accountFrom.getBalance(), new BigDecimal(0).setScale(4, RoundingMode.HALF_EVEN));
        assertEquals(accountTo.getBalance(), new BigDecimal(300).setScale(4, RoundingMode.HALF_EVEN));

    }

    @Test
    public void transferAccountBalanceNotSufficientFund() throws ServiceException {

        Transaction transaction = new Transaction(
                0L,
                Timestamp.from(Instant.now()),
                new BigDecimal(701).setScale(4, RoundingMode.HALF_EVEN),
                7L,
                1L,
                TransactionPurpose.TRANSFER.name()
        );
        try {
            transactionRepository.transferAccountBalance(transaction);
        } catch (ServiceException e) {
            logger.debug(e.getMessage());
        }

        Account accountFrom = accountRepository.getAccountById(7);
        logger.debug("Account From: " + accountFrom);
        assertEquals(accountFrom.getBalance(), new BigDecimal(700).setScale(4, RoundingMode.HALF_EVEN));

    }

    @Test
    public void testTransferFailOnDBLock() throws ServiceException {
        final String SQL_LOCK_ACC = "SELECT * FROM Account WHERE AccountId = 9 FOR UPDATE";
        Connection conn = null;
        PreparedStatement lockStmt = null;
        ResultSet rs = null;
        Account fromAccount = null;

        try {
            conn = H2Repository.getConnection();
            conn.setAutoCommit(false);
            // lock account for writing:
            lockStmt = conn.prepareStatement(SQL_LOCK_ACC);
            rs = lockStmt.executeQuery();
            if (rs.next()) {
                fromAccount = new Account(
                        rs.getLong("AccountId"),
                        rs.getBigDecimal("Balance")
                );
                logger.atDebug()
                        .addArgument(fromAccount)
                        .log("Locked Account: {}");
            }

            if (fromAccount == null) {
                throw new ServiceException("Locking error during test, SQL = " + SQL_LOCK_ACC);
            }
            // after lock account 8, try to transfer from account 2 to 8
            // default h2 timeout for acquire lock is 1sec

            Transaction transaction = new Transaction(
                    0L,
                    Timestamp.from(Instant.now()),
                    new BigDecimal(900).setScale(4, RoundingMode.HALF_EVEN),
                    8L,
                    9L,
                    TransactionPurpose.TRANSFER.name()
            );
            transactionRepository.transferAccountBalance(transaction);

            conn.commit();
        } catch (Exception e) {
            logger.error("Exception occurred, initiate a rollback");
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException re) {
                logger.error("Fail to rollback transaction", re);
            }
        } finally {
            DbUtils.closeQuietly(conn);
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(lockStmt);
        }

        // now inspect account 3 and 4 to verify no transaction occurred
        BigDecimal originalBalance = new BigDecimal(900).setScale(4, RoundingMode.HALF_EVEN);
        assertEquals(accountRepository.getAccountById(8).getBalance(), originalBalance);
        assertEquals(accountRepository.getAccountById(9).getBalance(), originalBalance);
    }

}