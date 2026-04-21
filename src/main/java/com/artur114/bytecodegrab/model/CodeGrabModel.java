package com.artur114.bytecodegrab.model;

import com.artur114.bytecodegrab.util.*;

import java.io.File;
import java.util.List;

public class CodeGrabModel {
    private final IListenBuss<IListener<List<String>>, List<String>> classesRequestDoneBuss = new ArrayListenBuss<>();
    private final IListenBuss<IListener<Percent>, Percent> classesRequestProcessBuss = new ArrayListenBuss<>();

    private final IListenBuss<IListener<AsyncByteCodeWriter.IGrabState>, AsyncByteCodeWriter.IGrabState> bytecodeRequestProcessBuss = new ArrayListenBuss<>();
    private final IListenBuss<IListener<File>, File> bytecodeRequestDoneBuss = new ArrayListenBuss<>();

    private AsyncClassesRequester requester;
    private AsyncByteCodeWriter writer;

    public void requestClassPath() {
        if (this.requester != null && !this.requester.isDone()) {
            return;
        }

        this.requester = new AsyncClassesRequester();
        this.requester.addDoneListener(this.classesRequestDoneBuss::listen);
        this.requester.addProcessListener(value -> this.classesRequestProcessBuss.listen(value.get(value.size() - 1)));
        this.requester.execute();
    }

    public void grabClasses(List<String> classes, IGrabStartData data) {
        if (this.writer != null && !this.writer.isDone()) {
            return;
        }

        this.writer = new AsyncByteCodeWriter(classes, data);
        this.writer.addDoneListener(this.bytecodeRequestDoneBuss::listen);
        this.writer.addProcessListener(value -> this.bytecodeRequestProcessBuss.listen(value.get(value.size() - 1)));
        this.writer.execute();
    }

    public void addCRequestProcessListener(IListener<Percent> listener) {
        this.classesRequestProcessBuss.registerListener(listener);
    }

    public void addCRequestDoneListener(IListener<List<String>> listener) {
        this.classesRequestDoneBuss.registerListener(listener);
    }

    public void addBCRequestProcessListener(IListener<AsyncByteCodeWriter.IGrabState> listener) {
        this.bytecodeRequestProcessBuss.registerListener(listener);
    }

    public void addBCRequestDoneListener(IListener<File> listener) {
        this.bytecodeRequestDoneBuss.registerListener(listener);
    }
}
