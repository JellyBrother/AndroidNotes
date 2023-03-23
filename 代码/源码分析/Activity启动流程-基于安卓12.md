# Activity启动流程-基于安卓12

# /home/data/aosp/android-11.0.0\_r46/frameworks/base/core/java/android/app/Activity.java

Activity.startActivity--Activity#startActivityForResult--Instrumentation.execStartActivity--

    public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
                @Nullable Bundle options) {
            if (mParent == null) {
                options = transferSpringboardActivityOptions(options);
                Instrumentation.ActivityResult ar =
                // 通过Instrumentation启动
                    mInstrumentation.execStartActivity(
                        this, mMainThread.getApplicationThread(), mToken, this,
                        intent, requestCode, options);
                if (ar != null) {
                    mMainThread.sendActivityResult(
                        mToken, mEmbeddedID, requestCode, ar.getResultCode(),
                        ar.getResultData());
                }
                if (requestCode >= 0) {
                    // If this start is requesting a result, we can avoid making
                    // the activity visible until the result is received.  Setting
                    // this code during onCreate(Bundle savedInstanceState) or onResume() will keep the
                    // activity hidden during this time, to avoid flickering.
                    // This can only be done when a result is requested because
                    // that guarantees we will get information back when the
                    // activity is finished, no matter what happens to it.
                    mStartedActivity = true;
                }
    
                cancelInputsAndStartExitTransition(options);
                // TODO Consider clearing/flushing other event sources and events for child windows.
            } else {
                if (options != null) {
                    mParent.startActivityFromChild(this, intent, requestCode, options);
                } else {
                    // Note we want to go through this method for compatibility with
                    // existing applications that may have overridden it.
                    mParent.startActivityFromChild(this, intent, requestCode);
                }
            }
        }

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/Instrumentation.java

Instrumentation.execStartActivity--ActivityTaskManager.getService().startActivity--ActivityTaskManagerService.startActivity--ActivityTaskManagerService.startActivityAsUser--

    @UnsupportedAppUsage
        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {
            IApplicationThread whoThread = (IApplicationThread) contextThread;
            Uri referrer = target != null ? target.onProvideReferrer() : null;
            if (referrer != null) {
                intent.putExtra(Intent.EXTRA_REFERRER, referrer);
            }
            if (mActivityMonitors != null) {
                synchronized (mSync) {
                    final int N = mActivityMonitors.size();
                    for (int i=0; i<N; i++) {
                        final ActivityMonitor am = mActivityMonitors.get(i);
                        ActivityResult result = null;
                        if (am.ignoreMatchingSpecificIntents()) {
                            if (options == null) {
                                options = ActivityOptions.makeBasic().toBundle();
                            }
                            result = am.onStartActivity(who, intent, options);
                        }
                        if (result != null) {
                            am.mHits++;
                            return result;
                        } else if (am.match(who, null, intent)) {
                            am.mHits++;
                            if (am.isBlocking()) {
                                return requestCode >= 0 ? am.getResult() : null;
                            }
                            break;
                        }
                    }
                }
            }
            try {
                intent.migrateExtraStreamToClipData(who);
                intent.prepareToLeaveProcess(who);
                // 通过ActivityTaskManagerService的startActivity方法来启动
                int result = ActivityTaskManager.getService().startActivity(whoThread,
                        who.getOpPackageName(), who.getAttributionTag(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()), token,
                        target != null ? target.mEmbeddedID : null, requestCode, 0, null, options);
                checkStartActivityResult(result, intent);
            } catch (RemoteException e) {
                throw new RuntimeException("Failure from system", e);
            }
            return null;
        }

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/ActivityTaskManager.java

        /**
         * @hide
         */
        @UnsupportedAppUsage
        public static IActivityManager getService() {
            return IActivityManagerSingleton.get();
        }
    
        private static IActivityTaskManager getTaskService() {
            return ActivityTaskManager.getService();
        }
    
        @UnsupportedAppUsage
        private static final Singleton<IActivityManager> IActivityManagerSingleton =
                new Singleton<IActivityManager>() {
                    @Override
                    protected IActivityManager create() {
                        final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                        // 拿到ActivityTaskManagerService
                        final IActivityManager am = IActivityManager.Stub.asInterface(b);
                        return am;
                    }
                };

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/services/core/java/com/android/server/wm/ActivityTaskManagerService.java

\--ActivityTaskManagerService.startActivityAsUser--ActivityStartController.obtainStarter--ActivityStarter.execute--

        @Override
        public final int startActivity(IApplicationThread caller, String callingPackage,
                String callingFeatureId, Intent intent, String resolvedType, IBinder resultTo,
                String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo,
                Bundle bOptions) {
            return startActivityAsUser(caller, callingPackage, callingFeatureId, intent, resolvedType,
                    resultTo, resultWho, requestCode, startFlags, profilerInfo, bOptions,
                    UserHandle.getCallingUserId());
        }

       private int startActivityAsUser(IApplicationThread caller, String callingPackage,
                @Nullable String callingFeatureId, Intent intent, String resolvedType,
                IBinder resultTo, String resultWho, int requestCode, int startFlags,
                ProfilerInfo profilerInfo, Bundle bOptions, int userId, boolean validateIncomingUser) {
            assertPackageMatchesCallingUid(callingPackage);
            enforceNotIsolatedCaller("startActivityAsUser");
    
            userId = getActivityStartController().checkTargetUser(userId, validateIncomingUser,
                    Binder.getCallingPid(), Binder.getCallingUid(), "startActivityAsUser");
    
            // TODO: Switch to user app stacks here. 
            // 获取ActivityStartController，通过ActivityStartController获取到ActivityStarter
            // 再执行ActivityStarter的execute方法
            return getActivityStartController().obtainStarter(intent, "startActivityAsUser")
                    .setCaller(caller)
                    .setCallingPackage(callingPackage)
                    .setCallingFeatureId(callingFeatureId)
                    .setResolvedType(resolvedType)
                    .setResultTo(resultTo)
                    .setResultWho(resultWho)
                    .setRequestCode(requestCode)
                    .setStartFlags(startFlags)
                    .setProfilerInfo(profilerInfo)
                    .setActivityOptions(bOptions)
                    .setUserId(userId)
                    .execute();
    
        }

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/services/core/java/com/android/server/wm/ActivityStartController.java

        ActivityStarter obtainStarter(Intent intent, String reason) {
            return mFactory.obtain().setIntent(intent).setReason(reason);
        }

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/services/core/java/com/android/server/wm/ActivityStarter.java

