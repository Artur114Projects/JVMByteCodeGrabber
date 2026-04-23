package com.artur114.bytecodegrab.model;

import com.artur114.bytecodegrab.agent.*;
import com.artur114.bytecodegrab.net.ServerSocketThread;
import com.sun.tools.attach.VirtualMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class ConnectModel {
    private static final Logger LOGGER = LogManager.getLogger("Model/ConnectModel");
    private final List<Runnable> connectListeners = new ArrayList<>();
    private IStateListener listener;
    private Thread connectionThread;

    public void connect(String pid)  {
        this.connectionThread = new Thread(() -> {
            try {
                this.listener.onStateChanged(new State("starting server").setPercent(0));
                ServerSocketThread server = ServerSocketThread.server();
                server.addDisposableConnectListener((agent) -> {
                    listener.onStateChanged(new State("connected").setPercent(4));

                    for (Runnable run : this.connectListeners) {
                        run.run();
                    }
                });
                this.listener.onStateChanged(new State("building agent").setPercent(1));
                File agent = this.createAgentJar();

                this.listener.onStateChanged(new State("attach to target vm").setPercent(2));
                VirtualMachine vm = VirtualMachine.attach(pid);

                this.listener.onStateChanged(new State("injecting agent").setPercent(3));
                try {
                    vm.loadAgent(agent.getAbsolutePath(), server.address());
                } finally {
                    vm.detach();
                    agent.delete();
                }

            } catch (Exception e) {
                this.listener.onStateChanged(new State("error").setPercent(-1));
                e.printStackTrace(System.err);
            }
        });

        this.connectionThread.start();
    }

    public void disconnect() {
        if (ServerSocketThread.agentOnline()) {
            ServerSocketThread.disconnectAgent();
        }
    }

    public void abort() {
        if (this.connectionThread != null) {
            this.connectionThread.interrupt();
        }

        if (ServerSocketThread.agentOnline()) {
            ServerSocketThread.disconnectAgent();
        }
    }

    public void setStateListener(IStateListener listener) {
        this.listener = listener;
    }

    public void addConnectListener(Runnable listener) {
        this.connectListeners.add(listener);
    }

    private File createAgentJar() throws Exception {
        File jarFile = File.createTempFile("bcg-agent", ".jar");
        jarFile.deleteOnExit();

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("Agent-Class", GrabberAgent.class.getName());
        manifest.getMainAttributes().putValue("Can-Retransform-Classes", "true");

        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarFile.toPath()), manifest)) {
            for (Class<?> clazz : this.agentClasses()) {
                String classPath = clazz.getName().replace('.', '/') + ".class";

                jos.putNextEntry(new JarEntry(classPath));
                jos.write(classBytes(clazz));
                jos.closeEntry();
            }
        }

        LOGGER.info("Created temp agent jar:");
        LOGGER.info("    file: {}", jarFile.getAbsoluteFile());

        return jarFile;
    }

    private byte[] classBytes(Class<?> clazz) throws Exception {
        String classPath = "/" + clazz.getName().replace('.', '/') + ".class";
        try (InputStream is = clazz.getResourceAsStream(classPath)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            if (is == null) {
                System.out.println("input stream is null!");
                return new byte[0];
            }
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    private Class<?>[] agentClasses() {
        return new Class[] {
            AgentLoggerAsync.class,
            AgentLoggerLog4j.class,
            AgentLoggerSout.class,
            AsyncTaskProcessor.class,
            Grabber.class,
            GrabberAgent.class,
            GrabberTransformer.class,
            IAgentLogger.class,
            IBytecodeOutput.class,
            SocketA.class,
        };
    }

    private static class State implements IConnectState {
        private final String stage;
        private int percent;

        public State(String stage) {
            this.stage = stage;
        }

        private State setPercent(int percent) {
            this.percent = percent;
            return this;
        }

        @Override
        public String stage() {
            return this.stage;
        }

        @Override
        public int percent() {
            return this.percent;
        }
    }

    public interface IStateListener {
        void onStateChanged(IConnectState state);
    }

    public interface IConnectState {
        String stage();
        int percent();
    }
}
