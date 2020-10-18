package com.jelly.app.main.head.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.jelly.app.main.head.HeadConstant;

public class SaveUtil {
    private static final String HTTPHEAD_SP_NAME = "HttpHead";

    public static void saveCookie(Context context, String cookie) {
        putString(context, HeadConstant.Key.COMMON_COOKIE, cookie);
    }

    public static String getCookie(Context context) {
        return getString(context, HeadConstant.Key.COMMON_COOKIE);
    }

    private static void putString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(HTTPHEAD_SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    private static String getString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(HTTPHEAD_SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }
}