\--ActivityStarter.execute--ActivityStarter.executeRequest--ActivityStarter.startActivityUnchecked--ActivityStarter.startActivityInner--

    /**
         * Resolve necessary information according the request parameters provided earlier, and execute
         * the request which begin the journey of starting an activity.
         * @return The starter result.
         */
        int execute() {
            try {
                // Refuse possible leaked file descriptors
                if (mRequest.intent != null && mRequest.intent.hasFileDescriptors()) {
                    throw new IllegalArgumentException("File descriptors passed in Intent");
                }
    
                final LaunchingState launchingState;
                synchronized (mService.mGlobalLock) {
                    final ActivityRecord caller = ActivityRecord.forTokenLocked(mRequest.resultTo);
                    final int callingUid = mRequest.realCallingUid == Request.DEFAULT_REAL_CALLING_UID
                            ?  Binder.getCallingUid() : mRequest.realCallingUid;
                    launchingState = mSupervisor.getActivityMetricsLogger().notifyActivityLaunching(
                            mRequest.intent, caller, callingUid);
                }
    
                // If the caller hasn't already resolved the activity, we're willing
                // to do so here. If the caller is already holding the WM lock here,
                // and we need to check dynamic Uri permissions, then we're forced
                // to assume those permissions are denied to avoid deadlocking.
                if (mRequest.activityInfo == null) {
                    mRequest.resolveActivity(mSupervisor);
                }
    
                // Add checkpoint for this shutdown or reboot attempt, so we can record the original
                // intent action and package name.
                if (mRequest.intent != null) {
                    String intentAction = mRequest.intent.getAction();
                    String callingPackage = mRequest.callingPackage;
                    if (intentAction != null && callingPackage != null
                            && (Intent.ACTION_REQUEST_SHUTDOWN.equals(intentAction)
                                    || Intent.ACTION_SHUTDOWN.equals(intentAction)
                                    || Intent.ACTION_REBOOT.equals(intentAction))) {
                        ShutdownCheckPoints.recordCheckPoint(intentAction, callingPackage, null);
                    }
                }
    
                int res;
                synchronized (mService.mGlobalLock) {
                    final boolean globalConfigWillChange = mRequest.globalConfig != null
                            && mService.getGlobalConfiguration().diff(mRequest.globalConfig) != 0;
                    final Task rootTask = mRootWindowContainer.getTopDisplayFocusedRootTask();
                    if (rootTask != null) {
                        rootTask.mConfigWillChange = globalConfigWillChange;
                    }
                    ProtoLog.v(WM_DEBUG_CONFIGURATION, "Starting activity when config "
                            + "will change = %b", globalConfigWillChange);
    
                    final long origId = Binder.clearCallingIdentity();
    
                    res = resolveToHeavyWeightSwitcherIfNeeded();
                    if (res != START_SUCCESS) {
                        return res;
                    }
                    // 加锁执行请求
                    res = executeRequest(mRequest);
    
                    Binder.restoreCallingIdentity(origId);
    
                    if (globalConfigWillChange) {
                        // If the caller also wants to switch to a new configuration, do so now.
                        // This allows a clean switch, as we are waiting for the current activity
                        // to pause (so we will not destroy it), and have not yet started the
                        // next activity.
                        mService.mAmInternal.enforceCallingPermission(
                                android.Manifest.permission.CHANGE_CONFIGURATION,
                                "updateConfiguration()");
                        if (rootTask != null) {
                            rootTask.mConfigWillChange = false;
                        }
                        ProtoLog.v(WM_DEBUG_CONFIGURATION,
                                    "Updating to new configuration after starting activity.");
    
                        mService.updateConfigurationLocked(mRequest.globalConfig, null, false);
                    }
    
                    // The original options may have additional info about metrics. The mOptions is not
                    // used here because it may be cleared in setTargetRootTaskIfNeeded.
                    final ActivityOptions originalOptions = mRequest.activityOptions != null
                            ? mRequest.activityOptions.getOriginalOptions() : null;
                    // If the new record is the one that started, a new activity has created.
                    final boolean newActivityCreated = mStartActivity == mLastStartActivityRecord;
                    // Notify ActivityMetricsLogger that the activity has launched.
                    // ActivityMetricsLogger will then wait for the windows to be drawn and populate
                    // WaitResult.
                    mSupervisor.getActivityMetricsLogger().notifyActivityLaunched(launchingState, res,
                            newActivityCreated, mLastStartActivityRecord, originalOptions);
                    if (mRequest.waitResult != null) {
                        mRequest.waitResult.result = res;
                        res = waitResultIfNeeded(mRequest.waitResult, mLastStartActivityRecord,
                                launchingState);
                    }
                    return getExternalResult(res);
                }
            } finally {
                onExecutionComplete();
            }
        }

     /**
         * Executing activity start request and starts the journey of starting an activity. Here
         * begins with performing several preliminary checks. The normally activity launch flow will
         * go through {@link #startActivityUnchecked} to {@link #startActivityInner}.
         */
        private int executeRequest(Request request) {
            if (TextUtils.isEmpty(request.reason)) {
                throw new IllegalArgumentException("Need to specify a reason.");
            }
            mLastStartReason = request.reason;
            mLastStartActivityTimeMs = System.currentTimeMillis();
            mLastStartActivityRecord = null;
    
            final IApplicationThread caller = request.caller;
            Intent intent = request.intent;
            NeededUriGrants intentGrants = request.intentGrants;
            String resolvedType = request.resolvedType;
            ActivityInfo aInfo = request.activityInfo;
            ResolveInfo rInfo = request.resolveInfo;
            final IVoiceInteractionSession voiceSession = request.voiceSession;
            final IBinder resultTo = request.resultTo;
            String resultWho = request.resultWho;
            int requestCode = request.requestCode;
            int callingPid = request.callingPid;
            int callingUid = request.callingUid;
            String callingPackage = request.callingPackage;
            String callingFeatureId = request.callingFeatureId;
            final int realCallingPid = request.realCallingPid;
            final int realCallingUid = request.realCallingUid;
            final int startFlags = request.startFlags;
            final SafeActivityOptions options = request.activityOptions;
            Task inTask = request.inTask;
            TaskFragment inTaskFragment = request.inTaskFragment;
    
            int err = ActivityManager.START_SUCCESS;
            // Pull the optional Ephemeral Installer-only bundle out of the options early.
            final Bundle verificationBundle =
                    options != null ? options.popAppVerificationBundle() : null;
    
            WindowProcessController callerApp = null;
            if (caller != null) {
                callerApp = mService.getProcessController(caller);
                if (callerApp != null) {
                    callingPid = callerApp.getPid();
                    callingUid = callerApp.mInfo.uid;
                } else {
                    Slog.w(TAG, "Unable to find app for caller " + caller + " (pid=" + callingPid
                            + ") when starting: " + intent.toString());
                    err = START_PERMISSION_DENIED;
                }
            }
    
            final int userId = aInfo != null && aInfo.applicationInfo != null
                    ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0;
            if (err == ActivityManager.START_SUCCESS) {
                Slog.i(TAG, "START u" + userId + " {" + intent.toShortString(true, true, true, false)
                        + "} from uid " + callingUid);
            }
    
            ActivityRecord sourceRecord = null;
            ActivityRecord resultRecord = null;
            if (resultTo != null) {
                sourceRecord = ActivityRecord.isInAnyTask(resultTo);
                if (DEBUG_RESULTS) {
                    Slog.v(TAG_RESULTS, "Will send result to " + resultTo + " " + sourceRecord);
                }
                if (sourceRecord != null) {
                    if (requestCode >= 0 && !sourceRecord.finishing) {
                        resultRecord = sourceRecord;
                    }
                }
            }
    
            final int launchFlags = intent.getFlags();
            if ((launchFlags & Intent.FLAG_ACTIVITY_FORWARD_RESULT) != 0 && sourceRecord != null) {
                // Transfer the result target from the source activity to the new one being started,
                // including any failures.
                if (requestCode >= 0) {
                    SafeActivityOptions.abort(options);
                    return ActivityManager.START_FORWARD_AND_REQUEST_CONFLICT;
                }
                resultRecord = sourceRecord.resultTo;
                if (resultRecord != null && !resultRecord.isInRootTaskLocked()) {
                    resultRecord = null;
                }
                resultWho = sourceRecord.resultWho;
                requestCode = sourceRecord.requestCode;
                sourceRecord.resultTo = null;
                if (resultRecord != null) {
                    resultRecord.removeResultsLocked(sourceRecord, resultWho, requestCode);
                }
                if (sourceRecord.launchedFromUid == callingUid) {
                    // The new activity is being launched from the same uid as the previous activity
                    // in the flow, and asking to forward its result back to the previous.  In this
                    // case the activity is serving as a trampoline between the two, so we also want
                    // to update its launchedFromPackage to be the same as the previous activity.
                    // Note that this is safe, since we know these two packages come from the same
                    // uid; the caller could just as well have supplied that same package name itself
                    // . This specifially deals with the case of an intent picker/chooser being
                    // launched in the app flow to redirect to an activity picked by the user, where
                    // we want the final activity to consider it to have been launched by the
                    // previous app activity.
                    callingPackage = sourceRecord.launchedFromPackage;
                    callingFeatureId = sourceRecord.launchedFromFeatureId;
                }
            }
    
            if (err == ActivityManager.START_SUCCESS && intent.getComponent() == null) {
                // We couldn't find a class that can handle the given Intent.
                // That's the end of that!
                err = ActivityManager.START_INTENT_NOT_RESOLVED;
            }
    
            if (err == ActivityManager.START_SUCCESS && aInfo == null) {
                // We couldn't find the specific class specified in the Intent.
                // Also the end of the line.
                err = ActivityManager.START_CLASS_NOT_FOUND;
            }
    
            if (err == ActivityManager.START_SUCCESS && sourceRecord != null
                    && sourceRecord.getTask().voiceSession != null) {
                // If this activity is being launched as part of a voice session, we need to ensure
                // that it is safe to do so.  If the upcoming activity will also be part of the voice
                // session, we can only launch it if it has explicitly said it supports the VOICE
                // category, or it is a part of the calling app.
                if ((launchFlags & FLAG_ACTIVITY_NEW_TASK) == 0
                        && sourceRecord.info.applicationInfo.uid != aInfo.applicationInfo.uid) {
                    try {
                        intent.addCategory(Intent.CATEGORY_VOICE);
                        if (!mService.getPackageManager().activitySupportsIntent(
                                intent.getComponent(), intent, resolvedType)) {
                            Slog.w(TAG, "Activity being started in current voice task does not support "
                                    + "voice: " + intent);
                            err = ActivityManager.START_NOT_VOICE_COMPATIBLE;
                        }
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Failure checking voice capabilities", e);
                        err = ActivityManager.START_NOT_VOICE_COMPATIBLE;
                    }
                }
            }
    
            if (err == ActivityManager.START_SUCCESS && voiceSession != null) {
                // If the caller is starting a new voice session, just make sure the target
                // is actually allowing it to run this way.
                try {
                    if (!mService.getPackageManager().activitySupportsIntent(intent.getComponent(),
                            intent, resolvedType)) {
                        Slog.w(TAG,
                                "Activity being started in new voice task does not support: " + intent);
                        err = ActivityManager.START_NOT_VOICE_COMPATIBLE;
                    }
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failure checking voice capabilities", e);
                    err = ActivityManager.START_NOT_VOICE_COMPATIBLE;
                }
            }
    
            final Task resultRootTask = resultRecord == null
                    ? null : resultRecord.getRootTask();
    
            if (err != START_SUCCESS) {
                if (resultRecord != null) {
                    resultRecord.sendResult(INVALID_UID, resultWho, requestCode, RESULT_CANCELED,
                            null /* data */, null /* dataGrants */);
                }
                SafeActivityOptions.abort(options);
                return err;
            }
    
            boolean abort = !mSupervisor.checkStartAnyActivityPermission(intent, aInfo, resultWho,
                    requestCode, callingPid, callingUid, callingPackage, callingFeatureId,
                    request.ignoreTargetSecurity, inTask != null, callerApp, resultRecord,
                    resultRootTask);
            abort |= !mService.mIntentFirewall.checkStartActivity(intent, callingUid,
                    callingPid, resolvedType, aInfo.applicationInfo);
            abort |= !mService.getPermissionPolicyInternal().checkStartActivity(intent, callingUid,
                    callingPackage);
    
            boolean restrictedBgActivity = false;
            if (!abort) {
                try {
                    Trace.traceBegin(Trace.TRACE_TAG_WINDOW_MANAGER,
                            "shouldAbortBackgroundActivityStart");
                    restrictedBgActivity = shouldAbortBackgroundActivityStart(callingUid,
                            callingPid, callingPackage, realCallingUid, realCallingPid, callerApp,
                            request.originatingPendingIntent, request.allowBackgroundActivityStart,
                            intent);
                } finally {
                    Trace.traceEnd(Trace.TRACE_TAG_WINDOW_MANAGER);
                }
            }
    
            // Merge the two options bundles, while realCallerOptions takes precedence.
            ActivityOptions checkedOptions = options != null
                    ? options.getOptions(intent, aInfo, callerApp, mSupervisor) : null;
            if (request.allowPendingRemoteAnimationRegistryLookup) {
                checkedOptions = mService.getActivityStartController()
                        .getPendingRemoteAnimationRegistry()
                        .overrideOptionsIfNeeded(callingPackage, checkedOptions);
            }
            if (mService.mController != null) {
                try {
                    // The Intent we give to the watcher has the extra data stripped off, since it
                    // can contain private information.
                    Intent watchIntent = intent.cloneFilter();
                    abort |= !mService.mController.activityStarting(watchIntent,
                            aInfo.applicationInfo.packageName);
                } catch (RemoteException e) {
                    mService.mController = null;
                }
            }
    
            mInterceptor.setStates(userId, realCallingPid, realCallingUid, startFlags, callingPackage,
                    callingFeatureId);
            if (mInterceptor.intercept(intent, rInfo, aInfo, resolvedType, inTask, callingPid,
                    callingUid, checkedOptions)) {
                // activity start was intercepted, e.g. because the target user is currently in quiet
                // mode (turn off work) or the target application is suspended
                intent = mInterceptor.mIntent;
                rInfo = mInterceptor.mRInfo;
                aInfo = mInterceptor.mAInfo;
                resolvedType = mInterceptor.mResolvedType;
                inTask = mInterceptor.mInTask;
                callingPid = mInterceptor.mCallingPid;
                callingUid = mInterceptor.mCallingUid;
                checkedOptions = mInterceptor.mActivityOptions;
    
                // The interception target shouldn't get any permission grants
                // intended for the original destination
                intentGrants = null;
            }
    
            if (abort) {
                if (resultRecord != null) {
                    resultRecord.sendResult(INVALID_UID, resultWho, requestCode, RESULT_CANCELED,
                            null /* data */, null /* dataGrants */);
                }
                // We pretend to the caller that it was really started, but they will just get a
                // cancel result.
                ActivityOptions.abort(checkedOptions);
                return START_ABORTED;
            }
    
            // If permissions need a review before any of the app components can run, we
            // launch the review activity and pass a pending intent to start the activity
            // we are to launching now after the review is completed.
            if (aInfo != null) {
                if (mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(
                        aInfo.packageName, userId)) {
                    final IIntentSender target = mService.getIntentSenderLocked(
                            ActivityManager.INTENT_SENDER_ACTIVITY, callingPackage, callingFeatureId,
                            callingUid, userId, null, null, 0, new Intent[]{intent},
                            new String[]{resolvedType}, PendingIntent.FLAG_CANCEL_CURRENT
                                    | PendingIntent.FLAG_ONE_SHOT, null);
    
                    Intent newIntent = new Intent(Intent.ACTION_REVIEW_PERMISSIONS);
    
                    int flags = intent.getFlags();
                    flags |= Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
    
                    /*
                     * Prevent reuse of review activity: Each app needs their own review activity. By
                     * default activities launched with NEW_TASK or NEW_DOCUMENT try to reuse activities
                     * with the same launch parameters (extras are ignored). Hence to avoid possible
                     * reuse force a new activity via the MULTIPLE_TASK flag.
                     *
                     * Activities that are not launched with NEW_TASK or NEW_DOCUMENT are not re-used,
                     * hence no need to add the flag in this case.
                     */
                    if ((flags & (FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_NEW_DOCUMENT)) != 0) {
                        flags |= Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
                    }
                    newIntent.setFlags(flags);
    
                    newIntent.putExtra(Intent.EXTRA_PACKAGE_NAME, aInfo.packageName);
                    newIntent.putExtra(Intent.EXTRA_INTENT, new IntentSender(target));
                    if (resultRecord != null) {
                        newIntent.putExtra(Intent.EXTRA_RESULT_NEEDED, true);
                    }
                    intent = newIntent;
    
                    // The permissions review target shouldn't get any permission
                    // grants intended for the original destination
                    intentGrants = null;
    
                    resolvedType = null;
                    callingUid = realCallingUid;
                    callingPid = realCallingPid;
    
                    rInfo = mSupervisor.resolveIntent(intent, resolvedType, userId, 0,
                            computeResolveFilterUid(
                                    callingUid, realCallingUid, request.filterCallingUid));
                    aInfo = mSupervisor.resolveActivity(intent, rInfo, startFlags,
                            null /*profilerInfo*/);
    
                    if (DEBUG_PERMISSIONS_REVIEW) {
                        final Task focusedRootTask =
                                mRootWindowContainer.getTopDisplayFocusedRootTask();
                        Slog.i(TAG, "START u" + userId + " {" + intent.toShortString(true, true,
                                true, false) + "} from uid " + callingUid + " on display "
                                + (focusedRootTask == null ? DEFAULT_DISPLAY
                                        : focusedRootTask.getDisplayId()));
                    }
                }
            }
    
            // If we have an ephemeral app, abort the process of launching the resolved intent.
            // Instead, launch the ephemeral installer. Once the installer is finished, it
            // starts either the intent we resolved here [on install error] or the ephemeral
            // app [on install success].
            if (rInfo != null && rInfo.auxiliaryInfo != null) {
                intent = createLaunchIntent(rInfo.auxiliaryInfo, request.ephemeralIntent,
                        callingPackage, callingFeatureId, verificationBundle, resolvedType, userId);
                resolvedType = null;
                callingUid = realCallingUid;
                callingPid = realCallingPid;
    
                // The ephemeral installer shouldn't get any permission grants
                // intended for the original destination
                intentGrants = null;
    
                aInfo = mSupervisor.resolveActivity(intent, rInfo, startFlags, null /*profilerInfo*/);
            }
            // TODO (b/187680964) Correcting the caller/pid/uid when start activity from shortcut
            // Pending intent launched from systemui also depends on caller app
            if (callerApp == null && realCallingPid > 0) {
                final WindowProcessController wpc = mService.mProcessMap.getProcess(realCallingPid);
                if (wpc != null) {
                    callerApp = wpc;
                }
            }
            final ActivityRecord r = new ActivityRecord.Builder(mService)
                    .setCaller(callerApp)
                    .setLaunchedFromPid(callingPid)
                    .setLaunchedFromUid(callingUid)
                    .setLaunchedFromPackage(callingPackage)
                    .setLaunchedFromFeature(callingFeatureId)
                    .setIntent(intent)
                    .setResolvedType(resolvedType)
                    .setActivityInfo(aInfo)
                    .setConfiguration(mService.getGlobalConfiguration())
                    .setResultTo(resultRecord)
                    .setResultWho(resultWho)
                    .setRequestCode(requestCode)
                    .setComponentSpecified(request.componentSpecified)
                    .setRootVoiceInteraction(voiceSession != null)
                    .setActivityOptions(checkedOptions)
                    .setSourceRecord(sourceRecord)
                    .build();
    
            mLastStartActivityRecord = r;
    
            if (r.appTimeTracker == null && sourceRecord != null) {
                // If the caller didn't specify an explicit time tracker, we want to continue
                // tracking under any it has.
                r.appTimeTracker = sourceRecord.appTimeTracker;
            }
    
            // Only allow app switching to be resumed if activity is not a restricted background
            // activity and target app is not home process, otherwise any background activity
            // started in background task can stop home button protection mode.
            // As the targeted app is not a home process and we don't need to wait for the 2nd
            // activity to be started to resume app switching, we can just enable app switching
            // directly.
            WindowProcessController homeProcess = mService.mHomeProcess;
            boolean isHomeProcess = homeProcess != null
                    && aInfo.applicationInfo.uid == homeProcess.mUid;
            if (!restrictedBgActivity && !isHomeProcess) {
                mService.resumeAppSwitches();
            }
    				// 全部校验后，不再校验启动Activity
            mLastStartActivityResult = startActivityUnchecked(r, sourceRecord, voiceSession,
                    request.voiceInteractor, startFlags, true /* doResume */, checkedOptions,
                    inTask, inTaskFragment, restrictedBgActivity, intentGrants);
    
            if (request.outActivity != null) {
                request.outActivity[0] = mLastStartActivityRecord;
            }
    
            return mLastStartActivityResult;
        }

    /**
         * Start an activity while most of preliminary checks has been done and caller has been
         * confirmed that holds necessary permissions to do so.
         * Here also ensures that the starting activity is removed if the start wasn't successful.
         */
        private int startActivityUnchecked(final ActivityRecord r, ActivityRecord sourceRecord,
                IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
                int startFlags, boolean doResume, ActivityOptions options, Task inTask,
                TaskFragment inTaskFragment, boolean restrictedBgActivity,
                NeededUriGrants intentGrants) {
            int result = START_CANCELED;
            boolean startResultSuccessful = false;
            final Task startedActivityRootTask;
    
            // Create a transition now to record the original intent of actions taken within
            // startActivityInner. Otherwise, logic in startActivityInner could start a different
            // transition based on a sub-action.
            // Only do the create here (and defer requestStart) since startActivityInner might abort.
            final TransitionController transitionController = r.mTransitionController;
            Transition newTransition = (!transitionController.isCollecting()
                    && transitionController.getTransitionPlayer() != null)
                    ? transitionController.createTransition(TRANSIT_OPEN) : null;
            RemoteTransition remoteTransition = r.takeRemoteTransition();
            if (newTransition != null && remoteTransition != null) {
                newTransition.setRemoteTransition(remoteTransition);
            }
            transitionController.collect(r);
            final boolean isTransient = r.getOptions() != null && r.getOptions().getTransientLaunch();
            try {
                mService.deferWindowLayout();
                Trace.traceBegin(Trace.TRACE_TAG_WINDOW_MANAGER, "startActivityInner");
                result = startActivityInner(r, sourceRecord, voiceSession, voiceInteractor,
                        startFlags, doResume, options, inTask, inTaskFragment, restrictedBgActivity,
                        intentGrants);
                startResultSuccessful = ActivityManager.isStartResultSuccessful(result);
                final boolean taskAlwaysOnTop = options != null && options.getTaskAlwaysOnTop();
                // Apply setAlwaysOnTop when starting an Activity is successful regardless of creating
                // a new Activity or recycling the existing Activity.
                if (taskAlwaysOnTop && startResultSuccessful) {
                    final Task targetRootTask =
                            mTargetRootTask != null ? mTargetRootTask : mTargetTask.getRootTask();
                    targetRootTask.setAlwaysOnTop(true);
                }
            } finally {
                Trace.traceEnd(Trace.TRACE_TAG_WINDOW_MANAGER);
                startedActivityRootTask = handleStartResult(r, result);
                mService.continueWindowLayout();
                mSupervisor.mUserLeaving = false;
    
                // Transition housekeeping
                if (!startResultSuccessful) {
                    if (newTransition != null) {
                        newTransition.abort();
                    }
                } else {
                    if (!mAvoidMoveToFront && mDoResume
                            && mRootWindowContainer.hasVisibleWindowAboveButDoesNotOwnNotificationShade(
                                r.launchedFromUid)) {
                        // If the UID launching the activity has a visible window on top of the
                        // notification shade and it's launching an activity that's going to be at the
                        // front, we should move the shade out of the way so the user can see it.
                        // We want to avoid the case where the activity is launched on top of a
                        // background task which is not moved to the front.
                        StatusBarManagerInternal statusBar = mService.getStatusBarManagerInternal();
                        if (statusBar != null) {
                            // This results in a async call since the interface is one-way
                            statusBar.collapsePanels();
                        }
                    }
                    final boolean started = result == START_SUCCESS || result == START_TASK_TO_FRONT;
                    if (started) {
                        // The activity is started new rather than just brought forward, so record
                        // it as an existence change.
                        transitionController.collectExistenceChange(r);
                    } else if (result == START_DELIVERED_TO_TOP && newTransition != null) {
                        // We just delivered to top, so there isn't an actual transition here
                        newTransition.abort();
                        newTransition = null;
                    }
                    if (isTransient) {
                        // `r` isn't guaranteed to be the actual relevant activity, so we must wait
                        // until after we launched to identify the relevant activity.
                        transitionController.setTransientLaunch(mLastStartActivityRecord);
                    }
                    if (newTransition != null) {
                        transitionController.requestStartTransition(newTransition,
                                mTargetTask, remoteTransition);
                    } else if (started) {
                        // Make the collecting transition wait until this request is ready.
                        transitionController.setReady(r, false);
                    }
                }
            }
    
            postStartActivityProcessing(r, result, startedActivityRootTask);
    
            return result;
        }

