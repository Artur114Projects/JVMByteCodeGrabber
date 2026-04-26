package com.artur114.bytecodegrab.main;

import com.artur114.bytecodegrab.conf.AppConfig;
import com.artur114.bytecodegrab.util.Icons;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Application extends JFrame {
    private static Application application = null;
    private JSplitPane baseSplitPane;
    public final AppConfig appData;

    public Application(AppConfig config) {
        this.setSize(700, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setTitle("Byte Code Grabber");
        this.setIconImages(Arrays.asList(Icons.image("icon"), Icons.image("icon_big")));

        this.initView();

        this.appData = config;
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

    protected static void init(AppConfig config) {
        application = new Application(config);
    }
}
