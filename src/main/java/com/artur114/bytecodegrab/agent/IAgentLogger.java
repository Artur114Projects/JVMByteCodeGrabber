package com.artur114.bytecodegrab.agent;

public interface IAgentLogger {
    void warn(String log);
    void info(String log);
    void error(String log);
    void warn(String log, Object arg);
    void info(String log, Object arg);
    void error(String log, Object arg);
    void warn(String log, Object... args);
    void info(String log, Object... args);
    void error(String log, Object... args);
    void warn(String log, Object arg, Object arg1);
    void info(String log, Object arg, Object arg1);
    void error(String log, Object arg, Object arg1);

    static IAgentLogger createBestLogger() {
        boolean hasLog4j = false;

        try {
            Class.forName("org.apache.logging.log4j.LogManager"); hasLog4j = true;
        } catch (ClassNotFoundException ignored) {}

        if (hasLog4j) {
            return new AgentLoggerLog4j();
        } else {
            return new AgentLoggerSout();
        }
    }

    static AgentLoggerAsync createBestAsyncLogger() {
        boolean hasLog4j = false;

        try {
            Class.forName("org.apache.logging.log4j.LogManager"); hasLog4j = true;
        } catch (ClassNotFoundException ignored) {}

        if (hasLog4j) {
            return new AgentLoggerAsync(new AgentLoggerLog4j());
        } else {
            return new AgentLoggerAsync(new AgentLoggerSout());
        }
    }
}