\--ActivityStarter.startActivityInner--RootWindowContainer.resumeFocusedTasksTopActivities--

    /**
         * Start an activity and determine if the activity should be adding to the top of an existing
         * task or delivered new intent to an existing activity. Also manipulating the activity task
         * onto requested or valid root-task/display.
         *
         * Note: This method should only be called from {@link #startActivityUnchecked}.
         */
        // TODO(b/152429287): Make it easier to exercise code paths through startActivityInner
        @VisibleForTesting
        int startActivityInner(final ActivityRecord r, ActivityRecord sourceRecord,
                IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
                int startFlags, boolean doResume, ActivityOptions options, Task inTask,
                TaskFragment inTaskFragment, boolean restrictedBgActivity,
                NeededUriGrants intentGrants) {
            setInitialState(r, options, inTask, inTaskFragment, doResume, startFlags, sourceRecord,
                    voiceSession, voiceInteractor, restrictedBgActivity);
    
            computeLaunchingTaskFlags();
    
            computeSourceRootTask();
    
            mIntent.setFlags(mLaunchFlags);
    
            // Get top task at beginning because the order may be changed when reusing existing task.
            final Task prevTopTask = mPreferredTaskDisplayArea.getFocusedRootTask();
            final Task reusedTask = getReusableTask();
    
            // If requested, freeze the task list
            if (mOptions != null && mOptions.freezeRecentTasksReordering()
                    && mSupervisor.mRecentTasks.isCallerRecents(r.launchedFromUid)
                    && !mSupervisor.mRecentTasks.isFreezeTaskListReorderingSet()) {
                mFrozeTaskList = true;
                mSupervisor.mRecentTasks.setFreezeTaskListReordering();
            }
    
            // Compute if there is an existing task that should be used for.
            final Task targetTask = reusedTask != null ? reusedTask : computeTargetTask();
            final boolean newTask = targetTask == null;
            mTargetTask = targetTask;
    
            computeLaunchParams(r, sourceRecord, targetTask);
    
            // Check if starting activity on given task or on a new task is allowed.
            int startResult = isAllowedToStart(r, newTask, targetTask);
            if (startResult != START_SUCCESS) {
                return startResult;
            }
    
            final ActivityRecord targetTaskTop = newTask
                    ? null : targetTask.getTopNonFinishingActivity();
            if (targetTaskTop != null) {
                // Recycle the target task for this launch.
                startResult = recycleTask(targetTask, targetTaskTop, reusedTask, intentGrants);
                if (startResult != START_SUCCESS) {
                    return startResult;
                }
            } else {
                mAddingToTask = true;
            }
    
            // If the activity being launched is the same as the one currently at the top, then
            // we need to check if it should only be launched once.
            final Task topRootTask = mPreferredTaskDisplayArea.getFocusedRootTask();
            if (topRootTask != null) {
                startResult = deliverToCurrentTopIfNeeded(topRootTask, intentGrants);
                if (startResult != START_SUCCESS) {
                    return startResult;
                }
            }
    
            if (mTargetRootTask == null) {
                mTargetRootTask = getLaunchRootTask(mStartActivity, mLaunchFlags, targetTask, mOptions);
            }
            if (newTask) {
                final Task taskToAffiliate = (mLaunchTaskBehind && mSourceRecord != null)
                        ? mSourceRecord.getTask() : null;
                setNewTask(taskToAffiliate);
            } else if (mAddingToTask) {
                addOrReparentStartingActivity(targetTask, "adding to task");
            }
    
            if (!mAvoidMoveToFront && mDoResume) {
                mTargetRootTask.getRootTask().moveToFront("reuseOrNewTask", targetTask);
                if (!mTargetRootTask.isTopRootTaskInDisplayArea() && mService.mInternal.isDreaming()) {
                    // Launching underneath dream activity (fullscreen, always-on-top). Run the launch-
                    // -behind transition so the Activity gets created and starts in visible state.
                    mLaunchTaskBehind = true;
                    r.mLaunchTaskBehind = true;
                }
            }
    
            mService.mUgmInternal.grantUriPermissionUncheckedFromIntent(intentGrants,
                    mStartActivity.getUriPermissionsLocked());
            if (mStartActivity.resultTo != null && mStartActivity.resultTo.info != null) {
                // we need to resolve resultTo to a uid as grantImplicitAccess deals explicitly in UIDs
                final PackageManagerInternal pmInternal =
                        mService.getPackageManagerInternalLocked();
                final int resultToUid = pmInternal.getPackageUid(
                        mStartActivity.resultTo.info.packageName, 0 /* flags */,
                        mStartActivity.mUserId);
                pmInternal.grantImplicitAccess(mStartActivity.mUserId, mIntent,
                        UserHandle.getAppId(mStartActivity.info.applicationInfo.uid) /*recipient*/,
                        resultToUid /*visible*/, true /*direct*/);
            }
            final Task startedTask = mStartActivity.getTask();
            if (newTask) {
                EventLogTags.writeWmCreateTask(mStartActivity.mUserId, startedTask.mTaskId);
            }
            mStartActivity.logStartActivity(EventLogTags.WM_CREATE_ACTIVITY, startedTask);
    
            mStartActivity.getTaskFragment().clearLastPausedActivity();
    
            mRootWindowContainer.startPowerModeLaunchIfNeeded(
                    false /* forceSend */, mStartActivity);
    
            final boolean isTaskSwitch = startedTask != prevTopTask && !startedTask.isEmbedded();
            mTargetRootTask.startActivityLocked(mStartActivity,
                    topRootTask != null ? topRootTask.getTopNonFinishingActivity() : null, newTask,
                    isTaskSwitch, mOptions, sourceRecord);
            if (mDoResume) {
                final ActivityRecord topTaskActivity = startedTask.topRunningActivityLocked();
                if (!mTargetRootTask.isTopActivityFocusable()
                        || (topTaskActivity != null && topTaskActivity.isTaskOverlay()
                        && mStartActivity != topTaskActivity)) {
                    // If the activity is not focusable, we can't resume it, but still would like to
                    // make sure it becomes visible as it starts (this will also trigger entry
                    // animation). An example of this are PIP activities.
                    // Also, we don't want to resume activities in a task that currently has an overlay
                    // as the starting activity just needs to be in the visible paused state until the
                    // over is removed.
                    // Passing {@code null} as the start parameter ensures all activities are made
                    // visible.
                    mTargetRootTask.ensureActivitiesVisible(null /* starting */,
                            0 /* configChanges */, !PRESERVE_WINDOWS);
                    // Go ahead and tell window manager to execute app transition for this activity
                    // since the app transition will not be triggered through the resume channel.
                    mTargetRootTask.mDisplayContent.executeAppTransition();
                } else {
                    // If the target root-task was not previously focusable (previous top running
                    // activity on that root-task was not visible) then any prior calls to move the
                    // root-task to the will not update the focused root-task.  If starting the new
                    // activity now allows the task root-task to be focusable, then ensure that we
                    // now update the focused root-task accordingly.
                    if (mTargetRootTask.isTopActivityFocusable()
                            && !mRootWindowContainer.isTopDisplayFocusedRootTask(mTargetRootTask)) {
                        mTargetRootTask.moveToFront("startActivityInner");
                    }
                    // RootWindowContainer从栈内恢复
                    mRootWindowContainer.resumeFocusedTasksTopActivities(
                            mTargetRootTask, mStartActivity, mOptions, mTransientLaunch);
                }
            }
            mRootWindowContainer.updateUserRootTask(mStartActivity.mUserId, mTargetRootTask);
    
            // Update the recent tasks list immediately when the activity starts
            mSupervisor.mRecentTasks.add(startedTask);
            mSupervisor.handleNonResizableTaskIfNeeded(startedTask,
                    mPreferredWindowingMode, mPreferredTaskDisplayArea, mTargetRootTask);
    
            return START_SUCCESS;
        }

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/services/core/java/com/android/server/wm/RootWindowContainer.java

