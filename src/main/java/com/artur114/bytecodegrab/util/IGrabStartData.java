package com.artur114.bytecodegrab.util;

import java.io.File;

public interface IGrabStartData {
    ClassNameType cnType();
    WriteType writeType();
    File file();

    enum ClassNameType {
        FULL, P_CLASS, J_CLASS
    }
    enum WriteType {
        REWRITE, ADD_R, ADD_S
    }
}
