package com.artur114.bytecodegrab.agent;

public interface IBytecodeOutput {
    void out(String className, byte[] bytes);
}
