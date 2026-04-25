package com.artur114.bytecodegrab.main;

import com.artur114.bytecodegrab.jcomp.JLoadingFrame;
import com.artur114.bytecodegrab.present.AppPresenter;
import com.artur114.bytecodegrab.util.Icons;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.sun.tools.attach.VirtualMachine;

import javax.swing.*;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Bootstrap {
    private static final Bootstrap bootstrap = new Bootstrap();

    public static Bootstrap bootstrap() {
        return bootstrap;
    }

    protected void start(String[] args) {
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
        this.preLaunch();
        JLoadingFrame loading = new JLoadingFrame();
        loading.setVisible(true);
        FlatIntelliJLaf.setup();

        SwingUtilities.invokeLater(() -> {
            Application.init();
            AppPresenter.init();

            AppPresenter.presenter().init(Application.application());

            loading.dispose();
            Application.application().setVisible(true);
        });
    }

    private void launchBootstrapper(Path thisJarPath) {
        this.preLaunch();
        JLoadingFrame loading = new JLoadingFrame();
        loading.setVisible(true);
        FlatIntelliJLaf.setup();

        SwingUtilities.invokeLater(() -> {
            AppBootstrap.init(thisJarPath);

            loading.dispose();
            AppBootstrap.application().view();
        });
    }

    private void preLaunch() {
        Icons.newIcons(() -> "light");
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
