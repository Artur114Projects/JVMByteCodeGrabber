package com.artur114.bytecodegrab.util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ArrayEDITListenBuss<L extends IListener<V>, V> implements IListenBuss<L, V> {
    private final List<L> list = new ArrayList<>();

    @Override
    public void registerListener(L listener) {
        this.list.add(listener);
    }

    @Override
    public void removeListener(L listener) {
        this.list.remove(listener);
    }

    @Override
    public void listen(V value) {
        if (SwingUtilities.isEventDispatchThread()) {
            for (IListener<V> listener : this.list) {
                listener.listen(value);
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                for (IListener<V> listener : this.list) {
                    listener.listen(value);
                }
            });
        }
    }

    @Override
    public List<L> listeners() {
        return this.list;
    }
}
