package com.jelly.baselibrary.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jelly.baselibrary.R;
import com.jelly.baselibrary.common.BaseCommon;

import java.util.List;

/**
 * Description：工具类
 */
public class PublicUtil {
    private static final String TAG = "PublicUtils";

    /**
     * Description：判定集合是否为空
     *
     * @param list 集合
     * @return true是空集合
     */
    public static boolean isEmptyList(List list) {
        if (list == null) {
            return true;
        }
        if (list.isEmpty()) {
            return true;
        }
        return false;
    }

    public static <T> boolean isEmptyArray(T[] list) {
        return list == null || list.length == 0;
    }

    /**
     * Description：判断是否有网络
     *
     * @param context 上下文
     * @return true没有网络
     */
    public static boolean isNetWorkDisconnect(Context context) {
        //连接服务 CONNECTIVITY_SERVICE
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //网络信息 NetworkInfo
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return false;
        }
        //没有网络
        ToastUtil.makeText(R.string.base_no_net);
        return true;
    }

    public static String getString(String text){
        return (text + "").replace("null", "");
    }
}
