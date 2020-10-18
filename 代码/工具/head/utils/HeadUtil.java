package com.jelly.app.main.head.utils;

import android.content.Context;

import com.jelly.app.main.head.HeadConstant;
import com.jelly.app.main.head.data.HttpHead;
import com.jelly.baselibrary.utils.LogUtil;

import org.json.JSONObject;

public class HeadUtil {

    public static HttpHead getFansRequstHead(Context context) {
        if (context == null) {
            return null;
        }
        HttpHead httpHead = new HttpHead();
        CookieUtil.setFansCookie1(context, httpHead);
        return httpHead;
    }

    public static HttpHead getServiceRequstHead(Context context) {
        if (context == null) {
            return null;
        }
        HttpHead httpHead = new HttpHead();
        httpHead.setToken("2222");
        return httpHead;
    }

    public static HttpHead parseFansResponseHead(Context context, String jsonString) {
        if (context == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return parseFansResponseHead(context, jsonObject);
        } catch (Exception e) {
            LogUtil.getInstance().e(HeadConstant.Log.TAG, "HeadUtil getFansResponseHead Exception:" + e.toString());
        }
        return null;
    }

    public static HttpHead parseFansResponseHead(Context context, JSONObject jsonObject) {
        if (context == null || jsonObject == null) {
            return null;
        }
        HttpHead httpHead = new HttpHead();
        // 解析cookie，静态变量存储存储
        String cookie = jsonObject.optString(HeadConstant.Key.COMMON_COOKIE);
        CookieUtil.saveFansCookie1(context, httpHead, cookie);
        return httpHead;
    }
}
