package com.jelly.app.main.head;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Map;

public class HeadUtil {
    private static String sCookie = "";

    public static HttpHead getFansRequstHead(Context context) {
        HttpHead httpHead = new HttpHead();
        // 将cookie置为静态变量存储
        if (TextUtils.isEmpty(sCookie)) {
            // 从永久存储中取cookie
            // 解密cookie
            // 校验cookie的时效性，如果失效了，就网络获取，在响应头设为永久保存
            if (true) {

            } else {
                // 临时变量有，并且有效，就设值
                httpHead.setCookie(sCookie);
            }
        }
        return httpHead;
    }

    public static HttpHead getServiceRequstHead(Context context) {
        HttpHead httpHead = new HttpHead();
        httpHead.setToken("2222");
        return httpHead;
    }

    public static HttpHead getFansResponseHead(Context context, String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return getFansResponseHead(context, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HttpHead getFansResponseHead(Context context, JSONObject jsonObject) {
        HttpHead httpHead = new HttpHead();
        // 解析cookie，静态变量存储存储
        sCookie = jsonObject.optString(HeadConstant.Key.COMMON_COOKIE);
        httpHead.setCookie(sCookie);
        // 加密cookie
        // 存储cookie
        return httpHead;
    }
}
