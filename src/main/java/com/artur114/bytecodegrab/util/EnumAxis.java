package com.artur114.bytecodegrab.util;

import javax.swing.*;

public enum EnumAxis {
    X_AXIS, Y_AXIS;

    public int toBoxLayout() {
        switch (this) {
            case X_AXIS:
                return BoxLayout.X_AXIS;
            case Y_AXIS:
                return BoxLayout.Y_AXIS;
            default:
                return -1;
        }
    }
}
