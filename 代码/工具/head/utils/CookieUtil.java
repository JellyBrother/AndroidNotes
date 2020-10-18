package com.jelly.app.main.head.utils;

import android.content.Context;
import android.text.TextUtils;

import com.jelly.app.main.head.HeadConstant;
import com.jelly.app.main.head.data.HttpHead;
import com.jelly.app.main.security.EncryptionUtils;
import com.jelly.baselibrary.utils.LogUtil;

public class CookieUtil {
    // 将cookie置为静态变量存储
    private static volatile String sFansCookie1 = "";

    protected static void setFansCookie1(Context context, HttpHead httpHead) {
        setCookie(context, httpHead, sFansCookie1);
    }

    protected static void saveFansCookie1(Context context, HttpHead httpHead, String cookie) {
        sFansCookie1 = cookie;
        saveCookie(context, httpHead, cookie);
    }

    protected static void setCookie(Context context, HttpHead httpHead, String sCookie) {
        if (TextUtils.isEmpty(sCookie)) {
            // 从本地取
            String cookie = SaveUtil.getCookie(context);
            try {
                // 解密
                sCookie = EncryptionUtils.decryptS2S(cookie);
            } catch (Exception e) {
                LogUtil.getInstance().e(HeadConstant.Log.TAG, "CookieUtil setCookie Exception:" + e.toString());
            }
        }
        /**
         * 加密、存储、解密、取出  都不止一个cookie的
         * 跟业务相关，有耦合，除非外面传type，但是传type会导致后期不好维护，不传type，会导致方法太多，代码重复。
         * 个人觉得不传type好一点，方便业务定制化。
         */
        if (TextUtils.isEmpty(sCookie)) {
            httpHead.setCookieUrl(HeadConstant.Url.COMMON_COOKIE_URL);
            return;
        }
        // 校验cookie的时效性，如果失效了，就网络获取，在响应头设为永久保存
//        if (true) {
//            httpHead.setCookieUrl(HeadConstant.Url.COMMON_COOKIE_URL);
//        } else {
        // cookie有效，就设值
        httpHead.setCookie(sCookie);
//        }
    }


    protected static void saveCookie(Context context, HttpHead httpHead, String cookie) {
        httpHead.setCookie(cookie);
        try {
            // 加密
            String encryptS2S = EncryptionUtils.encryptS2S(cookie);
            // 存储
            SaveUtil.saveCookie(context, encryptS2S);
        } catch (Exception e) {
            LogUtil.getInstance().e(HeadConstant.Log.TAG, "CookieUtil setCookie Exception:" + e.toString());
        }
    }
}
