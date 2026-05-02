package com.artur114.bytecodegrab.util;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class Theme {
    private static Theme themeInstance = null;
    private final IThemeRef theme;

    private Theme(IThemeRef theme) {
        this.theme = theme;
    }

    private void setupI() {
        switch (this.theme.nameT()) {
            case "light":
                FlatIntelliJLaf.setup();
                break;
            case "dark":
                FlatDarculaLaf.setup();

                Color baseColor = new Color(0x323436);
                UIManager.put("Panel.background", baseColor);
                UIManager.put("JRootPane.titleBarBackground", baseColor);
                UIManager.put("J.background", baseColor);
                UIManager.put("SplitPane.background", baseColor);
                UIManager.put("TitlePane.inactiveBackground", baseColor);
                UIManager.put("TitlePane.unifiedBackground", false);
                UIManager.put("TitlePane.background", baseColor);
                UIManager.put("MenuBar.background", baseColor);
                UIManager.put("OptionPane.background", baseColor);

                Color buttonBack = new Color(60, 63, 65);
                Color buttonBorder = new Color(0x444649);

                UIManager.put("Button.borderColor", buttonBorder);
                UIManager.put("Component.borderColor", buttonBorder);
                UIManager.put("Button.background", buttonBack);
                UIManager.put("TextField.background", buttonBack);

                UIManager.put("Tree.selectionInactiveBackground", new Color(0x585D60));
                UIManager.put("List.selectionInactiveBackground", new Color(0x585D60));

                UIManager.put("ProgressBar.foreground", new Color(0x4679db));
                break;
        }
    }

    private Color loadingFrameColorI() {
        switch (this.theme.nameT()) {
            case "light":
                return null;
            case "dark":
                return new Color(0x323436);
            default:
                return null;
        }
    }

    private Color jvmLabelColorI() {
        switch (this.theme.nameT()) {
            case "light":
                return Color.DARK_GRAY;
            case "dark":
                return Color.LIGHT_GRAY;
            default:
                return null;
        }
    }

    private Color borderColorI() {
        switch (this.theme.nameT()) {
            case "light":
                return Color.LIGHT_GRAY;
            case "dark":
                return new Color(0x37393b); // 2b2d30
            default:
                return null;
        }
    }

    private Color classThreeColorI() {
        switch (this.theme.nameT()) {
            case "light":
                return new Color(239, 239, 239);
            case "dark":
                return new Color(0x2F3032);
            default:
                return null;
        }
    }

    private Color jvmListThreeColorI() {
        switch (this.theme.nameT()) {
            case "light":
                return null;
            case "dark":
                return new Color(0x37393B);
            default:
                return null;
        }
    }

    public static void newTheme(IThemeRef theme) {
        themeInstance = new Theme(theme);
    }

    public static Optional<Color> loadingFrameColor() {
        if (themeInstance != null) {
            return Optional.ofNullable(themeInstance.loadingFrameColorI());
        } else {
            throw new IllegalStateException("Theme cannot be used before it is initialized");
        }
    }

    public static Optional<Color> jvmLabelColor() {
        if (themeInstance != null) {
            return Optional.ofNullable(themeInstance.jvmLabelColorI());
        } else {
            throw new IllegalStateException("Theme cannot be used before it is initialized");
        }
    }

    public static Optional<Color> borderColor() {
        if (themeInstance != null) {
            return Optional.ofNullable(themeInstance.borderColorI());
        } else {
            throw new IllegalStateException("Theme cannot be used before it is initialized");
        }
    }

    public static Optional<Color> classThreeColor() {
        if (themeInstance != null) {
            return Optional.ofNullable(themeInstance.classThreeColorI());
        } else {
            throw new IllegalStateException("Theme cannot be used before it is initialized");
        }
    }

    public static Optional<Color> jvmListThreeColor() {
        if (themeInstance != null) {
            return Optional.ofNullable(themeInstance.jvmListThreeColorI());
        } else {
            throw new IllegalStateException("Theme cannot be used before it is initialized");
        }
    }

    public static IThemeRef themeRef() {
        if (themeInstance != null) {
            return themeInstance.theme;
        } else {
            throw new IllegalStateException("Theme cannot be used before it is initialized");
        }
    }

    public static void setup() {
        if (themeInstance != null) {
            themeInstance.setupI();
        } else {
            throw new IllegalStateException("Theme cannot be used before it is initialized");
        }
    }
}
