package com.artur114.bytecodegrab.jcomp;

import com.artur114.bytecodegrab.util.EnumAxis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class JButtonsPane extends JPanel {
    private final List<JButton> buttons = new ArrayList<>();
    public JButtonsPane(EnumAxis axis) {
        this.setLayout(new BoxLayout(this, axis.toBoxLayout()));
    }

    public void addGlobalActionListener(ActionListener listener) {
        for (JButton button : this.buttons) {
            button.addActionListener(listener);
        }
    }

    public void addSplitter(int size) {
        this.add(Box.createHorizontalStrut(size));
    }

    public void add(JButton button) {
        if (this.buttons.contains(button)) {
            return;
        }
        this.buttons.add(button);
        this.add((Component) button);
    }

    public JButton createButton(Icon icon) {
        JButton button = new JButton(icon);
        this.buttons.add(button);
        this.add(button);
        return button;
    }

    public JButton createButton(String text) {
        JButton button = new JButton(text);
        this.buttons.add(button);
        this.add((Component) button);
        return button;
    }

    public JButton createButton(String text, Icon icon) {
        JButton button = new JButton(text, icon);
        this.buttons.add(button);
        this.add((Component) button);
        return button;
    }

    public JButton createButton(Icon icon, Consumer<JButton> conf) {
        JButton button = new JButton(icon);
        this.buttons.add(button);
        this.add((Component) button);
        conf.accept(button);
        return button;
    }

    public JButton createButton(String text, Consumer<JButton> conf) {
        JButton button = new JButton(text);
        this.buttons.add(button);
        this.add((Component) button);
        conf.accept(button);
        return button;
    }

    public JButton createButton(String text, Icon icon, Consumer<JButton> conf) {
        JButton button = new JButton(text, icon);
        this.buttons.add(button);
        this.add((Component) button);
        conf.accept(button);
        return button;
    }


    public void configure(Consumer<JButton> conf) {
        for (JButton button : this.buttons) {
            conf.accept(button);
        }
    }

    public void sort(Comparator<? super JButton> comparator) {
        this.sort(comparator, false);
    }

    public void sort(Comparator<? super JButton> comparator, boolean recompile) {
        this.buttons.sort(comparator);
        if (recompile) this.recompile();
    }

    public void recompile() {
        for (Component component : this.getComponents()) {
            this.remove(component);
        }

        for (JButton button : this.buttons) {
            this.add(button);
        }

        this.revalidate();
        this.repaint();
    }
}
