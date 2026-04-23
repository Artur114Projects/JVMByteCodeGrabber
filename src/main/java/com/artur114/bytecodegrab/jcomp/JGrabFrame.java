package com.artur114.bytecodegrab.jcomp;

import com.artur114.bytecodegrab.main.Application;
import com.artur114.bytecodegrab.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;

public class JGrabFrame extends JFrame {
    private static final Logger LOGGER = LogManager.getLogger("View/JGrabFrame");
    private final IListenBuss<IListener<IGrabStartData>, IGrabStartData> grabListenBuss = new ArrayEDITListenBuss<>();
    private final IListenBuss<IListener<Void>, Void> grabAbortListenBuss = new ArrayEDITListenBuss<>();
    private final IListenBuss<IListener<Void>, Void> frameCloseListenBuss = new ArrayEDITListenBuss<>();
    private final JCardContainer card;
    private boolean isDone = false;
    private JProgressBar progress;
    private JLabel labelTimePassed;
    private JLabel labelTimeLeft;
    private JLabel stateLabel;
    private final File output;
    private long startTime = -1;

    public JGrabFrame(Application parent, File output) {
        this.output = output;
        this.setSize(340, 150);
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
        panel.add(this.createDonePanel(), "done");

        this.add(panel);

        this.addGrabListener(value -> {
            this.startTime = System.currentTimeMillis();
            this.card.show("process");
        });
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

    public void setLeftTime(long time) {
        if (time == -1) {
            this.labelTimeLeft.setText("Time left: calculating...");
        } else {
            this.labelTimeLeft.setText("Time left: ~" + this.formatMillis(time));
        }

        this.labelTimePassed.setText("Time passed: " + this.formatMillis(System.currentTimeMillis() - this.startTime));
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
            this.progress.setValue(percent.x100kI());
        }
    }

    public void onDone() {
        this.card.show("done");
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
        panelBase.add(Box.createVerticalStrut(12));

        JPanel panelLabel = new JPanel();
        panelLabel.setLayout(new BoxLayout(panelLabel, BoxLayout.X_AXIS));
        panelLabel.add(Box.createHorizontalStrut(24));
        JLabel label = new JLabel("Wait for state");
        label.setForeground(new Color(0, 0, 0));
        label.setFont(label.getFont().deriveFont(12.0F));
        panelLabel.add(label);
        panelLabel.add(Box.createHorizontalGlue());
        panelBase.add(panelLabel);

        panelBase.add(Box.createVerticalStrut(4));

        JPanel panelBar = new JPanel();
        panelBar.setLayout(new BoxLayout(panelBar, BoxLayout.X_AXIS));
        panelBar.add(Box.createHorizontalStrut(24));
        JProgressBar bar = new JProgressBar(0, 100000);
        bar.setMaximumSize(new Dimension(350, 16));
        bar.setPreferredSize(new Dimension(280, 16));
        panelBar.add(bar);
        panelBar.add(Box.createHorizontalStrut(16));
        panelBase.add(panelBar);

        panelBase.add(Box.createVerticalStrut(4));

        JPanel panelTime = new JPanel();
        panelTime.setLayout(new BoxLayout(panelTime, BoxLayout.X_AXIS));
        panelTime.add(Box.createHorizontalStrut(24));

        JLabel labelTimePassed = new JLabel("Time passed: " + this.formatMillis(0));
        labelTimePassed.setForeground(new Color(20, 20, 20));

        JLabel labelTime = new JLabel("Time left: calculating");
        labelTime.setForeground(new Color(20, 20, 20));

        labelTimePassed.setFont(label.getFont().deriveFont(11.0F));
        labelTime.setFont(label.getFont().deriveFont(11.0F));
        panelTime.add(labelTimePassed);
        panelTime.add(Box.createHorizontalGlue());
        panelTime.add(labelTime);
        panelTime.add(Box.createHorizontalStrut(16));
        panelBase.add(panelTime);


        panelBase.add(Box.createVerticalStrut(12));

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
        panelButtonAbort.add(Box.createHorizontalStrut(16));
        panelBase.add(panelButtonAbort);

        panelBase.add(Box.createVerticalStrut(16));

        panelBase.add(Box.createVerticalGlue());

        this.labelTimePassed = labelTimePassed;
        this.labelTimeLeft = labelTime;
        this.stateLabel = label;
        this.progress = bar;

        return panelBase;
    }

    private JPanel createDonePanel() {
        JPanel panelBase = new JPanel();
        panelBase.setLayout(new BoxLayout(panelBase, BoxLayout.Y_AXIS));

        panelBase.add(Box.createVerticalGlue());
        panelBase.add(Box.createVerticalStrut(6));

        JPanel panelLabel = new JPanel();
        panelLabel.setLayout(new BoxLayout(panelLabel, BoxLayout.X_AXIS));
        panelLabel.add(Box.createHorizontalStrut(24));
        JLabel label = new JLabel("Successfully written to " + (this.output != null ? this.output.isDirectory() ? "dir" : "" : "") + ": " + (this.output != null ? this.output.getName() : "null"));
        label.setForeground(new Color(0, 0, 0));
        label.setFont(label.getFont().deriveFont(12.0F));
        panelLabel.add(label);
        panelLabel.add(Box.createHorizontalGlue());
        panelBase.add(panelLabel);

        panelBase.add(Box.createVerticalStrut(4));

        JPanel panelBar = new JPanel();
        panelBar.setLayout(new BoxLayout(panelBar, BoxLayout.X_AXIS));
        panelBar.add(Box.createHorizontalStrut(24));
        JProgressBar bar = new JProgressBar(0, 1);
        bar.setValue(1);
        bar.setMaximumSize(new Dimension(350, 16));
        bar.setPreferredSize(new Dimension(280, 16));
        panelBar.add(bar);
        panelBar.add(Box.createHorizontalStrut(16));
        panelBase.add(panelBar);

        panelBase.add(Box.createVerticalStrut(4));

        JPanel panelOk = new JPanel();
        panelOk.setLayout(new BoxLayout(panelOk, BoxLayout.X_AXIS));
        panelOk.add(Box.createHorizontalStrut(24));
        JCheckBox box = new JCheckBox("Open output " + (this.output != null ? this.output.isDirectory() ? "folder" : "file" : "file"));
        box.setFocusable(false);
        panelOk.add(box);
        panelOk.add(Box.createHorizontalGlue());
        JPanel panelButtonOk = new JPanel();
        panelButtonOk.setLayout(new BoxLayout(panelButtonOk, BoxLayout.X_AXIS));
        JButton buttonOk = new JButton("ok");
        buttonOk.addActionListener(e -> {
            if (box.isSelected()) {
                File open;
                if (this.output == null) {
                    open = new File(".");
                } else if (this.output.isDirectory()) {
                    open = this.output;
                } else {
                    File p = this.output.getParentFile();

                    if (p.exists()) {
                        open = p;
                    } else {
                        open = new File(".");
                    }
                }
                try {
                    Desktop.getDesktop().open(open);
                } catch (IOException ex) {
                    LOGGER.error("Error occurred while opening file: {}", open);
                    LOGGER.error(ex);
                }
            }
            this.dispose();
        });
        panelButtonOk.setBorder(BorderFactory.createEmptyBorder(2, 0 ,0 ,0));
        panelButtonOk.add(buttonOk);
        panelOk.add(panelButtonOk);
        panelOk.add(Box.createHorizontalStrut(16));
        panelBase.add(panelOk);

        panelBase.add(Box.createVerticalStrut(16));

        panelBase.add(Box.createVerticalGlue());

        return panelBase;
    }

    private String formatMillis(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours != 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
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
