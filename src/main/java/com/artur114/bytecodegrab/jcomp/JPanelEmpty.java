package com.artur114.bytecodegrab.jcomp;

import javax.swing.*;
import java.awt.*;

public class JPanelEmpty extends JPanel {
    public JPanelEmpty() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("NOOP");
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(label.getFont().deriveFont(40f));

        this.add(label);

        this.add(Box.createHorizontalGlue());
    }
}
