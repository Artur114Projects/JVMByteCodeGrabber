package com.artur114.bytecodegrab.jcomp;

import javax.swing.*;
import java.util.concurrent.Callable;

public class JDynLabel extends JLabel {
    private Callable<String> label;

    public JDynLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
    }

    public JDynLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
    }

    public JDynLabel(String text) {
        super(text);
    }

    public JDynLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
    }

    public JDynLabel(Icon image) {
        super(image);
    }

    public JDynLabel(Callable<String> label) {
        this.label = label;

        try {
            this.setText(this.label.call());
        } catch (Exception ignored) {}
    }

    public JDynLabel() {}

    public void setDynText(Callable<String> label) {
        this.label = label;
    }

    public void update() {
        try {
            this.setText(this.label.call());
        } catch (Exception ignored) {}
    }

    @Override
    public void revalidate() {
        try {
            this.setText(this.label.call());
        } catch (Exception ignored) {}

        super.revalidate();
    }
}
