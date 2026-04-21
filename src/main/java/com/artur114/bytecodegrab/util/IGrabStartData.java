package com.artur114.bytecodegrab.util;

import java.io.File;

public interface IGrabStartData {
    WriteType type();
    File file();

    enum WriteType {
        FULL, P_CLASS, J_CLASS
    }
}
