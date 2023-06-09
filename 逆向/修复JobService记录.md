# 修复JobService记录

## 问题

在Bugly平台有统计到 JobService相关的崩溃，有如下两个崩溃：

**崩溃一**

     Process: com.android.vending:instant_app_installer, PID: 12979
     java.lang.RuntimeException: An error occurred while executing doInBackground()
     at android.os.AsyncTask$AsyncFutureTask.done(AsyncTask.java:429)
     at java.util.concurrent.FutureTask.finishCompletion(FutureTask.java:383)
     at java.util.concurrent.FutureTask.setException(FutureTask.java:252)
     at java.util.concurrent.FutureTask.run(FutureTask.java:271)
     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
     at java.lang.Thread.run(Thread.java:929)
     Caused by: java.lang.SecurityException: Caller no longer running, last stopped +41ms because: finished start
     at android.os.Parcel.createException(Parcel.java:2091)
     at android.os.Parcel.readException(Parcel.java:2059)
     at android.os.Parcel.readException(Parcel.java:2007)
     at android.app.job.IJobCallback$Stub$Proxy.completeWork(IJobCallback.java:322)
     at android.app.job.JobParameters.completeWork(JobParameters.java:272)
     at cvo.b(PG:2)
     at cvk.doInBackground(PG:9)
     at android.os.AsyncTask$3.call(AsyncTask.java:389)
     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     ... 3 more

**崩溃二**

    Process: com.facebook.katana, PID: 23815
     java.lang.RuntimeException: java.lang.NullPointerException: throw with null exception
     at android.app.job.JobServiceEngine$JobHandler.handleMessage(JobServiceEngine.java:137)
     at android.os.Handler.dispatchMessage(Handler.java:110)
     at android.os.Looper.loop(Looper.java:219)
     at android.app.ActivityThread.main(ActivityThread.java:8676)
     at java.lang.reflect.Method.invoke(Native Method)
     at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:513)
     at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1109)
     Caused by: java.lang.NullPointerException: throw with null exception
     at X.8oH.A04(:49)
     at X.RnA.A02(:0)
     at com.facebook.common.jobscheduler.compat.JobServiceCompat.onStopJob(:8)
     at android.app.job.JobService$1.onStopJob(JobService.java:67)
     at android.app.job.JobServiceEngine$JobHandler.handleMessage(JobServiceEngine.java:133)
     ... 6 more

## JobService简介

_**JobService是Android L时候官方新增的组件，适用于需要特定条件才执行后台任务的场景。**_

_**由系统统一管理和调度，在特定场景下使用JobService更加灵活和省心，相当于是Service的加强或者优化**_

_**主要有添加任务和处理任务两个模块，**__**JobScheduler 负责添加任务，**_**JobService负责处理任务**

### JobService API

・onStartJob() Job开始时的回调，实现实际的工作逻辑。

注意，如果返回false的话，系统会自动结束本job。

・ jobFinished() Job执行完毕后，由App端自己调用，以通知JobScheduler已经完成了任务。

注意，该方法调用导致的Job结束并不会回调onStopJob(),只会回调onDestroy()。

・onStopJob() Job中止的时候回调。当JobScheduler发觉该Job条件不满足的时候，或者Job被抢占的时候强制回调该方法。

注意，如果想让这种意外中止的Job重新开始，复写该函数返回true。

另外还有父类Service的基础方法，可以覆写来实现一些辅助作用。

・onCreate() Service被初始化后的回调，可以在这里设置BroadcastReceiver或者ContentObserver等处理。

・onDestroy() Service被销毁前的回调。可以在这里注销BroadcastReceiver或者ContentObserver。

上面可以看出，JobService只是Job执行和中止时机的回调入口。

那如何将这个入口告诉系统，就需要用到JobScheduler了 。

### JobScheduler API

・schedule() 安排一个Job任务。

・enqueue() 安排一个Job任务，但是可以将一个任务排入队列。

・cancel() 取消一个执行ID的Job。

・cancelAll() 取消该app所有的注册到JobScheduler里的任务。

・getAllPendingJobs() 获取该app所有的注册到JobScheduler里未完成的任务列表。

・getPendingJob() 按照ID检索获得JobScheduler里未完成的该任务的JobInfo信息。

