package com.jelly.baselibrary.thread;

import com.jelly.baselibrary.utils.LogUtil;

/**
 * 处理线程池异常
 */
public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "ThreadExceptionHandler";

    public ThreadExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LogUtil.getInstance().e(TAG, "Thread:" + t.getName() + "uncaughtException: " + e.toString());
    }
}
