package com.artur114.bytecodegrab.frame;

import com.artur114.bytecodegrab.conf.AppConfig;
import com.artur114.bytecodegrab.conf.GrabConfig;
import com.artur114.bytecodegrab.jcomp.JCardContainer;
import com.artur114.bytecodegrab.jcomp.JDynLabel;
import com.artur114.bytecodegrab.main.Application;
import com.artur114.bytecodegrab.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class JGrabFrame extends JFrame {
    private static final Logger LOGGER = LogManager.getLogger("View/JGrabFrame");
    private final IListenBuss<IListener<IGrabStartData>, IGrabStartData> grabListenBuss = new ArrayEDITListenBuss<>();
    private final IListenBuss<IListener<Void>, Void> grabAbortListenBuss = new ArrayEDITListenBuss<>();
    private final IListenBuss<IListener<Void>, Void> frameCloseListenBuss = new ArrayEDITListenBuss<>();
    private final JCardContainer card;
    private final AppConfig config;
    private boolean isDone = false;
    private JProgressBar progress;
    private JLabel labelTimePassed;
    private JLabel labelTimeLeft;
    private JLabel stateLabel;
    private File output = null;
    private long startTime = -1;
    private JButton grabButton;
    private JButton okButton;
    private JDynLabel doneLabel;
    private JCheckBox doneBox;

    public JGrabFrame(Application parent) {
        this.config = parent.appData;
        this.setSize(340, 160);
        this.setLocationRelativeTo(parent);
        this.setTitle("Grabber");
        this.setIconImage(Icons.image("icon"));
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
                if (doneBox != null) {
                    config.grabConfig().checkBoxOPState = doneBox.isSelected();
                    config.save();
                }
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
        this.card.addCardChangeListener(value -> {
            switch (value) {
                case "select":
                    this.getRootPane().setDefaultButton(this.grabButton);
                    break;
                case "done":
                    this.getRootPane().setDefaultButton(this.okButton);
                    this.okButton.requestFocusInWindow();
                    this.doneLabel.update();
                    this.doneBox.setText("Open output " + (this.output != null ? this.output.isDirectory() ? "folder" : "file" : "file"));
                    break;
            }
        });

        this.card.show("select");
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

    public void view() {
        this.setVisible(true);

        if (this.config.grabConfig().checkBoxState) {
            this.grabButton.requestFocusInWindow();
        }
    }

    private JPanel createSelectPanel() {
        JPanel panelBase = new JPanel();
        panelBase.setLayout(new BoxLayout(panelBase, BoxLayout.Y_AXIS));

        panelBase.add(Box.createVerticalGlue());
        panelBase.add(Box.createVerticalStrut(6));

        JPanel panelFile = new JPanel();
        panelFile.setLayout(new BoxLayout(panelFile, BoxLayout.X_AXIS));
        panelFile.add(Box.createHorizontalStrut(32));
        JLabel labelFile = new JLabel("Output: ");
        labelFile.setFont(labelFile.getFont().deriveFont(12.0F));
        labelFile.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        panelFile.add(labelFile);
        JTextField field = new JTextField(this.config.grabConfig().lastSaveFolder);
        field.setFont(field.getFont().deriveFont(11.0F));
        field.setMaximumSize(new Dimension(200, 22));
        panelFile.add(field);
        JButton buttonBrowse = new JButton(Icons.iconQuad("folder", 16));
        buttonBrowse.setToolTipText("Browse");
        buttonBrowse.setFocusable(false);
        buttonBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(false);
            chooser.removeChoosableFileFilter(chooser.getChoosableFileFilters()[0]);
            chooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".zip");
                }

                @Override
                public String getDescription() {
                    return "All formats (*.jar, *.zip, dir)";
                }
            });
            chooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".jar");
                }

                @Override
                public String getDescription() {
                    return "jar files (*.jar)";
                }
            });
            chooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".zip");
                }

                @Override
                public String getDescription() {
                    return "zip files (*.zip)";
                }
            });
            chooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "dirs";
                }
            });

            File current = new File(field.getText());
            if (current.exists()) {
                if (!current.isDirectory()) {
                    current = current.getParentFile();
                }
                if (current != null) {
                    chooser.setCurrentDirectory(current);
                }
            }

            int i = chooser.showDialog(Application.application(), "Set output");

            if (i == 0) {
                field.setText(this.fixFileIfNeeded(chooser.getSelectedFile()).toString());
            }
        });
        panelFile.add(buttonBrowse);
        panelFile.setMaximumSize(new Dimension(400, 24));
        panelFile.add(Box.createHorizontalStrut(32));
        panelBase.add(panelFile);

        panelBase.add(Box.createVerticalStrut(5));

        JPanel panelComboBox = new JPanel();
        panelComboBox.setLayout(new BoxLayout(panelComboBox, BoxLayout.X_AXIS));
        panelComboBox.add(Box.createHorizontalStrut(32));
        JLabel label = new JLabel("Write format: ");
        label.setFont(label.getFont().deriveFont(12.0F));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        panelComboBox.add(label);
        String[] options = {"Full package", "Package + Class name", "Just class name"};
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedIndex(this.config.grabConfig().lastWriteFormat);
        comboBox.setFocusable(false);
        comboBox.setMaximumSize(new Dimension(400, 22));
        panelComboBox.setMaximumSize(new Dimension(400, 22));
        panelComboBox.add(comboBox);
        panelComboBox.add(Box.createHorizontalStrut(32));
        panelBase.add(panelComboBox);

        panelBase.add(Box.createVerticalStrut(10));

        JPanel panelButtonGrab = new JPanel();
        panelButtonGrab.setLayout(new BoxLayout(panelButtonGrab, BoxLayout.X_AXIS));
        panelButtonGrab.add(Box.createHorizontalStrut(28));
        JCheckBox checkBox = new JCheckBox("Save preset for next grab");
        checkBox.setSelected(this.config.grabConfig().checkBoxState);
        panelButtonGrab.add(checkBox);
        panelButtonGrab.add(checkBox);
        panelButtonGrab.add(Box.createHorizontalGlue());
        JButton buttonGrab = new JButton("Grab");
        buttonGrab.addActionListener(e -> {
            IGrabStartData.WriteType type = IGrabStartData.WriteType.values()[comboBox.getSelectedIndex()];
            File output = this.fixFileIfNeeded(new File(field.getText()));
            if (output.getParentFile() == null || output.getParentFile().exists()) {
                field.setText(output.toString());
                GrabConfig data = this.config.grabConfig();
                if (checkBox.isSelected()) {
                    data.lastSaveFolder = field.getText();
                    data.lastWriteFormat = comboBox.getSelectedIndex();
                    data.checkBoxState = true;
                } else {
                    data.lastSaveFolder = "";
                    data.lastWriteFormat = 0;
                    data.checkBoxState = false;
                }
                this.config.save();
                this.output = output;
                this.grabListenBuss.listen(new GrabData(type, output));
            } else {
                JOptionPane.showMessageDialog(this, "This path does not exist", "Grabb error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelButtonGrab.add(buttonGrab);
        panelButtonGrab.add(Box.createHorizontalStrut(32));
        panelBase.add(panelButtonGrab);

        panelBase.add(Box.createVerticalStrut(8));
        panelBase.add(Box.createVerticalGlue());

        this.getRootPane().setDefaultButton(this.grabButton = buttonGrab);

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

        JLabel labelTime = new JLabel("Time left: calculating");

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
        JDynLabel label = new JDynLabel(() -> "Successfully written to " + (this.output != null ? this.output.isDirectory() ? "dir" : "" : "") + ": " + (this.output != null ? this.output.getName() : "null"));
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
        box.setSelected(this.config.grabConfig().checkBoxOPState);
        panelOk.add(box);
        panelOk.add(Box.createHorizontalGlue());
        JPanel panelButtonOk = new JPanel();
        panelButtonOk.setLayout(new BoxLayout(panelButtonOk, BoxLayout.X_AXIS));
        JButton buttonOk = new JButton("Ok");
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
            if (this.doneBox != null) {
                this.config.grabConfig().checkBoxOPState = this.doneBox.isSelected();
                this.config.save();
            }
            this.dispose();
        });
        panelButtonOk.add(buttonOk);
        panelOk.add(panelButtonOk);
        panelOk.add(Box.createHorizontalStrut(16));
        panelBase.add(panelOk);

        panelBase.add(Box.createVerticalStrut(16));
        panelBase.add(Box.createVerticalGlue());

        this.okButton = buttonOk;
        this.doneLabel = label;
        this.doneBox = box;

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

    private File fixFileIfNeeded(File file) {
        if (file.getName().endsWith(".jar") || file.getName().endsWith("zip") || file.isDirectory()) {
            return file;
        }
        return new File(file.getAbsolutePath() + ".jar");
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
