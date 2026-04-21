package com.artur114.bytecodegrab.jcomp;

import com.artur114.bytecodegrab.main.Application;
import com.artur114.bytecodegrab.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class JGrabFrame extends JFrame {
    private final IListenBuss<IListener<IGrabStartData>, IGrabStartData> grabListenBuss = new ArrayListenBuss<>();
    private final IListenBuss<IListener<Void>, Void> grabAbortListenBuss = new ArrayListenBuss<>();
    private final IListenBuss<IListener<Void>, Void> frameCloseListenBuss = new ArrayListenBuss<>();
    private final JCardContainer card;
    private boolean isDone = false;
    private JProgressBar progress;
    private JButton buttonAbort;
    private JLabel stateLabel;
    private final File output;

    public JGrabFrame(Application parent, File output) {
        this.output = output;
        this.setSize(340, 180);
        this.setLocationRelativeTo(parent);
        this.setTitle("Grabber");
        this.setIconImage(Icons.image("icon_black.png"));
        this.setDefaultCloseOperation(JGrabFrame.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);

        JGrabFrame t = this;

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!t.isDone && t.card.isShowed("process")) {
                    int result = JOptionPane.showConfirmDialog(t, "Close grab frame and abort process?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                if (!t.isDone) {
                    t.grabAbortListenBuss.listen(null);
                }
                t.frameCloseListenBuss.listen(null);
                t.dispose();
            }
        });

        JPanel panel = new JPanel(new CardLayout());
        this.card = new JCardContainer(panel);

        panel.add(this.createSelectPanel(), "select");
        panel.add(this.createProcessPanel(), "process");

        this.add(panel);

        this.addGrabListener(value -> this.card.show("process"));
        this.addAbortListener(value -> {
            this.frameCloseListenBuss.listen(null);
            this.dispose();
        });
    }

    public void addFrameCloseListener(IListener<Void> listener) {
        this.frameCloseListenBuss.registerListener(listener);
    }

    public void addGrabListener(IListener<IGrabStartData> listener) {
        this.grabListenBuss.registerListener(listener);
    }

    public void addAbortListener(IListener<Void> listener) {
        this.grabAbortListenBuss.registerListener(listener);
    }

    public void setState(String state) {
        this.stateLabel.setText(state);
        this.repaint();
    }

    public void setProgress(Percent percent) {
        if (percent.isIndeterminate()) {
            this.progress.setIndeterminate(true);
        } else {
            this.progress.setIndeterminate(false);
            this.progress.setValue(percent.x100k());
        }
    }

    public void onDone() {
        this.setProgress(new Percent().setPercent(100.0F));
        this.stateLabel.setText("Done!");
        this.buttonAbort.setText("ok");
        this.isDone = true;
    }

    private JPanel createSelectPanel() {
        JPanel panelBase = new JPanel();
        panelBase.setLayout(new BoxLayout(panelBase, BoxLayout.Y_AXIS));

        panelBase.add(Box.createVerticalGlue());

        JPanel panelLabel = new JPanel();
        panelLabel.setLayout(new BoxLayout(panelLabel, BoxLayout.X_AXIS));
        panelLabel.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("Select write format");
        label.setFont(label.getFont().deriveFont(11.0F));
        panelLabel.add(label);
        panelLabel.add(Box.createHorizontalGlue());
        panelBase.add(panelLabel);

        panelBase.add(Box.createVerticalStrut(6));

        JPanel panelComboBox = new JPanel();
        panelComboBox.setLayout(new BoxLayout(panelComboBox, BoxLayout.X_AXIS));
        panelComboBox.add(Box.createHorizontalGlue());
        String[] options = {"Full package", "Package + Class name", "Just class name"};
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setFocusable(false);
        comboBox.setMaximumSize(new Dimension(200, 20));
        comboBox.setPreferredSize(new Dimension(200, 20));
        panelComboBox.add(comboBox);
        panelComboBox.add(Box.createHorizontalGlue());
        panelBase.add(panelComboBox);

        panelBase.add(Box.createVerticalStrut(6));

        JPanel panelButtonGrab = new JPanel();
        panelButtonGrab.setLayout(new BoxLayout(panelButtonGrab, BoxLayout.X_AXIS));
        panelButtonGrab.add(Box.createHorizontalGlue());
        JButton buttonGrab = new JButton("grab");
        buttonGrab.addActionListener(e -> {
            this.grabListenBuss.listen(new GrabData(IGrabStartData.WriteType.values()[comboBox.getSelectedIndex()], this.output));
        });
        panelButtonGrab.add(buttonGrab);
        panelButtonGrab.add(Box.createHorizontalGlue());
        panelBase.add(panelButtonGrab);

        panelBase.add(Box.createVerticalStrut(8));

        panelBase.add(Box.createVerticalGlue());

        return panelBase;
    }

    private JPanel createProcessPanel() {
        JPanel panelBase = new JPanel();
        panelBase.setLayout(new BoxLayout(panelBase, BoxLayout.Y_AXIS));

        panelBase.add(Box.createVerticalGlue());

        JPanel panelLabel = new JPanel();
        panelLabel.setLayout(new BoxLayout(panelLabel, BoxLayout.X_AXIS));
        panelLabel.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("Wait for state");
        label.setFont(label.getFont().deriveFont(11.0F));
        panelLabel.add(label);
        panelLabel.add(Box.createHorizontalGlue());
        panelBase.add(panelLabel);

        panelBase.add(Box.createVerticalStrut(6));

        JPanel panelBar = new JPanel();
        panelBar.setLayout(new BoxLayout(panelBar, BoxLayout.X_AXIS));
        panelBar.add(Box.createHorizontalGlue());
        JProgressBar bar = new JProgressBar(0, 100000);
        bar.setMaximumSize(new Dimension(280, 16));
        bar.setPreferredSize(new Dimension(280, 16));
        panelBar.add(bar);
        panelBar.add(Box.createHorizontalGlue());
        panelBase.add(panelBar);

        panelBase.add(Box.createVerticalStrut(6));

        JPanel panelButtonAbort = new JPanel();
        panelButtonAbort.setLayout(new BoxLayout(panelButtonAbort, BoxLayout.X_AXIS));
        panelButtonAbort.add(Box.createHorizontalGlue());
        JButton buttonAbort = new JButton("abort");
        buttonAbort.addActionListener(e -> {
            if (this.isDone) {
                this.dispose();
            } else {
                this.grabAbortListenBuss.listen(null);
            }
        });
        buttonAbort.setFocusable(false);
        panelButtonAbort.add(buttonAbort);
        panelButtonAbort.add(Box.createHorizontalGlue());
        panelBase.add(panelButtonAbort);

        panelBase.add(Box.createVerticalStrut(8));

        panelBase.add(Box.createVerticalGlue());

        this.buttonAbort = buttonAbort;
        this.stateLabel = label;
        this.progress = bar;

        return panelBase;
    }

    private static class GrabData implements IGrabStartData {
        private final WriteType type;
        private final File file;

        private GrabData(WriteType type, File file) {
            this.type = type;
            this.file = file;
        }

        @Override
        public WriteType type() {
            return this.type;
        }

        @Override
        public File file() {
            return this.file;
        }
    }
}