上面还提到需要创建JobInfo对象，实际要通过JobInfo.Builder类利用建造者模式创建出JobInfo对象。

### JobInfo.Builder API

我们选取几个代表性的API看看。

・Builder() JobInfo.Builder的内部类构造函数

注意，参数之一ID必须是APP UID内唯一的，如果APP和别的APP共用了UID,那么要防止该ID和别的APP里有冲突

・setOverrideDeadline() 设置job被立即执行的最大延迟期限

注意：即便其他条件没满足此期限到了也要立即执行

・setRequiresDeviceIdle() 是否需要在IDLE状态下运行该Job

・setRequiredNetworkType() 设置需要何种网络类型条件

至此，JobService，JobScheduler以及JobInfo三大块的API我们都已经有些了解了，那我们先写个简单的JobService跑起来试试。

## 问题分析

由于只有崩溃日志，没有复现步骤和界面，只能分析代码自己写示例代码复现。从以下几个角度分析问题

1.  自己写示例代码对比在真机上和在Gbox上代码调用流程是否一致。
    
2.  反编译查看对应应用堆栈代码的位置，看是否能看去问题。
    

#### JobService 简单示例代码

**JobScheduler** 在添加任务时有两种方式  **schedule** 和  **enqueue**

               val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val bundle =  PersistableBundle()
                bundle.putLong(SampleJobService.RUN_TIME,5000)
                val jobInfo: JobInfo = JobInfo.Builder(SampleJobService.JOB_ID_TWO, ComponentName(this, SampleJobService::class.java))
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setExtras(bundle)
                        .build()
                jobScheduler.schedule(jobInfo)

                val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val jobInfo: JobInfo = JobInfo.Builder(SampleJobService.JOB_ID_TWO, ComponentName(this, SampleJobService::class.java))
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .build()
                        
                 var taskId = enqueueIndex++
                 val intent = Intent("com.text.aaaa")
                 intent.putExtra("taskId",taskId)
                 intent.putExtra(SampleJobService.RUN_TIME,5000L)
                 val result=  jobScheduler.enqueue(jobInfo, JobWorkItem(intent)

**JobScheduler  取消任务的接口**

    val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    jobScheduler.cancel(SampleJobService.JOB_ID)

SampleJobService 实现

    class SampleJobService :JobService() {
        companion object{
            const val JOB_ID = 1
            const val TAG = "SampleJobService_client"
            const val RUN_TIME = "run_time"
        }
    
        private var jobMaps = hashMapOf<Int,MutableList<Job>>()
    
        override fun onCreate() {
            super.onCreate()
            Log.d(TAG,"onCreate $this")
    
        }
    
        override fun onDestroy() {
            super.onDestroy()
            Log.d(TAG,"onDestroy $this")
        }
    
        override fun onStartJob(params: JobParameters): Boolean {
            doWork(params)
            return true
        }
    
        
    
    
        private fun doWork(params: JobParameters) {
            Log.d(TAG,"执行 JOB ID: ${params.jobId}    $this ")
            val tempJobs = mutableListOf<Job>()
            var job = GlobalScope.launch(Dispatchers.IO) {
    
                var dequeueWork = params.dequeueWork()
                if (dequeueWork == null){
                    val runTime  = params.extras?.getLong(RUN_TIME, 0)
                    Log.d(TAG," 执行JOB ID: ${params.jobId}  dequeueWork is null runTime $runTime  $this")
                }else {
                   
                    while (dequeueWork != null) {
                       var taskId =  dequeueWork.intent.getIntExtra("taskId",-1)
                        val time = dequeueWork.intent.getLongExtra(RUN_TIME, 0)
                        params.completeWork(dequeueWork)
                        Log.d(TAG,"执行JOB ID: ${params.jobId}  任务ID：: $taskId    $this")
                        delay(time)
                        dequeueWork = params.dequeueWork()
                       // Log.d(TAG,"JOB_ID: ${params.jobId} next  dequeueWork $dequeueWork ")
                    }
                }
                val mutableLists = jobMaps.get(params.jobId)
                if (mutableLists != null && tempJobs.getOrNull(0) != null){
                    mutableLists.remove(tempJobs.get(0))
                }
                delay(1000)
                Log.d(TAG,"执行JOB ID: ${params.jobId}  完成")
                jobFinished(params, false)
            }
            var mutableList = jobMaps.get(params.jobId)
            if (mutableList == null){
                mutableList = mutableListOf()
                jobMaps.put(params.jobId,mutableList)
            }
            tempJobs.add(job)
            mutableList.add(job)
        }
    
        override fun onStopJob(params: JobParameters): Boolean {
            Log.d(TAG,"onStopJob ID：${params.jobId}   $this")
            cancelWork(params)
            return false // 系统会重新调度任务
        }
    
        private fun cancelWork(params: JobParameters) {
            var jobId = params.jobId
            var jobs = jobMaps.get(jobId)
            if (jobs != null){
                for (job in jobs){
                    if (job.isActive) {
                        job.cancel()
                    }
                }
            }
            jobMaps.remove(jobId)
        }
    
    }

真机调用两次 **enqueue  方法的打印**

    添加任务到 Job ID  2 任务ID:0
    onCreate 
    onStartJob JOB ID: 2   
    添加任务到 Job ID  2 任务ID:1
    执行JOB ID: 2  任务ID：: 0   
    执行JOB ID: 2  任务ID：: 1   
    onDestroy 
    执行JOB ID: 2  完成

Gbox中调用两次打印的日志：

    添加任务到 Job ID  2 任务ID:0
    onCreate 
    onStartJob JOB ID: 2   
     执行JOB ID: 2  dequeueWork is null runTime 0  
    添加任务到 Job ID  2 任务ID:1
    onStartJob JOB ID: 2  
    
    
    Process: com.vlite.unittest, PID: 17864
        java.lang.SecurityException: Caller no longer running, last stopped +5ms because: finished start
            at android.os.Parcel.createExceptionOrNull(Parcel.java:2373)
            at android.os.Parcel.createException(Parcel.java:2357)
            at android.os.Parcel.readException(Parcel.java:2340)
            at android.os.Parcel.readException(Parcel.java:2282)
            at android.app.job.IJobCallback$Stub$Proxy.completeWork(IJobCallback.java:322)
            at android.app.job.JobParameters.completeWork(JobParameters.java:311)
            at com.vlite.unittest.service.SampleJobService$doWork$job$1.invokeSuspend(SampleJobService.kt:54)
            at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
            at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
            at kotlinx.coroutines.internal.LimitedDispatcher.run(LimitedDispatcher.kt:42)
            at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:95)
            at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:570)
            at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:749)
            at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:677)
            at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:664)
        	Suppressed: kotlinx.coroutines.DiagnosticCoroutineContextException: [StandaloneCoroutine{Cancelling}@2b729aa, Dispatchers.IO]    

