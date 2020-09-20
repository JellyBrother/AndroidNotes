package com.jelly.baselibrary.base;

import android.os.Bundle;

/**
 * Description：Activity的基类
 */
public abstract class BaseLifecycleActivity<T extends BaseViewModel> extends BaseActivity {
    // 当前Activity持有的ViewModel
    protected T mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mViewModel == null) {
            mViewModel = initViewModel();
        }
        mViewModel.initData(getIntent().getExtras());
        observerData();
    }

    @Override
    protected void onDestroy() {
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
