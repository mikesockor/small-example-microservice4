package com.just.example.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger     logger     = LoggerFactory.getLogger(Utils.class);
    private static       Properties properties = new Properties();

    static {
        String configFileName = System.getProperty("application.properties");

        if (configFileName == null) {
            configFileName = "application.properties";
        }
        loadConfig(configFileName);

    }

    public static void loadConfig(String fileName) {
        if (fileName == null) {
            logger.warn("loadConfig: config file name cannot be null");
        } else {
            try {
                logger.info("loadConfig(): Loading config file: " + fileName);
                final InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
                properties.load(fis);

            }
            catch (FileNotFoundException fne) {
                logger.error("loadConfig(): file name not found " + fileName, fne);
            }
            catch (IOException ioe) {
                logger.error("loadConfig(): error when reading the config " + fileName, ioe);
            }
        }

    }

    public static String getStringProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }

    public static String getStringProperty(String key, String defaultVal) {
        String value = getStringProperty(key);
        return value != null ? value : defaultVal;
    }

    //initialise

    public static int getIntegerProperty(String key, int defaultVal) {
        String valueStr = getStringProperty(key);
        if (valueStr == null) {
            return defaultVal;
        } else {
            try {
                return Integer.parseInt(valueStr);

            }
            catch (Exception e) {
                logger.warn("getIntegerProperty(): cannot parse integer from properties file for: " + key + "fail over to default value: "
                    + defaultVal, e);
                return defaultVal;
            }
        }
    }

}
