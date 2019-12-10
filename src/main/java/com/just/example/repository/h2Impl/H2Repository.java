package com.just.example.repository.h2Impl;

import com.just.example.repository.AccountRepository;
import com.just.example.repository.Repository;
import com.just.example.repository.TransactionRepository;
import com.just.example.utils.Utils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbutils.DbUtils;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2Repository implements Repository {

    private static final String h2_driver         = Utils.getStringProperty("h2_driver");
    private static final String h2_connection_url = Utils.getStringProperty("h2_connection_url");
    private static final String h2_user           = Utils.getStringProperty("h2_user");
    private static final String h2_password       = Utils.getStringProperty("h2_password");

    private static final Logger logger = LoggerFactory.getLogger(H2Repository.class);

    private final AccountRepository     accountRepositoryH2     = new AccountRepositoryH2Impl();
    private final TransactionRepository transactionRepositoryH2 = new TransactionRepositoryH2Impl();

    public H2Repository() {
        DbUtils.loadDriver(h2_driver);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(h2_connection_url, h2_user, h2_password);
    }

    public AccountRepository getAccountRepository() {
        return accountRepositoryH2;
    }

    @Override public TransactionRepository getTransactionRepository() {
        return transactionRepositoryH2;
    }

    @Override
    public void initDB() {

        logger.info("initializing db tables ");
        Connection conn = null;
        try {
            conn = H2Repository.getConnection();
            final InputStream initialStream = getClass().getResourceAsStream("/init.sql");
            RunScript.execute(conn, new InputStreamReader(initialStream));
        }
        catch (SQLException e) {
            logger.error("initDB(): Error has occurred during initializing: ", e);
            throw new RuntimeException(e);
        }
        finally {
            DbUtils.closeQuietly(conn);
        }
    }

}
