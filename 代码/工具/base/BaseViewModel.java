package com.jelly.baselibrary.base;

import android.os.Bundle;

import androidx.lifecycle.AndroidViewModel;

import com.jelly.baselibrary.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：ViewModel的基类
 */
public abstract class BaseViewModel extends AndroidViewModel {
    private static String TAG = "BaseViewModel";
    // LiveData集合
    private List<BaseLiveData> mLiveDataList;

    public BaseViewModel() {
        super(null);
        TAG = this.getClass().getSimpleName();
        LogUtil.getInstance().d(TAG, "onCreate");
        this.mLiveDataList = new ArrayList<>();
    }

    protected <T> BaseLiveData<T> newLiveData() {
        BaseLiveData<T> liveData = new BaseLiveData<>();
        mLiveDataList.add(liveData);
        return liveData;
    }

    /**
     * Description：初始化数据
     *
     * @param bundle Bundle
     */
    protected abstract void initData(Bundle bundle);

    /**
     * Description：销毁数据
     */
    protected void destroyData() {
        for (BaseLiveData liveData : mLiveDataList) {
            liveData.removeObserver();
        }
        mLiveDataList.clear();
    }
}
