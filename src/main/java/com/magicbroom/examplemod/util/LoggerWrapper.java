package com.magicbroom.examplemod.util;

import com.magicbroom.examplemod.core.Config;
import org.slf4j.Logger;

/**
 * 日志包装器类
 * 根据配置文件控制不同级别日志的输出
 * INFO级别日志始终输出，其他级别根据配置控制
 */
public class LoggerWrapper {
    private final Logger logger;

    public LoggerWrapper(Logger logger) {
        this.logger = logger;
    }

    /**
     * INFO级别日志 - 始终输出，不受配置控制
     */
    public void info(String message) {
        logger.info(message);
    }

    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    /**
     * DEBUG级别日志 - 受配置控制
     */
    public void debug(String message) {
        if (Config.ENABLE_DEBUG_LOGS.get()) {
            logger.debug(message);
        }
    }

    public void debug(String format, Object... arguments) {
        if (Config.ENABLE_DEBUG_LOGS.get()) {
            logger.debug(format, arguments);
        }
    }

    /**
     * WARN级别日志 - 始终输出
     */
    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    /**
     * ERROR级别日志 - 始终输出
     */
    public void error(String message) {
        logger.error(message);
    }

    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * 获取原始Logger实例（用于特殊情况）
     */
    public Logger getOriginalLogger() {
        return logger;
    }
}