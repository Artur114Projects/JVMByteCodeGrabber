package com.artur114.bytecodegrab.frame;

import com.artur114.bytecodegrab.util.Icons;
import com.artur114.bytecodegrab.util.Theme;

import javax.swing.*;
import java.awt.*;

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
        Theme.loadingFrameColor().ifPresent(panel::setBackground);

        panel.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("LOADING");
        label.setForeground(this.getBackground().darker());
        label.setFont(label.getFont().deriveFont(40f));

        panel.add(label);
        panel.add(Box.createHorizontalGlue());

        this.add(panel);
    }
}
