package com.artur114.bytecodegrab.util;

import java.util.List;

public interface IListenBuss<L extends IListener<V>, V> {
    void registerListener(L listener);
    void removeListener(L listener);
    void listen(V value);
    List<L> listeners();
}
