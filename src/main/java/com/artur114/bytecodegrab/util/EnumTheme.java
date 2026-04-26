package com.artur114.bytecodegrab.util;

import com.jthemedetecor.OsThemeDetector;

public enum EnumTheme implements IThemeRef {
    SYSTEM, DARK, LIGHT;
    private static final EnumTheme systemTheme = OsThemeDetector.getDetector().isDark() ? DARK : LIGHT;

    @Override
    public String nameT() {
        switch (this) {
            case DARK:
                return "dark";
            case LIGHT:
                return "light";
            case SYSTEM:
                return systemTheme.nameT();
        }

        return "";
    }

    public static EnumTheme fromInt(int index) {
        if (index >= 3) return null;
        return values()[index];
    }
}
