package com.artur114.bytecodegrab.jcomp;

import com.artur114.bytecodegrab.main.Application;
import com.artur114.bytecodegrab.util.Icons;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class JLoadingFrame extends JFrame {
    public JLoadingFrame() {
        this.setSize(300, 200);
        this.setLocationRelativeTo(null);
        this.setTitle("Byte Code Grabber");
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIconImage(Icons.image("icon_loading"));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("LOADING");
        label.setForeground(Color.GRAY);
        label.setFont(label.getFont().deriveFont(40f));

        panel.add(label);
        panel.add(Box.createHorizontalGlue());

        this.add(panel);
    }
}
