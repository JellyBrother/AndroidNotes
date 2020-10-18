package com.jelly.app.main.head.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.jelly.baselibrary.common.BaseCommon;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Head implements Serializable, Parcelable {
    private static final long serialVersionUID = -2683014969141793652L;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mMap);
    }

    public Head() {
    }

    protected Head(Parcel in) {
        this.mMap = (LinkedHashMap<String, String>) in.readSerializable();
    }

    public static final Creator<Head> CREATOR = new Creator<Head>() {
        @Override
        public Head createFromParcel(Parcel source) {
            return new Head(source);
        }

        @Override
        public Head[] newArray(int size) {
            return new Head[size];
        }
    };

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
        if (TextUtils.isEmpty(key) || value == null) {
            return;
        }
        mMap.put(key, value);
    }

    public void put(Head head) {
        if (head == null) {
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
        return new JSONObject(mMap).toString();

//        StringBuilder stringBuilder = new StringBuilder("{\"Head\":");
//        for (String key : mMap.keySet()) {
//            String value = mMap.get(key);
//            if (TextUtils.isEmpty(key)) {
//                continue;
//            }
//            stringBuilder.append("\"");
//            stringBuilder.append(key);
//            stringBuilder.append("\"");
//            stringBuilder.append(":");
//            if (BaseCommon.Base.isDebug) {
//                stringBuilder.append(value);
//            } else if (value != null && value.length() > 1) {
//                // 对value进行脱敏处理
//                stringBuilder.append(value.charAt(0));
//            }
//            stringBuilder.append(",");
//        }
//        return stringBuilder.toString();
    }

    public Map getMap() {
        return mMap;
    }
}
