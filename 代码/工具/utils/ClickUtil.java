package com.jelly.baselibrary.utils;

import android.view.View;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClickUtil {
    // 点击限制时间间隔
    private static final int INTERVAL_TIME_START_ACTIVITY = 500;
    private static volatile ClickUtil instance = null;
    private static LinkedHashMap<Integer, Long> sHashMap;
    private static final int sHashMapSize = 5;
    private static int sLastClickHashCode;
    private static long sLastClickTime;

    private ClickUtil() {
        sHashMap = new LinkedHashMap(sHashMapSize);
    }

    public static synchronized ClickUtil getInstance() {
        if (instance == null) {
            synchronized (ClickUtil.class) {
                if (instance == null) {
                    instance = new ClickUtil();
                }
            }
        }
        return instance;
    }

    public boolean isFastClik(View view) {
        if (view == null || sHashMap == null) {
            return false;
        }
        int hashCode = view.hashCode();
//        if (sHashMap.size() >= sHashMapSize) {
//            Iterator<Integer> it = sHashMap.keySet().iterator();
//            while (it.hasNext()) {
//                Integer x = it.next();
//                if (x != hashCode) {
//                    it.remove();
//                }
//            }
//        }
        long currentTime = System.currentTimeMillis();
        if (sHashMap.containsKey(hashCode)) {
            long lastClickTime = sHashMap.get(hashCode);
            if (currentTime - lastClickTime >= INTERVAL_TIME_START_ACTIVITY) {
                sHashMap.put(hashCode, currentTime);
                return false;
            } else {
                return true;
            }
        }
        sHashMap.put(hashCode, currentTime);
        return false;
    }

    public void clear() {
        if (sHashMap != null) {
            sHashMap.clear();
        }
    }

    public boolean isFastClik2(View view) {
        if (view == null) {
            return false;
        }
        int hashCode = view.hashCode();
        if (sLastClickHashCode != hashCode) {
            // 说明点击的控件改变了，但是INTERVAL_TIME_START_ACTIVITY内同时点击多个控件多次，还是会有判断不了的情况。
            sLastClickHashCode = hashCode;
            return false;
        }
        if (System.currentTimeMillis() - sLastClickTime >= INTERVAL_TIME_START_ACTIVITY) {
            sLastClickTime = System.currentTimeMillis();
            return false;
        }
        return true;
    }
}