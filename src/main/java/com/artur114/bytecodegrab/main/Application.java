package com.artur114.bytecodegrab.main;

import com.artur114.bytecodegrab.conf.AppConfig;
import com.artur114.bytecodegrab.util.Icons;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.ui.FlatTitlePane;

import javax.swing.*;
import java.util.Arrays;

public class Application extends JFrame {
    private static Application application = null;
    private JSplitPane baseSplitPane;
    public final AppConfig appData;

    public Application() {
        this.setSize(700, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setTitle("Byte Code Grabber");
        this.setIconImages(Arrays.asList(Icons.image("icon"), Icons.image("grab")));

        this.initView();

        this.appData = AppConfig.load();
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
