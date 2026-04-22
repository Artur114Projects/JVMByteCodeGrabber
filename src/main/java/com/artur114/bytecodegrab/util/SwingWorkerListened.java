package com.artur114.bytecodegrab.util;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class SwingWorkerListened<T, V> extends SwingWorker<T, V> {
    private final IListenBuss<IListener<List<V>>, List<V>> processBuss = new ArrayEDITListenBuss<>();
    private final IListenBuss<IListener<T>, T> doneBuss = new ArrayEDITListenBuss<>();
    private boolean disableListen = false;


    public void addDoneListener(IListener<T> listener) {
        this.doneBuss.registerListener(listener);
    }

    public void addProcessListener(IListener<List<V>> listener) {
        this.processBuss.registerListener(listener);
    }

    public void disableListen() {
        this.disableListen = true;
    }

    @Override
    protected void process(List<V> chunks) {
        if (!this.disableListen) {
            this.processBuss.listen(chunks);
        }
    }

    @Override
    protected void done() {
        try {
            if (this.isCancelled() || this.disableListen) {
                return;
            }

            T ret = this.get();

            this.doneBuss.listen(ret);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