\--RootWindowContainer.resumeFocusedTasksTopActivities--Task.resumeTopActivityUncheckedLocked--

     boolean resumeFocusedTasksTopActivities(
                Task targetRootTask, ActivityRecord target, ActivityOptions targetOptions,
                boolean deferPause) {
            if (!mTaskSupervisor.readyToResume()) {
                return false;
            }
    
            boolean result = false;
            if (targetRootTask != null && (targetRootTask.isTopRootTaskInDisplayArea()
                    || getTopDisplayFocusedRootTask() == targetRootTask)) {
                    
                result = targetRootTask.resumeTopActivityUncheckedLocked(target, targetOptions,
                        deferPause);
            }
    
            for (int displayNdx = getChildCount() - 1; displayNdx >= 0; --displayNdx) {
                final DisplayContent display = getChildAt(displayNdx);
                final boolean curResult = result;
                boolean[] resumedOnDisplay = new boolean[1];
                display.forAllRootTasks(rootTask -> {
                    final ActivityRecord topRunningActivity = rootTask.topRunningActivity();
                    if (!rootTask.isFocusableAndVisible() || topRunningActivity == null) {
                        return;
                    }
                    if (rootTask == targetRootTask) {
                        // Simply update the result for targetRootTask because the targetRootTask
                        // had already resumed in above. We don't want to resume it again,
                        // especially in some cases, it would cause a second launch failure
                        // if app process was dead.
                        resumedOnDisplay[0] |= curResult;
                        return;
                    }
                    if (rootTask.getDisplayArea().isTopRootTask(rootTask)
                            && topRunningActivity.isState(RESUMED)) {
                        // Kick off any lingering app transitions form the MoveTaskToFront
                        // operation, but only consider the top task and root-task on that
                        // display.
                        rootTask.executeAppTransition(targetOptions);
                    } else {
                        resumedOnDisplay[0] |= topRunningActivity.makeActiveIfNeeded(target);
                    }
                });
                result |= resumedOnDisplay[0];
                if (!resumedOnDisplay[0]) {
                    // In cases when there are no valid activities (e.g. device just booted or launcher
                    // crashed) it's possible that nothing was resumed on a display. Requesting resume
                    // of top activity in focused root task explicitly will make sure that at least home
                    // activity is started and resumed, and no recursion occurs.
                    final Task focusedRoot = display.getFocusedRootTask();
                    if (focusedRoot != null) {
                        result |= focusedRoot.resumeTopActivityUncheckedLocked(target, targetOptions);
                    } else if (targetRootTask == null) {
                        result |= resumeHomeActivity(null /* prev */, "no-focusable-task",
                                display.getDefaultTaskDisplayArea());
                    }
                }
            }
    
            return result;
        }

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/services/core/java/com/android/server/wm/Task.java

\--Task.resumeTopActivityUncheckedLocked--Task.resumeTopActivityInnerLocked--Task.resumeNextFocusableActivityWhenRootTaskIsEmpty--RootWindowContainer.resumeFocusedTasksTopActivities-或者-RootWindowContainer.resumeHomeActivity--

     @GuardedBy("mService")
        boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options,
                boolean deferPause) {
            if (mInResumeTopActivity) {
                // Don't even start recursing.
                return false;
            }
    
            boolean someActivityResumed = false;
            try {
                // Protect against recursion.
                mInResumeTopActivity = true;
    
                if (isLeafTask()) {
                    if (isFocusableAndVisible()) {
                        someActivityResumed = resumeTopActivityInnerLocked(prev, options, deferPause);
                    }
                } else {
                    int idx = mChildren.size() - 1;
                    while (idx >= 0) {
                        final Task child = (Task) getChildAt(idx--);
                        if (!child.isTopActivityFocusable()) {
                            continue;
                        }
                        if (child.getVisibility(null /* starting */)
                                != TASK_FRAGMENT_VISIBILITY_VISIBLE) {
                            break;
                        }
    
                        someActivityResumed |= child.resumeTopActivityUncheckedLocked(prev, options,
                                deferPause);
                        // Doing so in order to prevent IndexOOB since hierarchy might changes while
                        // resuming activities, for example dismissing split-screen while starting
                        // non-resizeable activity.
                        if (idx >= mChildren.size()) {
                            idx = mChildren.size() - 1;
                        }
                    }
                }
    
                // When resuming the top activity, it may be necessary to pause the top activity (for
                // example, returning to the lock screen. We suppress the normal pause logic in
                // {@link #resumeTopActivityUncheckedLocked}, since the top activity is resumed at the
                // end. We call the {@link ActivityTaskSupervisor#checkReadyForSleepLocked} again here
                // to ensure any necessary pause logic occurs. In the case where the Activity will be
                // shown regardless of the lock screen, the call to
                // {@link ActivityTaskSupervisor#checkReadyForSleepLocked} is skipped.
                final ActivityRecord next = topRunningActivity(true /* focusableOnly */);
                if (next == null || !next.canTurnScreenOn()) {
                    checkReadyForSleep();
                }
            } finally {
                mInResumeTopActivity = false;
            }
    
            return someActivityResumed;
        }

        @GuardedBy("mService")
        private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options,
                boolean deferPause) {
            if (!mAtmService.isBooting() && !mAtmService.isBooted()) {
                // Not ready yet!
                return false;
            }
    
            final ActivityRecord topActivity = topRunningActivity(true /* focusableOnly */);
            if (topActivity == null) {
                // There are no activities left in this task, let's look somewhere else.
                return resumeNextFocusableActivityWhenRootTaskIsEmpty(prev, options);
            }
    
            final boolean[] resumed = new boolean[1];
            final TaskFragment topFragment = topActivity.getTaskFragment();
            resumed[0] = topFragment.resumeTopActivity(prev, options, deferPause);
            forAllLeafTaskFragments(f -> {
                if (topFragment == f) {
                    return;
                }
                if (!f.canBeResumed(null /* starting */)) {
                    return;
                }
                resumed[0] |= f.resumeTopActivity(prev, options, deferPause);
            }, true);
            return resumed[0];
        }

        private boolean resumeNextFocusableActivityWhenRootTaskIsEmpty(ActivityRecord prev,
                ActivityOptions options) {
            final String reason = "noMoreActivities";
    
            if (!isActivityTypeHome()) {
                final Task nextFocusedTask = adjustFocusToNextFocusableTask(reason);
                if (nextFocusedTask != null) {
                    // Try to move focus to the next visible root task with a running activity if this
                    // root task is not covering the entire screen or is on a secondary display with
                    // no home root task.
                    return mRootWindowContainer.resumeFocusedTasksTopActivities(nextFocusedTask,
                            prev, null /* targetOptions */);
                }
            }
    
            // If the current root task is a root home task, or if focus didn't switch to a different
            // root task - just start up the Launcher...
            ActivityOptions.abort(options);
            ProtoLog.d(WM_DEBUG_STATES, "resumeNextFocusableActivityWhenRootTaskIsEmpty: %s, "
                    + "go home", reason);
            return mRootWindowContainer.resumeHomeActivity(prev, reason, getDisplayArea());
        }

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/services/core/java/com/android/server/wm/RootWindowContainer.java