可以看出在Gbox中的JobService和真机的执行流程不一样，并且复现了Bugly的第一个异常

#### 分析崩溃一

既然已经复现了问题一，那么就可以结合示例代码分析问题了。

1.  首先得知道在什么情况下系统会抛出这个异常，第一反应就是去百度搜索一下这个异常出现的原因，但是这个异常却在百度上搜索不到。这就必须自己去分析这个异常出现的原因了
    

     在源码里面搜索关键字 **Caller no longer running**   找到抛出这个异常的地方，搜索到的代码如下：

      // JobServiceContext.java
      
      private boolean assertCallerLocked(JobCallback cb) {
            if (!verifyCallerLocked(cb)) {
                final long nowElapsed = sElapsedRealtimeClock.millis();
                if (!mPreviousJobHadSuccessfulFinish
                        && (nowElapsed - mLastUnsuccessfulFinishElapsed) < 15_000L) {
                    // Don't punish apps for race conditions
                    return false;
                }
                // It's been long enough that the app should really not be calling into JS for the
                // stopped job.
                StringBuilder sb = new StringBuilder(128);
                sb.append("Caller no longer running");
                if (cb.mStoppedReason != null) {
                    sb.append(", last stopped ");
                    TimeUtils.formatDuration(nowElapsed - cb.mStoppedTime, sb);
                    sb.append(" because: ");
                    sb.append(cb.mStoppedReason);
                }
                throw new SecurityException(sb.toString());
            }
            return true;
        }

