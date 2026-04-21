package com.artur114.bytecodegrab.agent;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

public class AbortListener extends Thread {
    private final DataInputStream connection;
    private final IAgentLogger logger;
    private boolean aborted = false;

    public AbortListener(IAgentLogger logger, SocketA connection) {
        try {
            this.connection = new DataInputStream(connection.getInputStream());
            this.logger = logger;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAborted() {
        return this.aborted;
    }

    public void shutdown() {
        this.interrupt();
        try {
            this.connection.close();
        } catch (IOException ignored) {}
    }

    @Override
    public void run() {
        try {
            String message = this.connection.readUTF();

            if ("ABORT".equals(message)) {
                this.aborted = true;
            } else {
                this.logger.warn("AbortListener listen not abort message {}!", message);
            }
        } catch (EOFException e) {
            this.aborted = true;
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
