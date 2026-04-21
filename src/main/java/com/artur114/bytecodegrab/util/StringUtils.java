package com.artur114.bytecodegrab.util;

public class StringUtils {
    public static int intPropFromMessage(String message, String property) {
        int index = message.indexOf(property + ":");

        if (index == -1) {
            throw new IllegalArgumentException();
        }

        int start = message.indexOf('[', index);
        int end = message.indexOf(']', index);

        if (start == -1 || end == -1 || end - 1 <= start) {
            throw new IllegalArgumentException();
        }

        return Integer.parseInt(message.substring(start + 1, end).replace(" ", ""));
    }

    public static String stringPropFromMessage(String message, String property) {
        int index = message.indexOf(property + ":");

        if (index == -1) {
            throw new IllegalArgumentException();
        }

        int start = message.indexOf('[', index);
        int end = message.indexOf(']', index);

        if (start == -1 || end == -1 || end - 1 <= start) {
            throw new IllegalArgumentException();
        }

        return message.substring(start + 1, end).replace(" ", "");
    }
}