可以看出当 verifyCallerLocked 方法返回 false 会导致崩溃，我们看一下 verifyCallerLocked 代码实现：

       // JobServiceContext.java
       
       private boolean verifyCallerLocked(JobCallback cb) {
            if (mRunningCallback != cb) {
                if (DEBUG) {
                    Slog.d(TAG, "Stale callback received, ignoring.");
                }
                return false;
            }
            return true;
        }

可以看出当 mRunningCallback 和 cb 不相等的时候会返回false. 再观察到异常 抛异常的地方会拼接：mStoppedReason ，再观察崩溃一的日志 ：

     Caller no longer running, last stopped +5ms because: finished start

在当前文件中 搜索：‘finished start’

      // JobServiceContext.java
      
    final class JobCallback extends IJobCallback.Stub {
     			 // 这个类里面的方法都是在客户端调用的
            public String mStoppedReason;
            public long mStoppedTime;
    
            @Override
            public void acknowledgeStartMessage(int jobId, boolean ongoing) {
            		// 注意看这个方法，
                doAcknowledgeStartMessage(this, jobId, ongoing);
            }
    
            @Override
            public void acknowledgeStopMessage(int jobId, boolean reschedule) {
                doAcknowledgeStopMessage(this, jobId, reschedule);
            }
    
            @Override
            public JobWorkItem dequeueWork(int jobId) {
                return doDequeueWork(this, jobId);
            }
    
            @Override
            public boolean completeWork(int jobId, int workId) {
                return doCompleteWork(this, jobId, workId);
            }
    
            @Override
            public void jobFinished(int jobId, boolean reschedule) {
                doJobFinished(this, jobId, reschedule);
            }
        }
    

     // JobServiceContext.java
     
    void doAcknowledgeStartMessage(JobCallback cb, int jobId, boolean ongoing) {
            doCallback(cb, ongoing, "finished start");
    }

可以看出调用完doAcknowledgeStartMessage后  mStoppedReason 会变成 ”finished start “ ，这个方法是从客户端调用过来的。我们来看下客户端在什么情况下会调用这个方法，下面是JobServiceEngine 类中的一段代码。

          // JobServiceEngine.java
          
          try {
               boolean workOngoing = JobServiceEngine.this.onStartJob(params);
                // 调用完 onStartJob 后就会调用这个方法
                ackStartMessage(params, workOngoing);
           } catch (Exception e) {
                 Log.e(TAG, "Error while executing job: " + params.getJobId());
                 throw new RuntimeException(e);
           }

          // JobServiceEngine.java
          
           private void ackStartMessage(JobParameters params, boolean workOngoing) {
                final IJobCallback callback = params.getCallback();
                final int jobId = params.getJobId();
                if (callback != null) {
                    try {
                    		// 这里就会调到服务端
                        callback.acknowledgeStartMessage(jobId, workOngoing);
                    } catch(RemoteException e) {
                        Log.e(TAG, "System unreachable for starting job.");
                    }
                } else {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "Attempting to ack a job that has already been processed.");
                    }
                }
            }

从上面代码可以看出 系统调用完 onStartJob  方法后就会调用  acknowledgeStartMessage 方法，这个系统调用的正常流程，不应该会报错才对，所以我们继续分析 acknowledgeStartMessage 后续调用：

        // JobServiceEngine.java
        acknowledgeStartMessage 方法最后会调用到当前方法
        void doCallbackLocked(boolean reschedule, String reason) {
            if (DEBUG) {
                Slog.d(TAG, "doCallback of : " + mRunningJob
                        + " v:" + VERB_STRINGS[mVerb]);
            }
            removeOpTimeOutLocked();
    
            if (mVerb == VERB_STARTING) {
            		// 第一次调用会走这个分支
                handleStartedLocked(reschedule);
            } else if (mVerb == VERB_EXECUTING ||
                    mVerb == VERB_STOPPING) {
                // 第二次调用就会走到这个分支
                handleFinishedLocked(reschedule, reason);
            } else {
                if (DEBUG) {
                    Slog.d(TAG, "Unrecognised callback: " + mRunningJob);
                }
            }
        }
    

