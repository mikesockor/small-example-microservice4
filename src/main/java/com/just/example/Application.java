package com.just.example;

import com.just.example.repository.Repository;
import com.just.example.service.AccountService;
import com.just.example.service.ServiceExceptionMapper;
import com.just.example.service.TransactionService;
import com.just.example.utils.Utils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {

        logger.info("H2 being used as repository implementation");
        Repository repository = Repository.getRepository(Utils.getStringProperty("repository_type"));
        logger.info("H2 initialization has started");
        repository.initDB();
        logger.info("jetty web server starting up");
        startService();
    }

    private static void startService() throws Exception {

        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitParameter("jersey.config.server.provider.classnames",
            AccountService.class.getCanonicalName() + ","
                + ServiceExceptionMapper.class.getCanonicalName() + ","
                + TransactionService.class.getCanonicalName()
        );

        try {
            server.start();
            server.join();
        }
        finally {
            server.destroy();
        }
    }

}
