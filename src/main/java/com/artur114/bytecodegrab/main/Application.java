package com.artur114.bytecodegrab.main;

import com.artur114.bytecodegrab.util.Icons;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.ui.FlatTitlePane;

import javax.swing.*;
import java.awt.*;

public class Application extends JFrame {
    private static Application application = null;
    private JSplitPane baseSplitPane;

    public Application() {
        this.setSize(700, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setTitle("Byte Code Grabber");
        this.setIconImage(Icons.image("icon_black.png"));

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
