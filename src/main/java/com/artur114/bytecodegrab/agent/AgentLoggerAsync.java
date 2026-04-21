package com.artur114.bytecodegrab.agent;

public class AgentLoggerAsync implements IAgentLogger {
    private final AsyncTaskProcessor processor;
    private final IAgentLogger output;

    public AgentLoggerAsync(IAgentLogger output) {
        this.output = output;

        this.processor = new AsyncTaskProcessor();
        this.processor.setName("BCG Logging Thread");
        this.processor.start();
    }

    public void shutdown() {
        this.processor.shutdown();
    }

    @Override
    public void warn(String log) {
        this.processor.addTask(() -> this.output.warn(log));
    }

    @Override
    public void info(String log) {
        this.processor.addTask(() -> this.output.info(log));
    }

    @Override
    public void error(String log) {
        this.processor.addTask(() -> this.output.error(log));
    }

    @Override
    public void warn(String log, Object arg) {
        this.processor.addTask(() -> this.output.warn(log, arg));
    }

    @Override
    public void info(String log, Object arg) {
        this.processor.addTask(() -> this.output.info(log, arg));
    }

    @Override
    public void error(String log, Object arg) {
        this.processor.addTask(() -> this.output.error(log, arg));
    }

    @Override
    public void warn(String log, Object... args) {
        this.processor.addTask(() -> this.output.warn(log, args));
    }

    @Override
    public void info(String log, Object... args) {
        this.processor.addTask(() -> this.output.info(log, args));
    }

    @Override
    public void error(String log, Object... args) {
        this.processor.addTask(() -> this.output.error(log, args));
    }

    @Override
    public void warn(String log, Object arg, Object arg1) {
        this.processor.addTask(() -> this.output.warn(log, arg, arg1));
    }

    @Override
    public void info(String log, Object arg, Object arg1) {
        this.processor.addTask(() -> this.output.info(log, arg, arg1));
    }

    @Override
    public void error(String log, Object arg, Object arg1) {
        this.processor.addTask(() -> this.output.error(log, arg, arg1));
    }
}
