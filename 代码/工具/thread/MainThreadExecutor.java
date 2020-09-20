package com.jelly.baselibrary.thread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * Description：post 线程到主线程执行
 */
public class MainThreadExecutor implements Executor {
    private final Handler mHandler;

    public MainThreadExecutor() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void execute(Runnable runnable) {
        mHandler.post(runnable);
    }

}
