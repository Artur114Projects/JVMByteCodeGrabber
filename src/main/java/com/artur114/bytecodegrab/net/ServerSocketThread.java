package com.artur114.bytecodegrab.net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerSocketThread extends Thread {
    private static final Logger LOGGER = LogManager.getLogger("Net/ServerSocketThread");
    private static ServerSocketThread server;

    public static ServerSocketThread server() {
        if (server == null) {
            try {
                server = new ServerSocketThread();
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return server;
    }

    public static boolean agentOnline() {
        return server != null && server.agent != null && !server.agent.connection.isClosed();
    }

    public static void disconnectAgent() {
        if (server != null && server.agent != null && !server.agent.connection.isClosed()) {
            try {
                server.agent.disconnect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final List<Consumer<AgentConnection>> disposableConnectionListeners = new ArrayList<>();
    private final List<Consumer<AgentConnection>> connectionListeners = new ArrayList<>();
    private final ServerSocket serverSocket;
    private AgentConnection agent;
    private boolean stop = false;

    private ServerSocketThread() throws IOException {
        this.serverSocket = new ServerSocket(0);

        this.setName("Server socket thread");
    }

    public void addConnectListener(Consumer<AgentConnection> listener) {
        this.connectionListeners.add(listener);
    }

    public void addDisposableConnectListener(Consumer<AgentConnection> listener) {
        this.disposableConnectionListeners.add(listener);
    }

    @Override
    public void run() {
        try {

            LOGGER.info("Server started, address: {}", this.address());

            while (!this.stop) {
                this.agent = new AgentConnection(this.serverSocket.accept());
                LOGGER.info("Agent connected!");
                LOGGER.info("    Connection: {}", this.agent);

                for (Consumer<AgentConnection> listener : this.connectionListeners) {
                    listener.accept(this.agent);
                }
                for (Consumer<AgentConnection> listener : this.disposableConnectionListeners) {
                    listener.accept(this.agent);
                }
                this.disposableConnectionListeners.clear();
            }
        } catch (IOException e) {
            if (!(e instanceof SocketException) && !e.getMessage().equals("Socket closed")) {
                e.printStackTrace(System.err);
            }
        } finally {
            this.close();
        }
    }

    public String address() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress() + ":" + serverSocket.getLocalPort();
    }

    public AgentConnection agent() {
        return this.agent;
    }

    public void close() {
        try {
            this.stop = true;
            this.serverSocket.close();
            if (this.agent != null) {
                this.agent.disconnect();
            }
            System.out.println("server is closed!");
        } catch (IOException ignored) {}
    }
}
