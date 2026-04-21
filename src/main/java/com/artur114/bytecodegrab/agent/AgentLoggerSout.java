package com.artur114.bytecodegrab.agent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AgentLoggerSout implements IAgentLogger {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void warn(String log) {
        this.soutString("WARN", log);
    }

    @Override
    public void info(String log) {
        this.soutString("INFO", log);
    }

    @Override
    public void error(String log) {
        this.soutString("ERROR", log);
    }

    @Override
    public void warn(String log, Object arg) {
        String[] splitLog = log.split("\\{}");

        if (splitLog.length != 2) {
            return;
        }

        this.soutString("WARN", splitLog[0] + arg + splitLog[1]);
    }

    @Override
    public void info(String log, Object arg) {
        String[] splitLog = log.split("\\{}");

        if (splitLog.length != 2) {
            return;
        }

        this.soutString("INFO", splitLog[0] + arg + splitLog[1]);
    }

    @Override
    public void error(String log, Object arg) {
        String[] splitLog = log.split("\\{}");

        if (splitLog.length != 2) {
            return;
        }

        this.soutString("ERROR", splitLog[0] + arg + splitLog[1]);
    }

    @Override
    public void warn(String log, Object... args) {
        this.soutString("WARN", String.format(log.replace("{}", "%s"), args));
    }

    @Override
    public void info(String log, Object... args) {
        this.soutString("INFO", String.format(log.replace("{}", "%s"), args));
    }

    @Override
    public void error(String log, Object... args) {
        this.soutString("ERROR", String.format(log.replace("{}", "%s"), args));
    }

    @Override
    public void warn(String log, Object arg, Object arg1) {
        String[] splitLog = log.split("\\{}");

        if (splitLog.length != 3) {
            return;
        }

        this.soutString("WARN", splitLog[0] + arg + splitLog[1] + arg1 + splitLog[2]);
    }

    @Override
    public void info(String log, Object arg, Object arg1) {
        String[] splitLog = log.split("\\{}");

        if (splitLog.length != 3) {
            return;
        }

        this.soutString("INFO", splitLog[0] + arg + splitLog[1] + arg1 + splitLog[2]);
    }

    @Override
    public void error(String log, Object arg, Object arg1) {
        String[] splitLog = log.split("\\{}");

        if (splitLog.length != 3) {
            return;
        }

        this.soutString("ERROR", splitLog[0] + arg + splitLog[1] + arg1 + splitLog[2]);
    }

    private void soutString(String type, String log) {
        System.out.println("[" + LocalTime.now().format(TIME_FORMATTER) + "] [BCG Agent thread/" + type + "] [BYTECODEGARB-AGENT]: " + log);
    }
}
