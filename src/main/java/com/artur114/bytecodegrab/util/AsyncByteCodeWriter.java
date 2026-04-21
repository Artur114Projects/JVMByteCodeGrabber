package com.artur114.bytecodegrab.util;

import com.artur114.bytecodegrab.agent.GrabberAgent;
import com.artur114.bytecodegrab.net.AgentConnection;
import com.artur114.bytecodegrab.net.ServerSocketThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AsyncByteCodeWriter extends SwingWorkerListened<File, AsyncByteCodeWriter.IGrabState> {
    private static final Logger LOGGER = LogManager.getLogger("Model/AsyncByteCodeWriter");
    private final EnumSaveType saveType;
    private final List<String> classes;
    private final IGrabStartData data;
    private final File outputFile;

    public AsyncByteCodeWriter(List<String> classes, IGrabStartData data) {
        this.outputFile = this.fixFileIfNeeded(data.file());
        this.saveType = this.computeSaveType(data.file());
        this.classes = classes;
        this.data = data;
    }

    @Override
    protected File doInBackground() throws Exception {
        ServerSocketThread server = ServerSocketThread.server();
        GrabState state = new GrabState();
        LOGGER.info("Request bytecode to grab");

        state.state = "Grabbing bytecode (? classes)";
        state.percent.setIndeterminate(true);
        this.publish(state);

        AgentConnection agent = server.agent();
        agent.send("REQUEST_CLASSES_BYTECODE");
        this.sendClassList(agent);

        int classesCount;

        String reply = agent.waitForMessage();

        if (reply.contains("CLASS_COUNT")) {
            classesCount = StringUtils.intPropFromMessage(reply, "CLASS_COUNT");
        } else {
            LOGGER.error("Reply is invalid, expected CLASS_COUNT[?i], got: {}", reply);
            return null;
        }

        state.state = String.format("Grabbing bytecode (%s classes)", classesCount);
        this.publish(state);

        while (true) {
            reply = agent.waitForMessage();
            int process;

            if (reply.contains("PROCESS")) {
                process = StringUtils.intPropFromMessage(reply, "PROCESS");
            } else {
                LOGGER.error("Reply is invalid, expected PROCESS[?i], got: {}", reply);
                return null;
            }

            state.percent.setPercent(process, classesCount);
            this.publish(state);

            if (process == classesCount) {
                break;
            }
        }

        LOGGER.info("Taking bytecode");

        state.state = "taking bytecode";
        this.publish(state);

        Map<String, byte[]> classes = this.takeBytecode(agent, state, classesCount);

        LOGGER.info("Writing output");

        state.state = "writing output";
        this.publish(state);

        switch (this.saveType) {
            case JAR:
                this.saveAsJar(classes, state, classesCount);
                break;
            case ZIP:
                this.saveAsZip(classes, state, classesCount);
                break;
            case DIR:

                break;
        }

        return this.outputFile;
    }

    private Map<String, byte[]> takeBytecode(AgentConnection connection, GrabState state, int classesCount) throws IOException {
        Map<String, byte[]> classes = new HashMap<>(classesCount);
        String reply = connection.waitForMessage();

        if (reply.contains("CLASS_BYTECODE_LIST_START")) {
            classesCount = StringUtils.intPropFromMessage(reply, "SIZE");
        } else {
            LOGGER.error("Reply is invalid, expected CLASS_BYTECODE_LIST_START[?i], got: {}", reply);
            return classes;
        }


        while (true) {
            reply = connection.waitForMessage();
            String name;

            if (reply.equals("CLASS_BYTECODE_LIST_END")) {
                break;
            }

            if (reply.contains("CLASS")) {
                name = StringUtils.stringPropFromMessage(reply, "CLASS");
            } else {
                LOGGER.error("Reply is invalid, expected CLASS[?s], got: {}", reply);
                return classes;
            }

            classes.put(name, connection.receiveBytes());

            state.percent.setPercent(classes.size(), classesCount);
            this.publish(state);
        }
        return classes;
    }

    private void saveAsJar(Map<String, byte[]> classes, GrabState state, int classesCount) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("Grabbed-Bytecode-Classes-Count", String.valueOf(classesCount));

        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(this.outputFile.toPath()), manifest)) {
            List<String> names = new ArrayList<>(classes.keySet());
            for (int i = 0; i != names.size(); i++) {
                String name = names.get(i);
                String classPath = name.replace('.', '/') + ".class";
                jos.putNextEntry(new JarEntry(classPath));
                jos.write(classes.get(name));
                jos.closeEntry();

                state.percent.setPercent(i, classesCount);
                this.publish(state);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void saveAsZip(Map<String, byte[]> classes, GrabState state, int classesCount) {
        try (ZipOutputStream jos = new ZipOutputStream(Files.newOutputStream(this.outputFile.toPath()))) {
            List<String> names = new ArrayList<>(classes.keySet());
            for (int i = 0; i != names.size(); i++) {
                String name = names.get(i);
                String classPath = name.replace('.', '/') + ".class";
                jos.putNextEntry(new ZipEntry(classPath));
                jos.write(classes.get(name));
                jos.closeEntry();

                state.percent.setPercent(i, classesCount);
                this.publish(state);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private File fixFileIfNeeded(File file) {
        if (file.getName().endsWith(".jar") || file.getName().endsWith("zip") || file.isDirectory()) {
            return file;
        }
        return new File(file.getAbsolutePath() + ".jar");
    }

    private EnumSaveType computeSaveType(File file) {
        if (file.getName().endsWith(".jar")) {
            return EnumSaveType.JAR;
        }
        if (file.getName().endsWith("zip")) {
            return EnumSaveType.ZIP;
        }
        if (file.isDirectory()) {
            return EnumSaveType.DIR;
        }
        return EnumSaveType.JAR;
    }

    private void sendClassList(AgentConnection connection) throws IOException {
        connection.send("CLASS_LIST_START SIZE:[" + this.classes.size() + "]");
        for (String clazz : this.classes) {
            connection.send(clazz);
        }
        connection.send("CLASS_LIST_END");
    }

    public interface IGrabState {
        Percent percent();
        String state();
    }

    private static class GrabState implements IGrabState {
        private final Percent percent = new Percent();
        private String state = "";

        @Override
        public Percent percent() {
            return this.percent;
        }

        @Override
        public String state() {
            return this.state;
        }
    }

    private enum EnumSaveType {
        JAR, DIR, ZIP
    }
}
