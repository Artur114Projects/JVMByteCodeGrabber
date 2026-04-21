package com.artur114.bytecodegrab.agent;


import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GrabberTransformer implements ClassFileTransformer {
    private Set<String> targetClasses = new HashSet<>();
    private IBytecodeOutput output = (s, b) -> {};
    private final AgentLoggerAsync logger;

    public GrabberTransformer() {
        this.logger = IAgentLogger.createBestAsyncLogger();
    }

    public void setTargetClasses(List<String> classes) {
        if (classes == null) {
            this.logger.shutdown();
            this.targetClasses = null; return;
        }

        this.targetClasses.clear();

        this.targetClasses.addAll(classes);
    }

    public void setBytecodeOutput(IBytecodeOutput output) {
        this.output = output;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            if (this.targetClasses == null) {
                return classfileBuffer;
            }
            if (className != null && this.targetClasses.contains(className.replace("/", "."))) {
                this.logger.info("Outputting class: {}", className);
                this.output.out(className, classfileBuffer);
            }
        } catch (Exception e) {
            this.logger.error("An error occurs while transform class: {}, skipping...", className);
        }
        return classfileBuffer;
    }
}