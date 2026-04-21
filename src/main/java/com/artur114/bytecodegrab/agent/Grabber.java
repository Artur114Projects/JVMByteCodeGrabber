package com.artur114.bytecodegrab.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Proxy;
import java.util.*;

public class Grabber extends Thread {
    private final IAgentLogger LOGGER = IAgentLogger.createBestLogger();
    private final GrabberTransformer transformer;
    private final Instrumentation inst;
    private final String host;
    private final int port;

    public Grabber(Instrumentation inst, String host, int port) {
        this.setName("BCG Agent thread");
        LOGGER.info("Agent injected!");

        this.transformer = new GrabberTransformer();
        this.inst = inst;
        this.host = host;
        this.port = port;

        inst.addTransformer(this.transformer);
        inst.addTransformer(this.transformer, true);
    }

    @Override
    public void run() {
        try (SocketA socket = new SocketA(this.host, this.port)) {
            LOGGER.info("Agent connected to: " + this.host + ":" + this.port);
            while (!socket.isClosed()) {
                String message = socket.waitForMessage();

                if (message == null) {
                    break;
                }

                switch (message) {
                    case "REQUEST_LOADED_CLASSES":
                        this.sendLoadedClases(socket);
                        break;
                    case "REQUEST_CLASSES_BYTECODE":
                        this.sendClassesByteCode(socket);
                        break;
                    default:
                        LOGGER.warn("Wrong message: {}", message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            this.disconnect();
        }
    }

    private void sendClassesByteCode(SocketA socket) throws IOException {
        String message = socket.waitForMessage();

        List<String> rawClassList;
        int classesCount;

        if (message.contains("CLASS_LIST_START")) {
            classesCount = this.intPropFromMessage(message, "SIZE");
            rawClassList = new ArrayList<>(classesCount);
        } else {
            LOGGER.warn("Can't dump classes, message is invalid! message: {}", message);
            return;
        }

        this.takeClassList(socket, rawClassList);
        List<Class<?>> classList = this.convertToClasses(rawClassList);
        Map<String, byte[]> grabbedClasses = new HashMap<>();

        this.transformer.setTargetClasses(rawClassList);
        this.transformer.setBytecodeOutput((grabbedClasses::put));

        socket.send("CLASS_COUNT:[" + classList.size() + "]");

        for (int i = 0; i != classList.size(); i++) {
            Class<?> clazz = classList.get(i);
            try {
                socket.send("PROCESS:[" + i + "]");
                this.inst.retransformClasses(clazz);
            } catch (UnmodifiableClassException e) {
                LOGGER.error("Attempt to retransform unmodifiable class, class: {}", clazz);
            } catch (Throwable t) {
                LOGGER.error("An error occurs while retransform class: {}, skipping...", clazz);
            }
        }
        socket.send("PROCESS:[" + classList.size() + "]");

        this.sendByteCode(socket, grabbedClasses);
    }

    private void sendLoadedClases(SocketA socket) throws IOException {
        LOGGER.info("Agent start class finding");
        Class<?>[] rawClasses = this.inst.getAllLoadedClasses();
        LOGGER.info("Agent found {} classes", rawClasses.length);
        socket.send("CLASS_LIST_START SIZE:[" + rawClasses.length + "]");
        LOGGER.info("Agent start sending classes");
        for (Class<?> clazz : rawClasses) {
            if (this.isUsefulClassSafe(clazz)) {
                socket.send(this.fixClassName(clazz, clazz.getName()));
            }
        }
        socket.send("CLASS_LIST_END");
        LOGGER.info("Agent end sending classes");
    }

    private void disconnect() {
        this.transformer.setTargetClasses(null);
        LOGGER.info("Agent disconnected");
    }

    private int intPropFromMessage(String message, String property) {
        int index = message.indexOf(property + ":");

        if (index == -1) {
            throw new IllegalArgumentException();
        }

        int start = message.indexOf('[', index);
        int end = message.indexOf(']', index);

        if (start == -1 || end == -1 || end - 1 <= start) {
            throw new IllegalArgumentException();
        }

        return Integer.parseInt(message.substring(start + 1, end).replace(" ", ""));
    }

    private void sendByteCode(SocketA out, Map<String, byte[]> classes) throws IOException {
        out.send("CLASS_BYTECODE_LIST_START SIZE:[" + classes.size() + "]");
        classes.forEach((key, value) -> {
            try {
                out.send("CLASS:[" + key + "]");
                out.send(value);
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        });
        out.send("CLASS_BYTECODE_LIST_END");
    }

    private void takeClassList(SocketA in, List<String> classList) throws IOException {
        while (true) {
            String message = in.waitForMessage();

            if (message.equals("CLASS_LIST_END")) {
                return;
            }

            classList.add(message);
        }
    }

    private List<Class<?>> convertToClasses(List<String> list) {
        List<Class<?>> ret = new ArrayList<>(list.size());
        Set<String> classes = new HashSet<>(list);
        for (Class<?> clazz : this.inst.getAllLoadedClasses()) {
            if (this.isUsefulClassSafe(clazz) && classes.contains(clazz.getName())) {
                if (this.inst.isModifiableClass(clazz)) {
                    ret.add(clazz);
                } else {
                    LOGGER.warn("Class {} is unmodifiable, skipping...", clazz.getName());
                }
            }
        }
        return ret;
    }

    private String fixClassName(Class<?> clazz, String className) {
        if (!this.inst.isModifiableClass(clazz)) {
            className = "!" + className;
        }
        return className;
    }

    private boolean isUsefulClassSafe(Class<?> clazz) {
        try {
            return this.isUsefulClass(clazz);
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean isUsefulClass(Class<?> clazz) {
        if (clazz.isPrimitive() || clazz.isArray()) {
            return false;
        }

        if (clazz.isLocalClass()) {
            return false;
        }

        if (clazz.isSynthetic()) {
            if (clazz.getName().contains("$$Lambda$")) {
                return false;
            }
        }

        String className = clazz.getName();
        if (Proxy.isProxyClass(clazz) || className.contains("$Proxy") || className.contains("$$EnhancerByCGLIB$$") || className.contains("$$FastClassByCGLIB$$")) {
            return false;
        }

        if (className.contains("/")) {
            return false;
        }

        return true;
    }
}
