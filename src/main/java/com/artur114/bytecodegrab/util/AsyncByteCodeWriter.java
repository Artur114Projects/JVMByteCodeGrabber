package com.artur114.bytecodegrab.util;

import com.artur114.bytecodegrab.agent.GrabberAgent;
import com.artur114.bytecodegrab.net.AgentConnection;
import com.artur114.bytecodegrab.net.NetReplyException;
import com.artur114.bytecodegrab.net.ServerSocketThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        AgentConnection agent = server.agent();
        GrabState state = new GrabState();

        if (this.isCancelled()) {
            return null;
        }

        LOGGER.info("Request bytecode to grab");
        state.state = "Grabbing bytecode (? classes)";
        state.percent.setIndeterminate(true);
        this.publish(state);
        int classesCount = this.requestBytecode(agent);

        if (this.isCancelled()) {
            return null;
        }

        state.state = String.format("Grabbing bytecode (%s classes)", classesCount);
        this.publish(state);
        this.waitForProcess(agent, state, classesCount);

        if (this.isCancelled()) {
            return null;
        }

        LOGGER.info("Taking bytecode");
        state.state = "Taking bytecode";
        this.publish(state);
        Map<String, byte[]> classes = this.takeBytecode(agent, state, classesCount);

        if (this.isCancelled()) {
            return null;
        }

        LOGGER.info("Writing output");
        state.state = "Writing output";
        this.publish(state);
        this.writeOutput(this.saveType, classes, state, classesCount);

        if (this.isCancelled()) {
            return null;
        }

        return this.outputFile;
    }

    private void waitForProcess(AgentConnection connection, GrabState state, int classesCount) throws IOException {
        long startTime = System.currentTimeMillis();
        while (true) {
            String reply = connection.waitForMessage();
            int process;

            if (reply.contains("PROCESS")) {
                process = StringUtils.intPropFromMessage(reply,  "PROCESS");
            } else {
                throw new NetReplyException("PROCESS[?i]", reply);
            }

            if (this.isCancelled()) {
                connection.send("ABORT"); return;
            } else {
                connection.send("NEXT");
            }

            state.timeLeft = this.computeRetransformLeftTime(startTime, process, classesCount);
            state.percent.setPercent(process, classesCount);
            this.publish(state);

            if (process == classesCount) {
                break;
            }
        }
    }

    private int requestBytecode(AgentConnection connection) throws IOException {
        connection.send("REQUEST_CLASSES_BYTECODE");
        this.sendClassList(connection);
        String reply = connection.waitForMessage();

        if (reply.contains("CLASS_COUNT")) {
            return StringUtils.intPropFromMessage(reply, "CLASS_COUNT");
        } else {
            throw new NetReplyException("CLASS_COUNT[?i]", reply);
        }
    }

    private Map<String, byte[]> takeBytecode(AgentConnection connection, GrabState state, int classesCount) throws IOException {
        Map<String, byte[]> classes = new HashMap<>(classesCount);
        String reply = connection.waitForMessage();

        if (reply.contains("CLASS_BYTECODE_LIST_START")) {
            classesCount = StringUtils.intPropFromMessage(reply, "SIZE");
        } else {
            throw new NetReplyException("CLASS_BYTECODE_LIST_START[?i]", reply);
        }

        long startTime = System.currentTimeMillis();
        while (true) {
            reply = connection.waitForMessage();
            String name;

            if (reply.equals("CLASS_BYTECODE_LIST_END")) {
                break;
            }

            if (reply.contains("CLASS")) {
                name = StringUtils.stringPropFromMessage(reply, "CLASS");
            } else {
                throw new NetReplyException("CLASS[?s]", reply);
            }

            if (this.isCancelled()) {
                connection.send("ABORT"); return null;
            } else {
                connection.send("NEXT");
            }

            classes.put(name, connection.receiveBytes());

            state.timeLeft = this.computeLeftTime(startTime, classes.size(), classesCount);
            state.percent.setPercent(classes.size(), classesCount);
            this.publish(state);
        }

        return classes;
    }

    private void writeOutput(EnumSaveType saveType, Map<String, byte[]> classes, GrabState state, int classesCount) {
        File file = null;
        switch (saveType) {
            case JAR:
                file = this.saveAsJar(this.prepareNameSpace(classes, this.data.type()), state, classesCount);
                break;
            case ZIP:
                file = this.saveAsZip(this.prepareNameSpace(classes, this.data.type()), state, classesCount);
                break;
            case DIR:
                file = this.saveAsDir(this.prepareNameSpace(classes, this.data.type()), state, classesCount);
                break;
        }

        if (file != null) {
            try {
                if (this.outputFile.isDirectory()) {
                    if (this.outputFile.delete() || this.outputFile.mkdir()) {
                        Files.move(file.toPath(), this.outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    Files.move(file.toPath(), this.outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, byte[]> prepareNameSpace(Map<String, byte[]> classes, IGrabStartData.WriteType type) {
        Map<String, byte[]> ret = new HashMap<>();

        classes.forEach((k, v) -> {
            switch (type) {
                case FULL:
                    ret.put(k + ".class", v);
                    break;
                case P_CLASS:
                    ret.put(k.replace("/", ".") + ".class", v);
                    break;
                case J_CLASS:
                    String name = k.substring(k.lastIndexOf("/") + 1) + ".class";

                    if (ret.containsKey(name)) {
                        ret.put(k.replace("/", ".") + ".class", v);
                    } else {
                        ret.put(name, v);
                    }

                    break;
            }
        });

        return ret;
    }

    private File saveAsDir(Map<String, byte[]> classes, GrabState state, int classesCount) {
        File file;
        try {
            file = File.createTempFile("bcg-temp-write", "");
        } catch (IOException e) {
            e.printStackTrace(System.err); return null;
        }

        long startTime = System.currentTimeMillis();
        List<String> names = new ArrayList<>(classes.keySet());
        for (int i = 0; i != names.size(); i++) {
            if (this.isCancelled()) {
                return null;
            }
            String name = names.get(i);
            File entry = new File(this.outputFile.getAbsolutePath() + "/" + name.substring(0, name.lastIndexOf("/")));
            if (entry.mkdirs()) {
                try (FileOutputStream fos = new FileOutputStream(this.outputFile.getAbsolutePath() + "/" + name)) {
                    fos.write(classes.get(name));
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            state.timeLeft = this.computeLeftTime(startTime, i, classesCount);
            state.percent.setPercent(i, classesCount);
            this.publish(state);
        }

        return file;
    }

    private File saveAsJar(Map<String, byte[]> classes, GrabState state, int classesCount) {
        File file;
        try {
            file = File.createTempFile("bcg-temp-write", "jar");
        } catch (IOException e) {
            e.printStackTrace(System.err); return null;
        }
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("Grabbed-Bytecode-Classes-Count", String.valueOf(classesCount));

        long startTime = System.currentTimeMillis();
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(file.toPath()), manifest)) {
            List<String> names = new ArrayList<>(classes.keySet());
            for (int i = 0; i != names.size(); i++) {
                if (this.isCancelled()) {
                    Files.deleteIfExists(file.toPath());
                    return null;
                }

                String name = names.get(i);
                jos.putNextEntry(new JarEntry(name));
                jos.write(classes.get(name));
                jos.closeEntry();

                state.timeLeft = this.computeLeftTime(startTime, i, classesCount);
                state.percent.setPercent(i, classesCount);
                this.publish(state);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return file;
    }

    private File saveAsZip(Map<String, byte[]> classes, GrabState state, int classesCount) {
        File file;
        try {
            file = File.createTempFile("bcg-temp-write", "zip");
        } catch (IOException e) {
            e.printStackTrace(System.err); return null;
        }
        long startTime = System.currentTimeMillis();
        try (ZipOutputStream jos = new ZipOutputStream(Files.newOutputStream(file.toPath()))) {
            List<String> names = new ArrayList<>(classes.keySet());
            for (int i = 0; i != names.size(); i++) {
                if (this.isCancelled()) {
                    Files.deleteIfExists(file.toPath());
                    return null;
                }

                String name = names.get(i);
                jos.putNextEntry(new ZipEntry(name));
                jos.write(classes.get(name));
                jos.closeEntry();

                state.timeLeft = this.computeLeftTime(startTime, i, classesCount);
                state.percent.setPercent(i, classesCount);
                this.publish(state);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return file;
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

    private long computeRetransformLeftTime(long startTime, int currentProgress, int maxProgress) {
        long timePassed = System.currentTimeMillis() - startTime;
        if (timePassed < 400) {
            return -1;
        }
        double timePerProcess = ((double) timePassed / currentProgress) * (1.8D - ((double) currentProgress / maxProgress));
        int processRemainder = maxProgress - currentProgress;
        return (long) (timePerProcess * processRemainder);
    }


    private long computeLeftTime(long startTime, int currentProgress, int maxProgress) {
        long timePassed = System.currentTimeMillis() - startTime;
        double timePerProcess = ((double) timePassed / currentProgress);
        int processRemainder = maxProgress - currentProgress;
        return (long) (timePerProcess * processRemainder);
    }

    public interface IGrabState {
        Percent percent();
        long timeLeft();
        String state();
    }

    private static class GrabState implements IGrabState {
        private final Percent percent = new Percent();
        private long timeLeft = -1;
        private String state = "";

        @Override
        public Percent percent() {
            return this.percent;
        }

        @Override
        public long timeLeft() {
            return this.timeLeft;
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
