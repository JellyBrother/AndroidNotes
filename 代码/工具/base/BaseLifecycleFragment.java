package com.jelly.baselibrary.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * Description：Activity的基类
 */
public abstract class BaseLifecycleFragment<T extends BaseViewModel> extends BaseFragment {
    // 当前Activity持有的ViewModel
    protected T mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (mViewModel == null) {
            mViewModel = initViewModel();
        }
        mViewModel.initData(getArguments());
        observerData();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.destroyData();
    }

    /**
     * Description：初始化ViewModel
     *
     * @return ViewModel
     */
    protected abstract T initViewModel();

    /**
     * Description：数据操作
     */
    protected abstract void observerData();
}
