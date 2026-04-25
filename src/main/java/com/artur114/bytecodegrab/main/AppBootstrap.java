package com.artur114.bytecodegrab.main;

import com.artur114.bytecodegrab.util.Icons;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

public class AppBootstrap extends JFrame {
    private static AppBootstrap application = null;
    private final Gson gson = new Gson();
    private final Path thisJarPath;
    private final Config config;

    public AppBootstrap(Path thisJarPath) {
        this.thisJarPath = thisJarPath;
        this.config = this.loadConfig();
        this.setSize(310, 140);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setTitle("BCG Bootstrap");
        this.setIconImage(Icons.image("icon_black"));
        this.setResizable(false);

        this.initView();
    }

    private void initView() {
        JPanel panelY = new JPanel();
        panelY.setLayout(new BoxLayout(panelY, BoxLayout.Y_AXIS));

        panelY.add(Box.createVerticalGlue());

        JPanel panelLabel = new JPanel();
        panelLabel.setLayout(new BoxLayout(panelLabel, BoxLayout.X_AXIS));
        panelLabel.add(Box.createHorizontalStrut(17));
        JLabel label = new JLabel("Please select launch JDK");
        panelLabel.add(label);
        panelLabel.add(Box.createHorizontalGlue());
        panelLabel.setMaximumSize(new Dimension(1000000, 24));
        panelY.add(panelLabel);

        panelY.add(Box.createVerticalStrut(4));

        JPanel panelJdk = new JPanel();
        panelJdk.setLayout(new BoxLayout(panelJdk, BoxLayout.X_AXIS));
        panelJdk.add(Box.createHorizontalStrut(15));
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(1000000, 23));
        field.setFont(field.getFont().deriveFont(11.0F));
        field.setText(this.config.jdkPath);
        panelJdk.add(field);
        JButton button = new JButton(Icons.iconQuad("folder", 16));
        button.setToolTipText("Browse");
        button.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(new File("."));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            File current = new File(field.getText());
            if (current.exists()) {
                if (!current.isDirectory()) {
                    current = current.getParentFile();
                }
                if (current != null) {
                    chooser.setCurrentDirectory(current);
                }
            }

            int ret = chooser.showDialog(this, "Set JDK");

            if (ret == 0) {
                field.setText(chooser.getSelectedFile().toString());
                this.config.jdkPath = field.getText();
            }
        });
        button.setFocusable(false);
        panelJdk.add(button);
        panelJdk.setMaximumSize(new Dimension(1000000, 24));
        panelJdk.add(Box.createHorizontalStrut(16));
        panelY.add(panelJdk);

        panelY.add(Box.createVerticalStrut(6));

        JPanel panelLaunch = new JPanel();
        panelLaunch.setLayout(new BoxLayout(panelLaunch, BoxLayout.X_AXIS));
        panelLaunch.add(Box.createHorizontalStrut(15));
        JCheckBox save = new JCheckBox("Save JDK for next launches");
        save.setSelected(this.config.checkBox);
        save.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        JButton launch = new JButton("Launch");
        launch.addActionListener(e -> {
            File file = new File(field.getText());
            String os = System.getProperty("os.name").toLowerCase();
            String javaExecutable = os.contains("win") ? "java.exe" : "java";
            Path javaExe = file.toPath().resolve("bin").resolve(javaExecutable);
            if (file.exists() && file.isDirectory() && Files.exists(javaExe)) {
                try {
                    String classpath = thisJarPath.toString();
                    Path tools = file.toPath().resolve("lib/tools.jar");
                    if (Files.exists(tools)) {
                        classpath += File.pathSeparator + tools;
                    }
                    ProcessBuilder pb = new ProcessBuilder(javaExe.toString(), "-classpath", classpath, "com.artur114.bytecodegrab.main.Main");
                    pb.inheritIO();
                    pb.start();
                    if (save.isSelected()) {
                        this.config.checkBox = true;
                        this.saveConfig(this.config);
                    } else {
                        this.config.checkBox = false;
                        this.config.jdkPath = "";
                        this.saveConfig(this.config);
                    }
                    System.exit(0);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "An error occurred during a launch attempt", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select valide JDK folder", "Invalid folder", JOptionPane.WARNING_MESSAGE);
            }
        });
        panelLaunch.add(save);
        panelLaunch.add(Box.createHorizontalGlue());
        panelLaunch.add(launch);
        panelLaunch.setMaximumSize(new Dimension(1000000, 22));
        panelLaunch.add(Box.createHorizontalStrut(16));
        panelY.add(panelLaunch);

        panelY.add(Box.createVerticalGlue());

        this.add(panelY);

        this.getRootPane().setDefaultButton(launch);
    }

    public void view() {
        this.setVisible(true);

        if (this.config.jdkPath != null && !this.config.jdkPath.isEmpty()) {
            this.getRootPane().getDefaultButton().requestFocusInWindow();
        }
    }

    private Config loadConfig() {
        Path path = this.configPath();
        if (!Files.exists(path)) {
            return new Config();
        }
        try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
            Config config = this.gson.fromJson(isr, Config.class);
            if (config == null) return new Config();
            return config;
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return new Config();
    }

    private void saveConfig(Config config) {
        try (OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(this.configPath()), StandardCharsets.UTF_8)) {
            osw.write(this.gson.toJson(config));
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private Path configPath() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path bcDir = Paths.get(tempDir, "ByteCodeGrabber");
        try {
            Files.createDirectories(bcDir);
        } catch (IOException ignored) {}
        return bcDir.resolve("bootstrap.json");
    }

    public static AppBootstrap application() {
        return application;
    }

    protected static void init(Path thisJarPath) {
        application = new AppBootstrap(thisJarPath);
    }

    private static class Config {
        private boolean checkBox;
        private String jdkPath;
    }
}
