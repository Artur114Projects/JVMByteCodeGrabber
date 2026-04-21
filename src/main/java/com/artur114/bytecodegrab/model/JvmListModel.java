package com.artur114.bytecodegrab.model;

import com.artur114.bytecodegrab.view.JvmListPanel;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;
import java.util.function.Consumer;

public class JvmListModel {
    public void scanJvmsAsync(Consumer<List<VirtualMachineDescriptor>> callBack) {
        Thread thread = new Thread(() -> callBack.accept(VirtualMachine.list()));

        thread.start();
    }
}