\-RootWindowContainer.resumeHomeActivity-RootWindowContainer.startHomeOnTaskDisplayArea--

ActivityTaskManagerService.getActivityStartController--ActivityStartController.startHomeActivity--

        boolean resumeHomeActivity(ActivityRecord prev, String reason,
                TaskDisplayArea taskDisplayArea) {
            if (!mService.isBooting() && !mService.isBooted()) {
                // Not ready yet!
                return false;
            }
    
            if (taskDisplayArea == null) {
                taskDisplayArea = getDefaultTaskDisplayArea();
            }
    
            final ActivityRecord r = taskDisplayArea.getHomeActivity();
            final String myReason = reason + " resumeHomeActivity";
    
            // Only resume home activity if isn't finishing.
            if (r != null && !r.finishing) {
                r.moveFocusableActivityToTop(myReason);
                return resumeFocusedTasksTopActivities(r.getRootTask(), prev, null);
            }
            return startHomeOnTaskDisplayArea(mCurrentUser, myReason, taskDisplayArea,
                    false /* allowInstrumenting */, false /* fromHomeKey */);
        }

      boolean startHomeOnTaskDisplayArea(int userId, String reason, TaskDisplayArea taskDisplayArea,
                boolean allowInstrumenting, boolean fromHomeKey) {
            // Fallback to top focused display area if the provided one is invalid.
            if (taskDisplayArea == null) {
                final Task rootTask = getTopDisplayFocusedRootTask();
                taskDisplayArea = rootTask != null ? rootTask.getDisplayArea()
                        : getDefaultTaskDisplayArea();
            }
    
            Intent homeIntent = null;
            ActivityInfo aInfo = null;
            if (taskDisplayArea == getDefaultTaskDisplayArea()) {
                homeIntent = mService.getHomeIntent();
                aInfo = resolveHomeActivity(userId, homeIntent);
            } else if (shouldPlaceSecondaryHomeOnDisplayArea(taskDisplayArea)) {
                Pair<ActivityInfo, Intent> info = resolveSecondaryHomeActivity(userId, taskDisplayArea);
                aInfo = info.first;
                homeIntent = info.second;
            }
            if (aInfo == null || homeIntent == null) {
                return false;
            }
    
            if (!canStartHomeOnDisplayArea(aInfo, taskDisplayArea, allowInstrumenting)) {
                return false;
            }
    
            // Updates the home component of the intent.
            homeIntent.setComponent(new ComponentName(aInfo.applicationInfo.packageName, aInfo.name));
            homeIntent.setFlags(homeIntent.getFlags() | FLAG_ACTIVITY_NEW_TASK);
            // Updates the extra information of the intent.
            if (fromHomeKey) {
                homeIntent.putExtra(WindowManagerPolicy.EXTRA_FROM_HOME_KEY, true);
                if (mWindowManager.getRecentsAnimationController() != null) {
                    mWindowManager.getRecentsAnimationController().cancelAnimationForHomeStart();
                }
            }
            homeIntent.putExtra(WindowManagerPolicy.EXTRA_START_REASON, reason);
    
            // Update the reason for ANR debugging to verify if the user activity is the one that
            // actually launched.
            final String myReason = reason + ":" + userId + ":" + UserHandle.getUserId(
                    aInfo.applicationInfo.uid) + ":" + taskDisplayArea.getDisplayId();
            mService.getActivityStartController().startHomeActivity(homeIntent, aInfo, myReason,
                    taskDisplayArea);
            return true;
        }

\--ActivityStartController.startHomeActivity--

    void startHomeActivity(Intent intent, ActivityInfo aInfo, String reason,
                TaskDisplayArea taskDisplayArea) {
            final ActivityOptions options = ActivityOptions.makeBasic();
            options.setLaunchWindowingMode(WINDOWING_MODE_FULLSCREEN);
            if (!ActivityRecord.isResolverActivity(aInfo.name)) {
                // The resolver activity shouldn't be put in root home task because when the
                // foreground is standard type activity, the resolver activity should be put on the
                // top of current foreground instead of bring root home task to front.
                options.setLaunchActivityType(ACTIVITY_TYPE_HOME);
            }
            final int displayId = taskDisplayArea.getDisplayId();
            options.setLaunchDisplayId(displayId);
            options.setLaunchTaskDisplayArea(taskDisplayArea.mRemoteToken
                    .toWindowContainerToken());
    
            // The home activity will be started later, defer resuming to avoid unnecessary operations
            // (e.g. start home recursively) when creating root home task.
            mSupervisor.beginDeferResume();
            final Task rootHomeTask;
            try {
                // Make sure root home task exists on display area.
                rootHomeTask = taskDisplayArea.getOrCreateRootHomeTask(ON_TOP);
            } finally {
                mSupervisor.endDeferResume();
            }
    
            mLastHomeActivityStartResult = obtainStarter(intent, "startHomeActivity: " + reason)
                    .setOutActivity(tmpOutRecord)
                    .setCallingUid(0)
                    .setActivityInfo(aInfo)
                    .setActivityOptions(options.toBundle())
                    .execute();
            mLastHomeActivityStartRecord = tmpOutRecord[0];
            if (rootHomeTask.mInResumeTopActivity) {
                // If we are in resume section already, home activity will be initialized, but not
                // resumed (to avoid recursive resume) and will stay that way until something pokes it
                // again. We need to schedule another resume.
                mSupervisor.scheduleResumeTopActivities();
            }
        }

        final void scheduleResumeTopActivities() {
            if (!mHandler.hasMessages(RESUME_TOP_ACTIVITY_MSG)) {
                mHandler.sendEmptyMessage(RESUME_TOP_ACTIVITY_MSG);
            }
        }

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/services/core/java/com/android/server/wm/ActivityTaskSupervisor.java

\--RootWindowContainer.attachApplication--RootWindowContainer.startActivityForAttachedApplicationIfNeeded-或者-ActivityTaskSupervisor.startSpecificActivity--ActivityTaskSupervisor.realStartActivityLocked--WindowProcessController.onStartActivity--

