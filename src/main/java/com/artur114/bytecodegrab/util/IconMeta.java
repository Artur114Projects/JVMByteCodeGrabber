package com.artur114.bytecodegrab.util;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class IconMeta {
    public Map<String, IconJ> themes;

    public static class IconJ {
        @SerializedName("disabled")
        String deIconPath;
        @SerializedName("default")
        String iconPath;
        @SerializedName("all")
        String all;
    }
}
