package com.artur114.bytecodegrab.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AgentLoggerLog4j implements IAgentLogger {
    private final Logger logger = LogManager.getLogger("BYTECODEGARB-AGENT");

    @Override
    public void warn(String log) {
        this.logger.warn(log);
    }

    @Override
    public void info(String log) {
        this.logger.info(log);
    }

    @Override
    public void error(String log) {
        this.logger.error(log);
    }

    @Override
    public void warn(String log, Object arg) {
        this.logger.warn(log, arg);
    }

    @Override
    public void info(String log, Object arg) {
        this.logger.info(log, arg);
    }

    @Override
    public void error(String log, Object arg) {
        this.logger.error(log, arg);
    }

    @Override
    public void warn(String log, Object... args) {
        this.logger.warn(log, args);
    }

    @Override
    public void info(String log, Object... args) {
        this.logger.info(log, args);
    }

    @Override
    public void error(String log, Object... args) {
        this.logger.error(log, args);
    }

    @Override
    public void warn(String log, Object arg, Object arg1) {
        this.logger.warn(log, arg, arg1);
    }

    @Override
    public void info(String log, Object arg, Object arg1) {
        this.logger.info(log, arg, arg1);
    }

    @Override
    public void error(String log, Object arg, Object arg1) {
        this.logger.error(log, arg, arg1);
    }
}
