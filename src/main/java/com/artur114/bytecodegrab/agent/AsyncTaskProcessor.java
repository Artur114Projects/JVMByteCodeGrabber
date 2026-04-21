package com.artur114.bytecodegrab.agent;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class AsyncTaskProcessor extends Thread {
    private final BlockingDeque<Runnable> tasks = new LinkedBlockingDeque<>();
    private boolean stop = false;

    public void addTask(Runnable task) {
        this.tasks.offer(task);
    }

    public void shutdown() {
        this.stop = true;
        this.interrupt();
    }

    @Override
    public void run() {
        while (!this.stop) {
            try {
                this.tasks.take().run();
            } catch (InterruptedException e) {
                this.interrupt();
                break;
            }
        }
    }
}
