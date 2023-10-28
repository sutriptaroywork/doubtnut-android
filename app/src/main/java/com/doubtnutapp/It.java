package com.doubtnutapp;

import com.google.gson.JsonObject;


public class It {
    public static String get(final JsonObject obj, final String key, final String defaultValue) {
        if (obj.has(key)) {
            return obj.getAsJsonObject(key).getAsString();
        }

        return "";
    }

    public static int get(final JsonObject obj, final String key, int defaultValue) {

        return 0;
    }
}
