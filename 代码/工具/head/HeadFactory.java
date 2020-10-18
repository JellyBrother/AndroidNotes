package com.jelly.app.main.head;

import android.content.Context;

import com.jelly.app.main.head.data.HttpHead;
import com.jelly.app.main.head.utils.HeadUtil;
import com.jelly.baselibrary.utils.LogUtil;

import org.json.JSONObject;

import java.util.Map;

public class HeadFactory {

    public static Map getRequstMap(Context context, HeadType type) {
        if (context == null) {
            return null;
        }
        HttpHead requstHead = getRequstHead(context, type);
        if (requstHead == null) {
            return null;
        }
        return requstHead.getMap();
    }

    public static HttpHead getRequstHead(Context context, HeadType type) {
        if (context == null) {
            return null;
        }
        if (type == HeadType.FANS) {
            return HeadUtil.getFansRequstHead(context);
        }
        if (type == HeadType.SERVICE) {
            return HeadUtil.getServiceRequstHead(context);
        }
        return null;
    }

    public static HttpHead parseResponseHead(Context context, HeadType type, String headJsonString) {
        if (context == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(headJsonString);
            return parseResponseHead(context, type, jsonObject);
        } catch (Exception e) {
            LogUtil.getInstance().e(HeadConstant.Log.TAG, "HeadFactory getResponseHead Exception:" + e.toString());
        }
        return null;
    }

    public static HttpHead parseResponseHead(Context context, HeadType type, JSONObject headJsonObject) {
        if (context == null) {
            return null;
        }
        if (type == HeadType.FANS) {
            return HeadUtil.parseFansResponseHead(context, headJsonObject);
        }
        if (type == HeadType.SERVICE) {
            // todo
        }
        return null;
    }
}
