package com.ck.dev.punjabify.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class PreferenceManager {

    public PreferenceManager() {
    }

    private static SharedPreferences init(Context context) {
        return context.getSharedPreferences(PreferenceConfig.META_DATA, Context.MODE_PRIVATE);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences.Editor editor = init(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        return init(context).getString(key, null);
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = init(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static Integer getInt(Context context, String key) {
        return init(context).getInt(key, -1);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = init(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static Boolean getBoolean(Context context, String key) {
        return init(context).getBoolean(key, false);
    }

}
