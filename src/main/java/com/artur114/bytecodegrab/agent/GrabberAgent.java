package com.artur114.bytecodegrab.agent;

import javax.swing.*;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class GrabberAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        try {
            String[] server = agentArgs.split(":");
            Grabber grabber = new Grabber(inst, server[0], Integer.parseInt(server[1]));
            grabber.start();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            throw new RuntimeException(t);
        }
    }
}