package ru.nanit.vasyascheduler.api.util;

import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.logging.log4j.LogManager;

public final class Logger {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(Log4JLogger.class);

    public Logger(){}

    public static void info(String message){
        LOGGER.info(message);
    }

    public static void info(Object message){
        LOGGER.info(message);
    }

    public static void warn(String message){
        LOGGER.warn(message);
    }

    public static void warn(Object message){
        LOGGER.warn(message);
    }

    public static void error(String message){
        LOGGER.error(message);
    }

    public static void error(String message, Throwable throwable){
        LOGGER.error(message, throwable);
    }

    public static void error(Object message){
        LOGGER.error(message);
    }

    public static void fatal(String message){
        LOGGER.fatal(message);
    }

    public static void fatal(Object message){
        LOGGER.fatal(message);
    }
}