ActivityTaskSupervisor.realStartActivityLocked跨进程启动

        private boolean startActivityForAttachedApplicationIfNeeded(ActivityRecord r,
                WindowProcessController app, ActivityRecord top) {
            if (r.finishing || !r.showToCurrentUser() || !r.visibleIgnoringKeyguard || r.app != null
                    || app.mUid != r.info.applicationInfo.uid || !app.mName.equals(r.processName)) {
                return false;
            }
    
            try {
                if (mTaskSupervisor.realStartActivityLocked(r, app,
                        top == r && r.isFocusable() /*andResume*/, true /*checkConfig*/)) {
                    mTmpBoolean = true;
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception in new application when starting activity "
                        + top.intent.getComponent().flattenToShortString(), e);
                mTmpRemoteException = e;
                return true;
            }
            return false;
        }

        void startSpecificActivity(ActivityRecord r, boolean andResume, boolean checkConfig) {
            // Is this activity's application already running?
            final WindowProcessController wpc =
                    mService.getProcessController(r.processName, r.info.applicationInfo.uid);
    
            boolean knownToBeDead = false;
            if (wpc != null && wpc.hasThread()) {
                try {
                    realStartActivityLocked(r, wpc, andResume, checkConfig);
                    return;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception when starting activity "
                            + r.intent.getComponent().flattenToShortString(), e);
                }
    
                // If a dead object exception was thrown -- fall through to
                // restart the application.
                knownToBeDead = true;
            }
    
            r.notifyUnknownVisibilityLaunchedForKeyguardTransition();
    
            final boolean isTop = andResume && r.isTopRunningActivity();
            mService.startProcessAsync(r, knownToBeDead, isTop, isTop ? "top-activity" : "activity");
        }

    boolean realStartActivityLocked(ActivityRecord r, WindowProcessController proc,
                boolean andResume, boolean checkConfig) throws RemoteException {
                    
    
            if (!mRootWindowContainer.allPausedActivitiesComplete()) {
                // While there are activities pausing we skipping starting any new activities until
                // pauses are complete. NOTE: that we also do this for activities that are starting in
                // the paused state because they will first be resumed then paused on the client side.
                ProtoLog.v(WM_DEBUG_STATES,
                        "realStartActivityLocked: Skipping start of r=%s some activities pausing...",
                        r);
                return false;
            }
    
            final Task task = r.getTask();
            final Task rootTask = task.getRootTask();
    
            beginDeferResume();
            // The LaunchActivityItem also contains process configuration, so the configuration change
            // from WindowProcessController#setProcess can be deferred. The major reason is that if
            // the activity has FixedRotationAdjustments, it needs to be applied with configuration.
            // In general, this reduces a binder transaction if process configuration is changed.
            proc.pauseConfigurationDispatch();
    
            try {
                r.startFreezingScreenLocked(proc, 0);
    
                // schedule launch ticks to collect information about slow apps.
                r.startLaunchTickingLocked();
    
                r.setProcess(proc);
    
                // Ensure activity is allowed to be resumed after process has set.
                if (andResume && !r.canResumeByCompat()) {
                    andResume = false;
                }
    
                r.notifyUnknownVisibilityLaunchedForKeyguardTransition();
    
                // Have the window manager re-evaluate the orientation of the screen based on the new
                // activity order.  Note that as a result of this, it can call back into the activity
                // manager with a new orientation.  We don't care about that, because the activity is
                // not currently running so we are just restarting it anyway.
                if (checkConfig) {
                    // Deferring resume here because we're going to launch new activity shortly.
                    // We don't want to perform a redundant launch of the same record while ensuring
                    // configurations and trying to resume top activity of focused root task.
                    mRootWindowContainer.ensureVisibilityAndConfig(r, r.getDisplayId(),
                            false /* markFrozenIfConfigChanged */, true /* deferResume */);
                }
    
                if (mKeyguardController.checkKeyguardVisibility(r) && r.allowMoveToFront()) {
                    // We only set the visibility to true if the activity is not being launched in
                    // background, and is allowed to be visible based on keyguard state. This avoids
                    // setting this into motion in window manager that is later cancelled due to later
                    // calls to ensure visible activities that set visibility back to false.
                    r.setVisibility(true);
                }
    
                final int applicationInfoUid =
                        (r.info.applicationInfo != null) ? r.info.applicationInfo.uid : -1;
                if ((r.mUserId != proc.mUserId) || (r.info.applicationInfo.uid != applicationInfoUid)) {
                    Slog.wtf(TAG,
                            "User ID for activity changing for " + r
                                    + " appInfo.uid=" + r.info.applicationInfo.uid
                                    + " info.ai.uid=" + applicationInfoUid
                                    + " old=" + r.app + " new=" + proc);
                }
    
                // Send the controller to client if the process is the first time to launch activity.
                // So the client can save binder transactions of getting the controller from activity
                // task manager service.
                final IActivityClientController activityClientController =
                        proc.hasEverLaunchedActivity() ? null : mService.mActivityClientController;
                r.launchCount++;
                r.lastLaunchTime = SystemClock.uptimeMillis();
                proc.setLastActivityLaunchTime(r.lastLaunchTime);
    
                if (DEBUG_ALL) Slog.v(TAG, "Launching: " + r);
    
                final LockTaskController lockTaskController = mService.getLockTaskController();
                if (task.mLockTaskAuth == LOCK_TASK_AUTH_LAUNCHABLE
                        || task.mLockTaskAuth == LOCK_TASK_AUTH_LAUNCHABLE_PRIV
                        || (task.mLockTaskAuth == LOCK_TASK_AUTH_ALLOWLISTED
                                && lockTaskController.getLockTaskModeState()
                                        == LOCK_TASK_MODE_LOCKED)) {
                    lockTaskController.startLockTaskMode(task, false, 0 /* blank UID */);
                }
    
                try {
                    if (!proc.hasThread()) {
                        throw new RemoteException();
                    }
                    List<ResultInfo> results = null;
                    List<ReferrerIntent> newIntents = null;
                    if (andResume) {
                        // We don't need to deliver new intents and/or set results if activity is going
                        // to pause immediately after launch.
                        results = r.results;
                        newIntents = r.newIntents;
                    }
                    if (DEBUG_SWITCH) Slog.v(TAG_SWITCH,
                            "Launching: " + r + " savedState=" + r.getSavedState()
                                    + " with results=" + results + " newIntents=" + newIntents
                                    + " andResume=" + andResume);
                    EventLogTags.writeWmRestartActivity(r.mUserId, System.identityHashCode(r),
                            task.mTaskId, r.shortComponentName);
                    if (r.isActivityTypeHome()) {
                        // Home process is the root process of the task.
                        updateHomeProcess(task.getBottomMostActivity().app);
                    }
                    mService.getPackageManagerInternalLocked().notifyPackageUse(
                            r.intent.getComponent().getPackageName(), NOTIFY_PACKAGE_USE_ACTIVITY);
                    r.forceNewConfig = false;
                    mService.getAppWarningsLocked().onStartActivity(r);
                    r.compat = mService.compatibilityInfoForPackageLocked(r.info.applicationInfo);
    
                    // Because we could be starting an Activity in the system process this may not go
                    // across a Binder interface which would create a new Configuration. Consequently
                    // we have to always create a new Configuration here.
                    final Configuration procConfig = proc.prepareConfigurationForLaunchingActivity();
                    final MergedConfiguration mergedConfiguration = new MergedConfiguration(
                            procConfig, r.getMergedOverrideConfiguration());
                    r.setLastReportedConfiguration(mergedConfiguration);
    
                    logIfTransactionTooLarge(r.intent, r.getSavedState());
    
                    if (r.isEmbedded()) {
                        // Sending TaskFragmentInfo to client to ensure the info is updated before
                        // the activity creation.
                        mService.mTaskFragmentOrganizerController.dispatchPendingInfoChangedEvent(
                                r.getOrganizedTaskFragment());
                    }
    
                    // Create activity launch transaction.
                    final ClientTransaction clientTransaction = ClientTransaction.obtain(
                            proc.getThread(), r.appToken);
    
                    final boolean isTransitionForward = r.isTransitionForward();
                    clientTransaction.addCallback(LaunchActivityItem.obtain(new Intent(r.intent),
                            System.identityHashCode(r), r.info,
                            // TODO: Have this take the merged configuration instead of separate global
                            // and override configs.
                            mergedConfiguration.getGlobalConfiguration(),
                            mergedConfiguration.getOverrideConfiguration(), r.compat,
                            r.getFilteredReferrer(r.launchedFromPackage), task.voiceInteractor,
                            proc.getReportedProcState(), r.getSavedState(), r.getPersistentSavedState(),
                            results, newIntents, r.takeOptions(), isTransitionForward,
                            proc.createProfilerInfoIfNeeded(), r.assistToken, activityClientController,
                            r.createFixedRotationAdjustmentsIfNeeded(), r.shareableActivityToken,
                            r.getLaunchedFromBubble()));
    
                    // Set desired final state.
                    final ActivityLifecycleItem lifecycleItem;
                    if (andResume) {
                        lifecycleItem = ResumeActivityItem.obtain(isTransitionForward);
                    } else {
                        lifecycleItem = PauseActivityItem.obtain();
                    }
                    clientTransaction.setLifecycleStateRequest(lifecycleItem);
    
                    // Schedule transaction.
                    mService.getLifecycleManager().scheduleTransaction(clientTransaction);
    
                    if (procConfig.seq > mRootWindowContainer.getConfiguration().seq) {
                        // If the seq is increased, there should be something changed (e.g. registered
                        // activity configuration).
                        proc.setLastReportedConfiguration(procConfig);
                    }
                    if ((proc.mInfo.privateFlags & ApplicationInfo.PRIVATE_FLAG_CANT_SAVE_STATE) != 0
                            && mService.mHasHeavyWeightFeature) {
                        // This may be a heavy-weight process! Note that the package manager will ensure
                        // that only activity can run in the main process of the .apk, which is the only
                        // thing that will be considered heavy-weight.
                        if (proc.mName.equals(proc.mInfo.packageName)) {
                            if (mService.mHeavyWeightProcess != null
                                    && mService.mHeavyWeightProcess != proc) {
                                Slog.w(TAG, "Starting new heavy weight process " + proc
                                        + " when already running "
                                        + mService.mHeavyWeightProcess);
                            }
                            mService.setHeavyWeightProcess(r);
                        }
                    }
    
                } catch (RemoteException e) {
                    if (r.launchFailed) {
                        // This is the second time we failed -- finish activity and give up.
                        Slog.e(TAG, "Second failure launching "
                                + r.intent.getComponent().flattenToShortString() + ", giving up", e);
                        proc.appDied("2nd-crash");
                        r.finishIfPossible("2nd-crash", false /* oomAdj */);
                        return false;
                    }
    
                    // This is the first time we failed -- restart process and
                    // retry.
                    r.launchFailed = true;
                    proc.removeActivity(r, true /* keepAssociation */);
                    throw e;
                }
            } finally {
                endDeferResume();
                proc.resumeConfigurationDispatch();
            }
    
            r.launchFailed = false;
    
            // TODO(lifecycler): Resume or pause requests are done as part of launch transaction,
            // so updating the state should be done accordingly.
            if (andResume && readyToResume()) {
                // As part of the process of launching, ActivityThread also performs
                // a resume.
                rootTask.minimalResumeActivityLocked(r);
            } else {
                // This activity is not starting in the resumed state... which should look like we asked
                // it to pause+stop (but remain visible), and it has done so and reported back the
                // current icicle and other state.
                ProtoLog.v(WM_DEBUG_STATES, "Moving to PAUSED: %s "
                        + "(starting in paused state)", r);
                r.setState(PAUSED, "realStartActivityLocked");
                mRootWindowContainer.executeAppTransitionForAllDisplay();
            }
            // Perform OOM scoring after the activity state is set, so the process can be updated with
            // the latest state.
            proc.onStartActivity(mService.mTopProcessState, r.info);
    
            // Launch the new version setup screen if needed.  We do this -after-
            // launching the initial activity (that is, home), so that it can have
            // a chance to initialize itself while in the background, making the
            // switch back to it faster and look better.
            if (mRootWindowContainer.isTopDisplayFocusedRootTask(rootTask)) {
                mService.getActivityStartController().startSetupActivity();
            }
    
            // Update any services we are bound to that might care about whether
            // their client may have activities.
            if (r.app != null) {
                r.app.updateServiceConnectionActivities();
            }
    
            return true;
        }

# /home/data/aosp/android-12.1.0\_r1/frameworks/base/services/core/java/com/android/server/wm/WindowProcessController.java

\--WindowProcessController.onStartActivity--

        void onStartActivity(int topProcessState, ActivityInfo info) {
            String packageName = null;
            if ((info.flags & ActivityInfo.FLAG_MULTIPROCESS) == 0
                    || !"android".equals(info.packageName)) {
                // Don't add this if it is a platform component that is marked to run in multiple
                // processes, because this is actually part of the framework so doesn't make sense
                // to track as a separate apk in the process.
                packageName = info.packageName;
            }
            // update ActivityManagerService.PendingStartActivityUids list.
            if (topProcessState == ActivityManager.PROCESS_STATE_TOP) {
                mAtm.mAmInternal.addPendingTopUid(mUid, mPid);
            }
            prepareOomAdjustment();
            // Posting the message at the front of queue so WM lock isn't held when we call into AM,
            // and the process state of starting activity can be updated quicker which will give it a
            // higher scheduling group.
            final Message m = PooledLambda.obtainMessage(WindowProcessListener::onStartActivity,
                    mListener, topProcessState, shouldSetProfileProc(), packageName,
                    info.applicationInfo.longVersionCode);
            mAtm.mH.sendMessageAtFrontOfQueue(m);
        }

# ATMS与ActivityThread通信

客户端Instrumentation.execStartActivity的方法内跨进程调用的时候，传客户端的whoThread是IApplicationThread，指向的是ActivityThread的内部类ApplicationThread，

    ActivityTaskManager.getService().startActivity(whoThread,
                        who.getOpPackageName(), who.getAttributionTag(), intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()), token,
                        target != null ? target.mEmbeddedID : null, 
                        requestCode, 0, null, options);

