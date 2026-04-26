package com.artur114.bytecodegrab.main;

import com.artur114.bytecodegrab.conf.AppConfig;
import com.artur114.bytecodegrab.frame.JLoadingFrame;
import com.artur114.bytecodegrab.present.AppPresenter;
import com.artur114.bytecodegrab.util.EnumTheme;
import com.artur114.bytecodegrab.util.IThemeRef;
import com.artur114.bytecodegrab.util.Icons;
import com.artur114.bytecodegrab.util.Theme;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.sun.tools.attach.VirtualMachine;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Bootstrap {
    private static final Bootstrap bootstrap = new Bootstrap();

    private LaunchArgs args = null;

    public static Bootstrap bootstrap() {
        return bootstrap;
    }

    protected void start(String[] args) {
        this.args = LaunchArgs.parse(args);

        if (!this.launchedFromJdk()) {
            try {
                Path path = this.thisJarPath();

                if (this.canBootstrap(path)) {
                    this.launchBootstrapper(path);
                } else {
                    FlatIntelliJLaf.setup();
                    JOptionPane.showMessageDialog(null, "Software Requires JDK to Work", "JDK Required", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        this.launch();
    }

    private void launch() {
        AppConfig config = AppConfig.load();
        EnumTheme theme = EnumTheme.fromInt(config.themeConfig().selectedTheme);
        if (theme == null) {
            theme = EnumTheme.SYSTEM;
            config.themeConfig().selectedTheme = 0;
            config.save();
        }
        this.preLaunch(theme);
        JLoadingFrame loading = new JLoadingFrame();
        loading.setVisible(true);
        this.initTheme();

        SwingUtilities.invokeLater(() -> {
            Application.init(config);
            AppPresenter.init();

            AppPresenter.presenter().init(Application.application());

            loading.dispose();
            Application.application().setVisible(true);
        });
    }

    private void launchBootstrapper(Path thisJarPath) {
        this.preLaunch(EnumTheme.LIGHT);
        JLoadingFrame loading = new JLoadingFrame();
        loading.setVisible(true);
        this.initTheme();

        SwingUtilities.invokeLater(() -> {
            AppBootstrap.init(thisJarPath);

            loading.dispose();
            AppBootstrap.application().view();
        });
    }

    private void preLaunch(IThemeRef theme) {
        Theme.newTheme(theme);
        Icons.newIcons(theme);
    }

    private void initTheme() {
        Theme.setup();
    }

    public boolean launchJar(Path jar, File jdk) {
        String classpath = jar.toString();
        Path tools = jdk.toPath().resolve("lib/tools.jar");
        if (Files.exists(tools)) {
            classpath += File.pathSeparator + tools;
        }

        String os = System.getProperty("os.name").toLowerCase();
        String javaExecutable = os.contains("win") ? "java.exe" : "java";
        Path javaExe = jdk.toPath().resolve("bin").resolve(javaExecutable);

        List<String> run = new ArrayList<>();
        run.add(javaExe.toString());
        run.add("-classpath");
        run.add(classpath);
        run.add("com.artur114.bytecodegrab.main.Main");
        run.add("--relaunched=" + jdk);

        try {
            ProcessBuilder pb = new ProcessBuilder(run);
            pb.inheritIO();
            pb.start();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }

        return true;
    }

    public boolean tryRelaunch() {
        String exePath = System.getProperty("launch4j.exe.path");
        if (exePath != null) {
            try {
                new ProcessBuilder(exePath).inheritIO().start();
                return true;
            } catch (IOException e) {
                e.printStackTrace(System.err);
                return false;
            }
        }

        Path path;
        try {
            path = this.thisJarPath();
        } catch (URISyntaxException e) {
            return false;
        }

        if (this.canBootstrap(path)) {
            List<String> run = this.relaunchArgs(path);
            if (run == null) {
                return false;
            }
            try {
                ProcessBuilder pb = new ProcessBuilder(run);
                pb.inheritIO();
                pb.start();
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            return false;
        }
    }

    private List<String> relaunchArgs(Path jar) {
        File java = null;
        Path javaEx;

        if (this.args.hasArg("--relaunched")) {
            java = new File(this.args.argValueS("--relaunched"));
        }

        if (java == null || !java.exists()) {
            java = new File(System.getProperty("java.home"));
        }

        if (!java.exists()) {
            return null;
        }

        javaEx = java.toPath().resolve("bin").resolve(System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java");

        if (!Files.exists(javaEx)) {
            return null;
        }

        List<String> list = new ArrayList<>();

        String classpath = jar.toString();
        Path tools = java.toPath().resolve("lib/tools.jar");
        if (Files.exists(tools)) {
            classpath += File.pathSeparator + tools;
        }

        list.add(javaEx.toString());
        list.add("-classpath");
        list.add(classpath);
        list.add("com.artur114.bytecodegrab.main.Main");
        list.add("--relaunched=" + java);

        return list;
    }

    private Path thisJarPath() throws URISyntaxException {
        URL url = Bootstrap.class.getProtectionDomain().getCodeSource().getLocation();
        return Paths.get(url.toURI());
    }

    private boolean canBootstrap(Path jar) {
        if (jar == null) return false;
        File file = jar.toFile();
        return file.exists() && file.getName().endsWith(".jar");
    }

    private boolean launchedFromJdk() {
        try {
            VirtualMachine.list();
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }
}
