package com.artur114.bytecodegrab.main;

import com.artur114.bytecodegrab.util.Icons;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class Application extends JFrame {
    private static Application application = null;
    private JSplitPane baseSplitPane;

    public Application() {
        this.setSize(700, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setTitle("Byte Code Grabber");
        this.setIconImage(Icons.image("icon.png"));

        FlatLightLaf.setup();

        this.initView();
    }

    private void initView() {
        this.baseSplitPane = new JSplitPane();
        this.add(this.baseSplitPane);
    }

    public JSplitPane baseSplitPane() {
        return this.baseSplitPane;
    }

    public static Application application() {
        return application;
    }

    protected static void init() {
        application = new Application();
    }
}