服务端ActivityTaskManagerService的startActivityAsUser会接收ApplicationThread，并传给ActivityStarter，并放入Request mRequest里面，

    public int startActivityAsUser(IApplicationThread caller, String callingPackage,
                String callingFeatureId, Intent intent, String resolvedType, IBinder resultTo,
                String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo,
                Bundle bOptions, int userId) {

ActivityThread 有mH对象，被内部类ApplicationThread调用sendMessage方法去发送各种事件，ApplicationThread的事件发送由ActivityStarter去调用，然后在ActivityThread的H类处理事件结果。

ActivityTaskManagerService里面有个class LocalService extends ActivityTaskManagerInternal {，里面会有各种生命周期调用和间接消息发送

/home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/ActivityThread.java

/home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/ActivityThread.ApplicationThread

/home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/ActivityThread.H

        private class ApplicationThread extends IApplicationThread.Stub {
            private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
            if (DEBUG_MESSAGES) {
                Slog.v(TAG,
                        "SCHEDULE " + what + " " + mH.codeToString(what) + ": " + arg1 + " / " + obj);
            }
            Message msg = Message.obtain();
            msg.what = what;
            msg.obj = obj;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            if (async) {
                msg.setAsynchronous(true);
            }
            mH.sendMessage(msg);
        }

        class H extends Handler {
            public static final int BIND_APPLICATION        = 110;
            @UnsupportedAppUsage
            public static final int EXIT_APPLICATION        = 111;

# H.RELAUNCH\_ACTIVITY发送

    ApplicationThread的bindApplication方法执行，
    执行时机是应用1的startActivity，在应用1内找不到的时候，跨进程找到应用2，于是启动应用2，
    开始打开应用2的Activity
    				@Override
            public final void bindApplication(String processName, ApplicationInfo appInfo,
                    ProviderInfoList providerList, ComponentName instrumentationName,
                    ProfilerInfo profilerInfo, Bundle instrumentationArgs,
                    IInstrumentationWatcher instrumentationWatcher,
                    IUiAutomationConnection instrumentationUiConnection, int debugMode,
                    boolean enableBinderTracking, boolean trackAllocation,
                    boolean isRestrictedBackupMode, boolean persistent, Configuration config,
                    CompatibilityInfo compatInfo, Map services, Bundle coreSettings,
                    String buildSerial, AutofillOptions autofillOptions,
                    ContentCaptureOptions contentCaptureOptions, long[] disabledCompatChanges,
                    SharedMemory serializedSystemFontMap) {
                if (services != null) {
                    if (false) {
                        // Test code to make sure the app could see the passed-in services.
                        for (Object oname : services.keySet()) {
                            if (services.get(oname) == null) {
                                continue; // AM just passed in a null service.
                            }
                            String name = (String) oname;
    
                            // See b/79378449 about the following exemption.
                            switch (name) {
                                case "package":
                                case Context.WINDOW_SERVICE:
                                    continue;
                            }
    
                            if (ServiceManager.getService(name) == null) {
                                Log.wtf(TAG, "Service " + name + " should be accessible by this app");
                            }
                        }
                    }
    
                    // Setup the service cache in the ServiceManager
                    ServiceManager.initServiceCache(services);
                }
    
                setCoreSettings(coreSettings);
            
           public void setCoreSettings(Bundle coreSettings) {
                sendMessage(H.SET_CORE_SETTINGS, coreSettings);
            }
        
       H类：
              case SET_CORE_SETTINGS:
                        Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "setCoreSettings");
                        handleSetCoreSettings((Bundle) msg.obj);
                        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                        break;
                        
         private void handleSetCoreSettings(Bundle coreSettings) {
            synchronized (mCoreSettingsLock) {
                mCoreSettings = coreSettings;
            }
            onCoreSettingsChange();
        }

        private void onCoreSettingsChange() {
            if (updateDebugViewAttributeState()) {
                // request all activities to relaunch for the changes to take place
                relaunchAllActivities(false /* preserveWindows */, "onCoreSettingsChange");
            }
        }

        这个方法不知道在哪里调用
        void scheduleRelaunchActivity(IBinder token) {
            final ActivityClientRecord r = mActivities.get(token);
            if (r != null) {
                Log.i(TAG, "Schedule relaunch activity: " + r.activityInfo.name);
                scheduleRelaunchActivityIfPossible(r, !r.stopped /* preserveWindow */);
            }
        }

        private void relaunchAllActivities(boolean preserveWindows, String reason) {
            Log.i(TAG, "Relaunch all activities: " + reason);
            for (int i = mActivities.size() - 1; i >= 0; i--) {
                scheduleRelaunchActivityIfPossible(mActivities.valueAt(i), preserveWindows);
            }
        }

        // 启动launch
        private void scheduleRelaunchActivityIfPossible(@NonNull ActivityClientRecord r,
                boolean preserveWindow) {
            if ((r.activity != null && r.activity.mFinished) || r.token instanceof Binder) {
                // Do not schedule relaunch if the activity is finishing or is a local object (e.g.
                // created by ActivtiyGroup that server side doesn't recognize it).
                return;
            }
            if (preserveWindow && r.window != null) {
                r.mPreserveWindow = true;
            }
            mH.removeMessages(H.RELAUNCH_ACTIVITY, r.token);
            sendMessage(H.RELAUNCH_ACTIVITY, r.token);
        }

H接收H.RELAUNCH\_ACTIVITY并处理消息

     case RELAUNCH_ACTIVITY:
                        handleRelaunchActivityLocally((IBinder) msg.obj);

     /** Performs the activity relaunch locally vs. requesting from system-server. */
        public void handleRelaunchActivityLocally(IBinder token) {
            final ActivityClientRecord r = mActivities.get(token);
            if (r == null) {
                Log.w(TAG, "Activity to relaunch no longer exists");
                return;
            }
    
            final int prevState = r.getLifecycleState();
    
            if (prevState < ON_START || prevState > ON_STOP) {
                Log.w(TAG, "Activity state must be in [ON_START..ON_STOP] in order to be relaunched,"
                        + "current state is " + prevState);
                return;
            }
    
            // Initialize a relaunch request.
            final MergedConfiguration mergedConfiguration = new MergedConfiguration(
                    r.createdConfig != null
                            ? r.createdConfig : mConfigurationController.getConfiguration(),
                    r.overrideConfig);
            final ActivityRelaunchItem activityRelaunchItem = ActivityRelaunchItem.obtain(
                    null /* pendingResults */, null /* pendingIntents */, 0 /* configChanges */,
                    mergedConfiguration, r.mPreserveWindow);
            // Make sure to match the existing lifecycle state in the end of the transaction.
            final ActivityLifecycleItem lifecycleRequest =
                    TransactionExecutorHelper.getLifecycleRequestForCurrentState(r);
            // Schedule the transaction.
            final ClientTransaction transaction = ClientTransaction.obtain(this.mAppThread, r.token);
            transaction.addCallback(activityRelaunchItem);
            transaction.setLifecycleStateRequest(lifecycleRequest);
            executeTransaction(transaction);
        }

/home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/ClientTransactionHandler.java

        public void executeTransaction(ClientTransaction transaction) {
            transaction.preExecute(this);
            getTransactionExecutor().execute(transaction);
            transaction.recycle();
        }

/home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/servertransaction/ClientTransaction.java

        public void preExecute(android.app.ClientTransactionHandler clientTransactionHandler) {
            if (mActivityCallbacks != null) {
                final int size = mActivityCallbacks.size();
                for (int i = 0; i < size; ++i) {
                    mActivityCallbacks.get(i).preExecute(clientTransactionHandler, mActivityToken);
                }
            }
            if (mLifecycleStateRequest != null) {
                mLifecycleStateRequest.preExecute(clientTransactionHandler, mActivityToken);
            }
        }

/home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/servertransaction/ActivityRelaunchItem.java继承ActivityTransactionItem继承ClientTransactionItem实现BaseClientRequest

        @Override
        public void preExecute(android.app.ClientTransactionHandler client, IBinder token) {
            client.updatePendingConfiguration(mConfiguration);
        }

        @Override
        public void preExecute(ClientTransactionHandler client, IBinder token) {
            mActivityClientRecord = client.prepareRelaunchActivity(token, mPendingResults,
                    mPendingNewIntents, mConfigChanges, mConfig, mPreserveWindow);
        }

        @Override
        public void execute(ClientTransactionHandler client, ActivityClientRecord r,
                PendingTransactionActions pendingActions) {
            if (mActivityClientRecord == null) {
                if (DEBUG_ORDER) Slog.d(TAG, "Activity relaunch cancelled");
                return;
            }
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityRestart");
            client.handleRelaunchActivity(mActivityClientRecord, pendingActions);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        }

/home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/ActivityThread.java

     @Override
        public void handleRelaunchActivity(ActivityClientRecord tmp,
                PendingTransactionActions pendingActions) {
            // If we are getting ready to gc after going to the background, well
            // we are back active so skip it.
            unscheduleGcIdler();
            mSomeActivitiesChanged = true;
    
            int configChanges = 0;
    
            // First: make sure we have the most recent configuration and most
            // recent version of the activity, or skip it if some previous call
            // had taken a more recent version.
            synchronized (mResourcesManager) {
                int N = mRelaunchingActivities.size();
                IBinder token = tmp.token;
                tmp = null;
                for (int i=0; i<N; i++) {
                    ActivityClientRecord r = mRelaunchingActivities.get(i);
                    if (r.token == token) {
                        tmp = r;
                        configChanges |= tmp.pendingConfigChanges;
                        mRelaunchingActivities.remove(i);
                        i--;
                        N--;
                    }
                }
    
                if (tmp == null) {
                    if (DEBUG_CONFIGURATION) Slog.v(TAG, "Abort, activity not relaunching!");
                    return;
                }
    
                if (DEBUG_CONFIGURATION) Slog.v(TAG, "Relaunching activity "
                        + tmp.token + " with configChanges=0x"
                        + Integer.toHexString(configChanges));
            }
    
            Configuration changedConfig = mConfigurationController.getPendingConfiguration(
                    true /* clearPending */);
            mPendingConfiguration = null;
    
            if (tmp.createdConfig != null) {
                // If the activity manager is passing us its current config,
                // assume that is really what we want regardless of what we
                // may have pending.
                final Configuration config = mConfigurationController.getConfiguration();
                if (config == null
                        || (tmp.createdConfig.isOtherSeqNewer(config)
                                && config.diff(tmp.createdConfig) != 0)) {
                    if (changedConfig == null
                            || tmp.createdConfig.isOtherSeqNewer(changedConfig)) {
                        changedConfig = tmp.createdConfig;
                    }
                }
            }
    
            if (DEBUG_CONFIGURATION) Slog.v(TAG, "Relaunching activity "
                    + tmp.token + ": changedConfig=" + changedConfig);
    
            // If there was a pending configuration change, execute it first.
            if (changedConfig != null) {
                mConfigurationController.updateDefaultDensity(changedConfig.densityDpi);
                mConfigurationController.handleConfigurationChanged(changedConfig, null);
    
                // These are only done to maintain @UnsupportedAppUsage and should be removed someday.
                mCurDefaultDisplayDpi = mConfigurationController.getCurDefaultDisplayDpi();
                mConfiguration = mConfigurationController.getConfiguration();
            }
    
            ActivityClientRecord r = mActivities.get(tmp.token);
            if (DEBUG_CONFIGURATION) Slog.v(TAG, "Handling relaunch of " + r);
            if (r == null) {
                return;
            }
    
            r.activity.mConfigChangeFlags |= configChanges;
            r.mPreserveWindow = tmp.mPreserveWindow;
    
            r.activity.mChangingConfigurations = true;
    
            // If we are preserving the main window across relaunches we would also like to preserve
            // the children. However the client side view system does not support preserving
            // the child views so we notify the window manager to expect these windows to
            // be replaced and defer requests to destroy or hide them. This way we can achieve
            // visual continuity. It's important that we do this here prior to pause and destroy
            // as that is when we may hide or remove the child views.
            //
            // There is another scenario, if we have decided locally to relaunch the app from a
            // call to recreate, then none of the windows will be prepared for replacement or
            // preserved by the server, so we want to notify it that we are preparing to replace
            // everything
            try {
                if (r.mPreserveWindow) {
                    WindowManagerGlobal.getWindowSession().prepareToReplaceWindows(
                            r.token, true /* childrenOnly */);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
    
            handleRelaunchActivityInner(r, configChanges, tmp.pendingResults, tmp.pendingIntents,
                    pendingActions, tmp.startsNotResumed, tmp.overrideConfig, "handleRelaunchActivity");
            if (pendingActions != null) {
                // Only report a successful relaunch to WindowManager.
                pendingActions.setReportRelaunchToWindowManager(true);
            }
        }

    private void handleRelaunchActivityInner(ActivityClientRecord r, int configChanges,
                List<ResultInfo> pendingResults, List<ReferrerIntent> pendingIntents,
                PendingTransactionActions pendingActions, boolean startsNotResumed,
                Configuration overrideConfig, String reason) {
            // Preserve last used intent, it may be set from Activity#setIntent().
            final Intent customIntent = r.activity.mIntent;
            // Need to ensure state is saved.
            if (!r.paused) {
                performPauseActivity(r, false, reason, null /* pendingActions */);
            }
            if (!r.stopped) {
                callActivityOnStop(r, true /* saveState */, reason);
            }
    
            handleDestroyActivity(r, false, configChanges, true, reason);
    
            r.activity = null;
            r.window = null;
            r.hideForNow = false;
            r.nextIdle = null;
            // Merge any pending results and pending intents; don't just replace them
            if (pendingResults != null) {
                if (r.pendingResults == null) {
                    r.pendingResults = pendingResults;
                } else {
                    r.pendingResults.addAll(pendingResults);
                }
            }
            if (pendingIntents != null) {
                if (r.pendingIntents == null) {
                    r.pendingIntents = pendingIntents;
                } else {
                    r.pendingIntents.addAll(pendingIntents);
                }
            }
            r.startsNotResumed = startsNotResumed;
            r.overrideConfig = overrideConfig;
    
            handleLaunchActivity(r, pendingActions, customIntent);
        }

     @Override
        public Activity handleLaunchActivity(ActivityClientRecord r,
                PendingTransactionActions pendingActions, Intent customIntent) {
            // If we are getting ready to gc after going to the background, well
            // we are back active so skip it.
            unscheduleGcIdler();
            mSomeActivitiesChanged = true;
    
            if (r.profilerInfo != null) {
                mProfiler.setProfiler(r.profilerInfo);
                mProfiler.startProfiling();
            }
    
            if (r.mPendingFixedRotationAdjustments != null) {
                // The rotation adjustments must be applied before handling configuration, so process
                // level display metrics can be adjusted.
                overrideApplicationDisplayAdjustments(r.token, adjustments ->
                        adjustments.setFixedRotationAdjustments(r.mPendingFixedRotationAdjustments));
            }
    
            // Make sure we are running with the most recent config.
            mConfigurationController.handleConfigurationChanged(null, null);
    
            if (localLOGV) Slog.v(
                TAG, "Handling launch of " + r);
    
            // Initialize before creating the activity
            if (ThreadedRenderer.sRendererEnabled
                    && (r.activityInfo.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0) {
                HardwareRenderer.preload();
            }
            WindowManagerGlobal.initialize();
    
            // Hint the GraphicsEnvironment that an activity is launching on the process.
            GraphicsEnvironment.hintActivityLaunch();
    
            final Activity a = performLaunchActivity(r, customIntent);
    
            if (a != null) {
                r.createdConfig = new Configuration(mConfigurationController.getConfiguration());
                reportSizeConfigurations(r);
                if (!r.activity.mFinished && pendingActions != null) {
                    pendingActions.setOldState(r.state);
                    pendingActions.setRestoreInstanceState(true);
                    pendingActions.setCallOnPostCreate(true);
                }
            } else {
                // If there was an error, for any reason, tell the activity manager to stop us.
                ActivityClient.getInstance().finishActivity(r.token, Activity.RESULT_CANCELED,
                        null /* resultData */, Activity.DONT_FINISH_TASK_WITH_ACTIVITY);
            }
    
            return a;
        }

     /**  Core implementation of activity launch. */
        private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
            ActivityInfo aInfo = r.activityInfo;
            if (r.packageInfo == null) {
                r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo,
                        Context.CONTEXT_INCLUDE_CODE);
            }
    
            ComponentName component = r.intent.getComponent();
            if (component == null) {
                component = r.intent.resolveActivity(
                    mInitialApplication.getPackageManager());
                r.intent.setComponent(component);
            }
    
            if (r.activityInfo.targetActivity != null) {
                component = new ComponentName(r.activityInfo.packageName,
                        r.activityInfo.targetActivity);
            }
    
            ContextImpl appContext = createBaseContextForActivity(r);
            Activity activity = null;
            try {
                java.lang.ClassLoader cl = appContext.getClassLoader();
                // 新建Activity
                activity = mInstrumentation.newActivity(
                        cl, component.getClassName(), r.intent);
                StrictMode.incrementExpectedActivityCount(activity.getClass());
                r.intent.setExtrasClassLoader(cl);
                r.intent.prepareToEnterProcess(isProtectedComponent(r.activityInfo),
                        appContext.getAttributionSource());
                if (r.state != null) {
                    r.state.setClassLoader(cl);
                }
            } catch (Exception e) {
                if (!mInstrumentation.onException(activity, e)) {
                    throw new RuntimeException(
                        "Unable to instantiate activity " + component
                        + ": " + e.toString(), e);
                }
            }
    
            try {
                Application app = r.packageInfo.makeApplication(false, mInstrumentation);
    
                if (localLOGV) Slog.v(TAG, "Performing launch of " + r);
                if (localLOGV) Slog.v(
                        TAG, r + ": app=" + app
                        + ", appName=" + app.getPackageName()
                        + ", pkg=" + r.packageInfo.getPackageName()
                        + ", comp=" + r.intent.getComponent().toShortString()
                        + ", dir=" + r.packageInfo.getAppDir());
    
                // updatePendingActivityConfiguration() reads from mActivities to update
                // ActivityClientRecord which runs in a different thread. Protect modifications to
                // mActivities to avoid race.
                synchronized (mResourcesManager) {
                    mActivities.put(r.token, r);
                }
    
                if (activity != null) {
                    CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
                    Configuration config =
                            new Configuration(mConfigurationController.getCompatConfiguration());
                    if (r.overrideConfig != null) {
                        config.updateFrom(r.overrideConfig);
                    }
                    if (DEBUG_CONFIGURATION) Slog.v(TAG, "Launching activity "
                            + r.activityInfo.name + " with config " + config);
                    Window window = null;
                    if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                        window = r.mPendingRemoveWindow;
                        r.mPendingRemoveWindow = null;
                        r.mPendingRemoveWindowManager = null;
                    }
    
                    // Activity resources must be initialized with the same loaders as the
                    // application context.
                    appContext.getResources().addLoaders(
                            app.getResources().getLoaders().toArray(new ResourcesLoader[0]));
    
                    appContext.setOuterContext(activity);
                    // 添加到屏幕
                    activity.attach(appContext, this, getInstrumentation(), r.token,
                            r.ident, app, r.intent, r.activityInfo, title, r.parent,
                            r.embeddedID, r.lastNonConfigurationInstances, config,
                            r.referrer, r.voiceInteractor, window, r.configCallback,
                            r.assistToken, r.shareableActivityToken);
    
                    if (customIntent != null) {
                        activity.mIntent = customIntent;
                    }
                    r.lastNonConfigurationInstances = null;
                    checkAndBlockForNetworkAccess();
                    activity.mStartedActivity = false;
                    int theme = r.activityInfo.getThemeResource();
                    if (theme != 0) {
                        activity.setTheme(theme);
                    }
    
                    if (r.mActivityOptions != null) {
                        activity.mPendingOptions = r.mActivityOptions;
                        r.mActivityOptions = null;
                    }
                    activity.mLaunchedFromBubble = r.mLaunchedFromBubble;
                    activity.mCalled = false;
                    // Assigning the activity to the record before calling onCreate() allows
                    // ActivityThread#getActivity() lookup for the callbacks triggered from
                    // ActivityLifecycleCallbacks#onActivityCreated() or
                    // ActivityLifecycleCallback#onActivityPostCreated().
                    r.activity = activity;
                    // Activity创建
                    if (r.isPersistable()) {
                        mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
                    } else {
                        mInstrumentation.callActivityOnCreate(activity, r.state);
                    }
                    if (!activity.mCalled) {
                        throw new SuperNotCalledException(
                            "Activity " + r.intent.getComponent().toShortString() +
                            " did not call through to super.onCreate()");
                    }
                    mLastReportedWindowingMode.put(activity.getActivityToken(),
                            config.windowConfiguration.getWindowingMode());
                }
                r.setState(ON_CREATE);
    
            } catch (SuperNotCalledException e) {
                throw e;
    
            } catch (Exception e) {
                if (!mInstrumentation.onException(activity, e)) {
                    throw new RuntimeException(
                        "Unable to start activity " + component
                        + ": " + e.toString(), e);
                }
            }
    
            return activity;
        }

/home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/Instrumentation.java

        public void callActivityOnCreate(Activity activity, Bundle icicle) {
            prePerformCreate(activity);
            activity.performCreate(icicle);
            postPerformCreate(activity);
        }

/home/data/aosp/android-12.1.0\_r1/frameworks/base/core/java/android/app/Activity.java

     @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.R, trackingBug = 170729553)
        final void performCreate(Bundle icicle, PersistableBundle persistentState) {
            if (Trace.isTagEnabled(Trace.TRACE_TAG_WINDOW_MANAGER)) {
                Trace.traceBegin(Trace.TRACE_TAG_WINDOW_MANAGER, "performCreate:"
                        + mComponent.getClassName());
            }
            dispatchActivityPreCreated(icicle);
            mCanEnterPictureInPicture = true;
            // initialize mIsInMultiWindowMode and mIsInPictureInPictureMode before onCreate
            final int windowingMode = getResources().getConfiguration().windowConfiguration
                    .getWindowingMode();
            mIsInMultiWindowMode = inMultiWindowMode(windowingMode);
            mIsInPictureInPictureMode = windowingMode == WINDOWING_MODE_PINNED;
            restoreHasCurrentPermissionRequest(icicle);
            // Activity的生命周期
            if (persistentState != null) {
                onCreate(icicle, persistentState);
            } else {
                onCreate(icicle);
            }
            EventLogTags.writeWmOnCreateCalled(mIdent, getComponentName().getClassName(),
                    "performCreate");
            mActivityTransitionState.readState(icicle);
    
            mVisibleFromClient = !mWindow.getWindowStyle().getBoolean(
                    com.android.internal.R.styleable.Window_windowNoDisplay, false);
            mFragments.dispatchActivityCreated();
            mActivityTransitionState.setEnterActivityOptions(this, getActivityOptions());
            dispatchActivityPostCreated(icicle);
            Trace.traceEnd(Trace.TRACE_TAG_WINDOW_MANAGER);
        }
    
        final void performNewIntent(@NonNull Intent intent) {
            mCanEnterPictureInPicture = true;
            onNewIntent(intent);
        }
    
        final void performStart(String reason) {
            dispatchActivityPreStarted();
            mActivityTransitionState.setEnterActivityOptions(this, getActivityOptions());
            mFragments.noteStateNotSaved();
            mCalled = false;
            mFragments.execPendingActions();
            mInstrumentation.callActivityOnStart(this);
            EventLogTags.writeWmOnStartCalled(mIdent, getComponentName().getClassName(), reason);
    
            if (!mCalled) {
                throw new SuperNotCalledException(
                    "Activity " + mComponent.toShortString() +
                    " did not call through to super.onStart()");
            }
            mFragments.dispatchStart();
            mFragments.reportLoaderStart();
    
            // Warn app developers if the dynamic linker logged anything during startup.
            boolean isAppDebuggable =
                    (mApplication.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            if (isAppDebuggable) {
                String dlwarning = getDlWarning();
                if (dlwarning != null) {
                    String appName = getApplicationInfo().loadLabel(getPackageManager())
                            .toString();
                    String warning = "Detected problems with app native libraries\n" +
                                     "(please consult log for detail):\n" + dlwarning;
                    if (isAppDebuggable) {
                          new AlertDialog.Builder(this).
                              setTitle(appName).
                              setMessage(warning).
                              setPositiveButton(android.R.string.ok, null).
                              setCancelable(false).
                              show();
                    } else {
                        Toast.makeText(this, appName + "\n" + warning, Toast.LENGTH_LONG).show();
                    }
                }
            }
    
            GraphicsEnvironment.getInstance().showAngleInUseDialogBox(this);
    
            mActivityTransitionState.enterReady(this);
            dispatchActivityPostStarted();
        }