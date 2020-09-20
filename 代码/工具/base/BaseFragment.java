package com.jelly.baselibrary.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jelly.baselibrary.utils.LogUtil;

/**
 * Description：Activity的基类
 */
public abstract class BaseFragment extends Fragment {
    private static String TAG = "BaseFragment";
    // 上下文
    protected Activity mActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TAG = this.getClass().getSimpleName();
        LogUtil.getInstance().d(TAG, "onCreateView");
        mActivity = getActivity();
        View rootView = getRootView(inflater, container, savedInstanceState);
        initView(rootView, savedInstanceState);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.getInstance().d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.getInstance().d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.getInstance().d(TAG, "onDestroy");
    }

    /**
     * Description：初始化控件
     *
     * @param inflater           LayoutInflater
     * @param container          ViewGroup
     * @param savedInstanceState Bundle
     * @return 根布局
     */
    protected abstract View getRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * Description：初始化控件
     *
     * @param rootView           View
     * @param savedInstanceState Bundle
     */
    protected abstract void initView(View rootView, Bundle savedInstanceState);
}
