package com.artur114.bytecodegrab.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AgentLoggerLog4j implements IAgentLogger {
    private final Logger logger = LogManager.getLogger("BYTECODEGARB-AGENT");
    private final AgentLoggerSout sout = new AgentLoggerSout();
    private boolean down = false;

    @Override
    public void warn(String log) {
        if (this.down) {
            this.sout.warn(log);
            return;
        }
        try {
            this.logger.warn(log);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void info(String log) {
        if (this.down) {
            this.sout.info(log);
            return;
        }
        try {
            this.logger.info(log);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void error(String log) {
        if (this.down) {
            this.sout.error(log);
            return;
        }
        try {
            this.logger.error(log);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void warn(String log, Object arg) {
        if (this.down) {
            this.sout.warn(log, arg);
            return;
        }
        try {
            this.logger.warn(log, arg);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void info(String log, Object arg) {
        if (this.down) {
            this.sout.info(log, arg);
            return;
        }
        try {
            this.logger.info(log, arg);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void error(String log, Object arg) {
        if (this.down) {
            this.sout.error(log, arg);
            return;
        }
        try {
            this.logger.error(log, arg);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void warn(String log, Object... args) {
        if (this.down) {
            this.sout.warn(log, args);
            return;
        }
        try {
            this.logger.warn(log, args);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void info(String log, Object... args) {
        if (this.down) {
            this.sout.info(log, args);
            return;
        }
        try {
            this.logger.info(log, args);
        } catch (Throwable e) {
            down = true;
        }
    }

    @Override
    public void error(String log, Object... args) {
        if (this.down) {
            this.sout.error(log, args);
            return;
        }
        try {
            this.logger.error(log, args);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void warn(String log, Object arg, Object arg1) {
        if (this.down) {
            this.sout.warn(log, arg, arg1);
            return;
        }
        try {
            this.logger.warn(log, arg, arg1);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void info(String log, Object arg, Object arg1) {
        if (this.down) {
            this.sout.info(log, arg, arg1);
            return;
        }
        try {
            this.logger.info(log, arg, arg1);
        } catch (Throwable e) {
            this.down = true;
        }
    }

    @Override
    public void error(String log, Object arg, Object arg1) {
        if (this.down) {
            this.sout.error(log, arg, arg1);
            return;
        }
        try {
            this.logger.error(log, arg, arg1);
        } catch (Throwable e) {
            this.down = true;
        }
    }
}