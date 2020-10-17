package com.jelly.app.main.head;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Head {
    private LinkedHashMap<String, String> mMap = new LinkedHashMap();

    public int size() {
        return mMap.size();
    }

    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    public boolean containsKey(String key) {
        return mMap.containsKey(key);
    }

    public boolean containsValue(String s) {
        return mMap.containsValue(s);
    }

    public String get(String key) {
        return mMap.get(key);
    }

    public void put(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        mMap.put(key, value);
    }

    public void put(Head head) {
        if (head == null || head.getMap() == null || head.getMap().isEmpty()) {
            return;
        }
        Map map = head.getMap();
        if (map == null || map.isEmpty()) {
            return;
        }
        map.putAll(map);
    }

    public void putAll(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        mMap.putAll(map);
    }

    public void clear() {
        mMap.clear();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Head:");
        Set<String> strings = mMap.keySet();
        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = mMap.get(key);
            stringBuilder.append(key);
            stringBuilder.append(":");
            stringBuilder.append(value);
        }
        return stringBuilder.toString();
    }

    public Map getMap() {
        return mMap;
    }
}
