package com.artur114.bytecodegrab.main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LaunchArgs {
    public static LaunchArgs parse(String[] args) {
        return new LaunchArgs(args);
    }

    private final Set<String> args;

    private LaunchArgs(String[] args) {
        this.args = new HashSet<>(Arrays.asList(args));
    }

    public boolean hasArg(String arg) {
        for (String a : this.args) {
            if (a.equals(arg)) {
                return true;
            }
            String[] split = a.split("=");

            if (split.length == 2) {
                if (split[0].equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int argValueI(String arg) {
        for (String a : this.args) {
            if (a.contains(arg)) {
                String[] split = a.split("=");

                if (split.length == 2) {
                    if (split[0].equals(arg)) {
                        return Integer.parseInt(split[1]);
                    }
                }
            }
        }
        throw new IllegalArgumentException();
    }

    public String argValueS(String arg) {
        for (String a : this.args) {
            if (a.contains(arg)) {
                String[] split = a.split("=");

                if (split.length == 2) {
                    if (split[0].equals(arg)) {
                        return split[1];
                    }
                }
            }
        }
        throw new IllegalArgumentException();
    }
}