我们分别来看下handleStartedLocked 和 handleFinishedLocked 方法

        // JobServiceEngine.java
       private void handleStartedLocked(boolean workOngoing) {
            switch (mVerb) {
                case VERB_STARTING:
                		// 第一次调用 会走到这个分支，把  mVerb 赋值为 VERB_EXECUTING
                    mVerb = VERB_EXECUTING;
                    if (!workOngoing) {
                        // Job is finished already so fast-forward to handleFinished.
                        handleFinishedLocked(false, "onStartJob returned false");
                        return;
                    }
                    if (mCancelled) {
                        if (DEBUG) {
                            Slog.d(TAG, "Job cancelled while waiting for onStartJob to complete.");
                        }
                        // Cancelled *while* waiting for acknowledgeStartMessage from client.
                        handleCancelLocked(null);
                        return;
                    }
                    scheduleOpTimeOutLocked();
                    break;
                default:
                    Slog.e(TAG, "Handling started job but job wasn't starting! Was "
                            + VERB_STRINGS[mVerb] + ".");
                    return;
            }
        }

       // JobServiceEngine.java
       
      private void handleFinishedLocked(boolean reschedule, String reason) {
            switch (mVerb) {
                case VERB_EXECUTING:
                case VERB_STOPPING:
                		// 第二次调用会走到这里，这个方法会把  mRunningCallback 设置为NULL
                    closeAndCleanupJobLocked(reschedule, reason);
                    break;
                default:
                    Slog.e(TAG, "Got an execution complete message for a job that wasn't being" +
                            "executed. Was " + VERB_STRINGS[mVerb] + ".");
            }
        }

从上面的分析已经可以很清晰地看出，在客户端连续调用两次   acknowledgeStartMessage  方法后，再去调用  params.completeWork 会报出第一个崩溃。

问题会产生的原因已经分析清楚了，那么在什么情况下会调用两次 acknowledgeStartMessage 方法呢？ 这个方法我们没有主动调用过，都是系统调用的。 这个问题的答应就需要在我们的代码里去查找了。

#### 分析代理 ProxyJobService

我们代理的 ProxyJobService 也是继承的 JobService 那么在执行完  ProxyJobService 的 onStartJob 会调用一次  acknowledgeStartMessage 。而被代理的 JobService 执行完 onStartJob 后执行的 acknowledgeStartMessage 我们也会代理调用 ProxyJobService 的 acknowledgeStartMessage 方法，这种情况下我们就会调用两次 acknowledgeStartMessage ，**最终导致问题的产生**。

#### 初步修改

被代理的JobService 调用 acknowledgeStartMessage 方法时不再调用  ProxyJobService 的 acknowledgeStartMessage 方法。

修改完成后我们再来测试，测试日志如下：

    SampleJobService_client: 添加任务到 Job ID  2 任务ID:31
    SampleJobService_client: onCreate 
    SampleJobService_client: onStartJob JOB ID: 2   
    SampleJobService_client: 执行JOB ID: 2  任务ID：: 31   
    SampleJobService_client: onStopJob ID：2 
    SampleJobService_client: onDestroy 

通过测试可以发现，崩溃一已经不会再出现了。不过当前这种修改方式并不合理，应该是 ProxyJobService 执行完 onStartJob 不调用 acknowledgeStartMessage  方法， 被代理的JobService在调用该方法的时候需要穿透过去。

#### 最终的修改方案

JobService 本质是通过普通Service实现的。我们可以不用系统的 JobService 而是自定义 JobService ，把 JobService 代码完全复制，但是在执行完 onStartJob 不调用  acknowledgeStartMessage ，然后代理类 ProxyJobService 继承自定义的 JobService

## 问题二

修复完第一个问题后，发现连续执行 **enqueue** 方法发现在GBox上的表现和真机的表现还是不一样

真机上连续点击三次 enqueue 执行的日志

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/G1wvqrRGx9EQqako/img/b4f089ec-d04f-4b4d-8007-ce8b5b4640c2.png)

// GBox上连续点击三次 enqueue 执行的日志

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/G1wvqrRGx9EQqako/img/0427c00b-98a8-4227-b26a-b58ae9cd869a.png)

