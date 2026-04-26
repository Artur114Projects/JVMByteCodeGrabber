package com.artur114.bytecodegrab.conf;

import com.artur114.bytecodegrab.main.AppBootstrap;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppConfig {
    private static final Gson gson = new Gson();
    private final ConfigObject data;

    private AppConfig(ConfigObject data) {
        this.data = data;
    }

    public GrabConfig grabConfig() {
        return this.data.grabConfig;
    }

    public ThemeConfig themeConfig() {
        return this.data.themeConfig;
    }

    public void save() {
        saveConfig(this.data);
    }

    public static AppConfig load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            return new AppConfig(new ConfigObject());
        }
        try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
            ConfigObject config = gson.fromJson(isr, ConfigObject.class);
            if (config == null) return new AppConfig(new ConfigObject());
            return new AppConfig(config);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return new AppConfig(new ConfigObject());
    }

    private static void saveConfig(ConfigObject config) {
        try (OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(configPath()), StandardCharsets.UTF_8)) {
            osw.write(gson.toJson(config));
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private static Path configPath() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path bcDir = Paths.get(tempDir, "ByteCodeGrabber");
        try {
            Files.createDirectories(bcDir);
        } catch (IOException ignored) {}
        return bcDir.resolve("main-data.json");
    }

    private static class ConfigObject {
        public ThemeConfig themeConfig = new ThemeConfig();
        public GrabConfig grabConfig = new GrabConfig();
    }
}
