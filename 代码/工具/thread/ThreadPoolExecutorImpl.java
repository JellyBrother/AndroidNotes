package com.jelly.baselibrary.thread;

import android.os.Process;

import com.jelly.baselibrary.utils.LogUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定义线程池，UI线程池和后台线程池。
 */
public class ThreadPoolExecutorImpl {
    private static final String TAG = "ThreadPoolExecutorImpl";

    private static final int TYPE_UI_RELEVANT_THREAD = 1;
    private static final int TYPE_BACKGROUND_THREAD = 2;
    private static final int AIDL_THREAD_MAX_COUNT = 10;

    /**
     * Relevant UI、Logic process、I/O、etc
     */
    private final ThreadPoolExecutor mUiThreadPoolExecutor;

    /**
     * Network process、Cost more time、etc
     */
    private final ThreadPoolExecutor mBgThreadPoolExecutor;

    /**
     * AIDL跨进程使用,同时连接不能超过15个
     */
    private final ThreadPoolExecutor mAidlThreadPoolExecutor;

    private final MainThreadExecutor mMainThreadExecutor;


    public static ThreadPoolExecutorImpl getInstance() {
        return SingletonInstanceHolder.INSTANCE;
    }

    private ThreadPoolExecutorImpl() {
        mMainThreadExecutor = new MainThreadExecutor();

        int cpuCores = Runtime.getRuntime().availableProcessors();

        mUiThreadPoolExecutor = new ThreadPoolExecutor(
                cpuCores + 1,
                cpuCores + 1,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolFactory(TYPE_UI_RELEVANT_THREAD, Process.THREAD_PRIORITY_DISPLAY)
        );
        mUiThreadPoolExecutor.allowCoreThreadTimeOut(true);

        mBgThreadPoolExecutor = new ThreadPoolExecutor(
                cpuCores * 2 + 1,
                cpuCores * 2 + 1,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolFactory(TYPE_BACKGROUND_THREAD, Process.THREAD_PRIORITY_DEFAULT)
        );
        mBgThreadPoolExecutor.allowCoreThreadTimeOut(true);

        int aidlThread = (cpuCores * 2 + 1 > AIDL_THREAD_MAX_COUNT) ? AIDL_THREAD_MAX_COUNT : cpuCores * 2 + 1;
        mAidlThreadPoolExecutor = new ThreadPoolExecutor(
                aidlThread,
                aidlThread,
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolFactory(TYPE_BACKGROUND_THREAD, Process.THREAD_PRIORITY_DEFAULT)
        );
        mAidlThreadPoolExecutor.allowCoreThreadTimeOut(true);

    }

    public void executeUiTask(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mUiThreadPoolExecutor.execute(runnable);
    }


    public Future submitUiTask(Runnable runnable) {
        if (runnable == null) {
            return null;
        }
        return mUiThreadPoolExecutor.submit(runnable);
    }

    /**
     * Description: 带有返回值的异步任务
     *
     * @param <T>      返回值类型
     * @param callable
     * @return
     */
    public <T> Future<T> submitUiTask(Callable<T> callable) {
        if (callable == null) {
            return null;
        }
        return mUiThreadPoolExecutor.submit(callable);
    }

    public void executeBgTask(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mBgThreadPoolExecutor.execute(runnable);
    }


    public Future submitBgTask(Runnable runnable) {
        if (runnable == null) {
            return null;
        }
        return mBgThreadPoolExecutor.submit(runnable);
    }

    public Future submitBgTask(Callable runnable) {
        if (runnable == null) {
            return null;
        }
        return mBgThreadPoolExecutor.submit(runnable);
    }

    public void executeAidlTask(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mAidlThreadPoolExecutor.execute(runnable);
    }


    public Future submitAidlTask(Runnable runnable) {
        if (runnable == null) {
            return null;
        }
        return mAidlThreadPoolExecutor.submit(runnable);
    }

    /**
     * Description:通过handler将runnable post到主线程执行
     *
     * @param runnable
     */
    public void executeMainThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mMainThreadExecutor.execute(runnable);
    }

    /**
     * 打印线程池的参数信息
     */
    public void printThreadState() {
        LogUtil.getInstance().d(TAG, "-PriorityUiThreadPool, PoolCoreSize: " + mUiThreadPoolExecutor.getCorePoolSize()
                + ", ActiveThreadCount: " + mUiThreadPoolExecutor.getActiveCount()
                + ", CompletedTaskCount: " + mUiThreadPoolExecutor.getCompletedTaskCount()
                + ", CurPoolSize:" + mUiThreadPoolExecutor.getPoolSize()
                + ", ScheduledTaskCount: " + mUiThreadPoolExecutor.getTaskCount()
                + ", QueueSize: " + mUiThreadPoolExecutor.getQueue().size()
                + ", LargestPoolSize: " + mUiThreadPoolExecutor.getLargestPoolSize());

        LogUtil.getInstance().d(TAG, "-PriorityBkgThreadPool, PoolCoreSize: " + mBgThreadPoolExecutor.getCorePoolSize()
                + ", ActiveThreadCount: " + mBgThreadPoolExecutor.getActiveCount()
                + ", CompletedTaskCount: " + mBgThreadPoolExecutor.getCompletedTaskCount()
                + ", CurPoolSize:" + mBgThreadPoolExecutor.getPoolSize()
                + ", ScheduledTaskCount: " + mBgThreadPoolExecutor.getTaskCount()
                + ", QueueSize: " + mBgThreadPoolExecutor.getQueue().size()
                + ", LargestPoolSize: " + mBgThreadPoolExecutor.getLargestPoolSize());

    }

    private static class ThreadPoolFactory implements ThreadFactory {
        private final int mTaskType;

        private final int mThreadPriority;

        private AtomicInteger mThreadCount = new AtomicInteger(1);

        public ThreadPoolFactory(int taskType, int threadPriority) {
            mTaskType = taskType;
            mThreadPriority = threadPriority;
        }

        @Override
        public Thread newThread(final Runnable runnable) {
            Runnable wrapperRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Process.setThreadPriority(mThreadPriority);
                    } catch (IllegalArgumentException e) {
                        LogUtil.getInstance().e(TAG, "setThreadPriority exception" + e.toString());
                    } catch (SecurityException e) {
                        LogUtil.getInstance().e(TAG, "setThreadPriority exception" + e.toString());
                    } catch (Throwable throwable) {
                        LogUtil.getInstance().e(TAG, "setThreadPriority exception" + throwable.toString());
                    }

                    runnable.run();
                }
            };

            if (mTaskType == TYPE_UI_RELEVANT_THREAD) {
                Thread thread = new Thread(wrapperRunnable, "PriorityUiThreadPool #" + mThreadCount.getAndIncrement());
                thread.setUncaughtExceptionHandler(new ThreadExceptionHandler());
                return thread;
            } else if (mTaskType == TYPE_BACKGROUND_THREAD) {
                Thread thread = new Thread(wrapperRunnable, "PriorityBgThreadPool #" + mThreadCount.getAndIncrement());
                thread.setUncaughtExceptionHandler(new ThreadExceptionHandler());
                return thread;
            }
            return null;
        }
    }

    private static class SingletonInstanceHolder {
        private SingletonInstanceHolder() {
        }

        public static final ThreadPoolExecutorImpl INSTANCE = new ThreadPoolExecutorImpl();
    }

}