可以发现在真机上没有调用 ID为0的任务未执行完成时，添加新的任务时并不会重新执行 **onStartJob** 方法，也不会调用 **onStopJob** 方法，可以看出Gbox和真机的调用流程有明显的差别。第二个崩溃刚好就是在**onStopJob** 触发的。

#### 问题分析

在什么情况下，前一个任务还未执行完成时，添加任务会调用**onStopJob** 并且重新调用 **onStartJob** 方法呢？

JobScheduler的 enqueue 方法，最终会调用到 JobSchedulerService的 scheduleAsPackage 方法。

               // 找到正在运行的Job
               final JobStatus toCancel = mJobs.getJobByUidAndJobId(uId, job.getId());
    
                if (work != null && toCancel != null) {
                    // Fast path: we are adding work to an existing job, and the JobInfo is not
                    // changing.  We can just directly enqueue this work in to the job.
                    if (toCancel.getJob().equals(job)) {
    										// 如果JobInfo相同，把当前任务添加到这个job中，并返回
                        toCancel.enqueueWorkLocked(work);
    
                        // If any of work item is enqueued when the source is in the foreground,
                        // exempt the entire job.
                        toCancel.maybeAddForegroundExemption(mIsUidActivePredicate);
    
                        return JobScheduler.RESULT_SUCCESS;
                    }
                }
    
                JobStatus jobStatus = JobStatus.createFromJobInfo(job, uId, packageName, userId, tag);
    
                // Return failure early if expedited job quota used up.
                if (jobStatus.isRequestedExpeditedJob()
                        && !mQuotaController.isWithinEJQuotaLocked(jobStatus)) {
                    return JobScheduler.RESULT_FAILURE;
                }
    
                // Give exemption if the source is in the foreground just now.
                // Note if it's a sync job, this method is called on the handler so it's not exactly
                // the state when requestSync() was called, but that should be fine because of the
                // 1 minute foreground grace period.
                jobStatus.maybeAddForegroundExemption(mIsUidActivePredicate);
    
                if (DEBUG) Slog.d(TAG, "SCHEDULE: " + jobStatus.toShortString());
                // Jobs on behalf of others don't apply to the per-app job cap
                if (packageName == null) {
                    if (mJobs.countJobsForUid(uId) > MAX_JOBS_PER_APP) {
                        Slog.w(TAG, "Too many jobs for uid " + uId);
                        throw new IllegalStateException("Apps may not schedule more than "
                                    + MAX_JOBS_PER_APP + " distinct jobs");
                    }
                }
    
                // This may throw a SecurityException.
                jobStatus.prepareLocked();
    
                if (toCancel != null) {
                	//如果有正在运行的Job，并且与添加的JobInfo不相同，取消真正运行的Job
                    // Implicitly replaces the existing job record with the new instance
                    cancelJobImplLocked(toCancel, jobStatus, JobParameters.STOP_REASON_CANCELLED_BY_APP,
                            JobParameters.INTERNAL_STOP_REASON_CANCELED, "job rescheduled by app");
                } else {
                    startTrackingJobLocked(jobStatus, null);
                }
    

通过源码可以看出当第二次添加任务时，JobInfo 不相同时，会取消当前真正运行的Job。

分析到这里就更加疑惑了，示例代码里面明明每次 enqueue 时，JobInfo都是使用同一个，为什么在Gbox中还是会触发取消Job分支？ 

最后通过调试 service端发现，在  JobInfo 的 networkRequest 字段的networkCapabilities 字段对象中包含uid 信息。由于系统执行Job时创建的JobInfo 使用的是Gbox的uid，添加时JobInfo  使用的是模拟的uid，这两个uid不一样导致每次  enqueue 都会取消正在运行的Job.

#### 修复

在调用到 JobSchedulerService 前，修改 JobInfo中的uid为 Gbox的uid。

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.S ) {
                //  NetworkRequest  字段  networkCapabilities  类型 NetworkCapabilities  方法  setSingleUid 方法
                NetworkRequest requiredNetwork = newJobInfo.getRequiredNetwork();
                NetworkCapabilities capabilities = Ref_NetworkRequest.networkCapabilities.get(requiredNetwork);
                Ref_NetworkCapabilities.setSingleUid.invoke(capabilities,HostContext.getUid());
            }