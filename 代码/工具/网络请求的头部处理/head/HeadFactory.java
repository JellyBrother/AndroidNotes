package com.jelly.app.main.head;

import android.content.Context;

import java.util.Map;

public class HeadFactory {

    public static Map getRequstMap(Context context, HeadType type) {
        HttpHead requstHead = getRequstHead(context, type);
        if (requstHead == null) {
            return null;
        }
        return requstHead.getMap();
    }

    public static HttpHead getRequstHead(Context context, HeadType type) {
        if (type == HeadType.FANS) {
            return HeadUtil.getFansRequstHead(context);
        }
        if (type == HeadType.SERVICE) {
            return HeadUtil.getServiceRequstHead(context);
        }
        return null;
    }

    public static HttpHead getResponseHead(Context context, HeadType type, String headJsonString) {
        if (type == HeadType.FANS) {
            return HeadUtil.getFansResponseHead(context, headJsonString);
        }
        if (type == HeadType.SERVICE) {
            // todo
        }
        return null;
    }
}
