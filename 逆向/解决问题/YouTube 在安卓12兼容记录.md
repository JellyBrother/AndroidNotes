# YouTube 在安卓12兼容记录

### 现象

在小米12的手机上，初次启动能够正常进入应用，再次启动，则出现黑屏，并且一直出现GC，然后导致ANR

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/XJ9LnWRooe38lvDe/img/f2d10f7e-efa1-47b0-83f6-40b39a7e356d.png)

### 分析过程

*   初步详细分析log, 解决log中出现的异常. （问题依旧）
    
*   通过strace命令跟踪youtube进程，没有发现有效的系统调用。纯Java逻辑导致卡死，黑屏，一直GC（没有效信息）
    
*   因为最后的现象是一直GC，内存不停疯涨，尝试使用Android Studio来Dump Head Memory（操作失败，可能因为一直在GC的原因，等待很长时间也无法dump下来），Trace Java Method也提示操作失败。无法正常Trace（这块后续要做一下分析，主进程能Trace，但是虚拟App进程无法正常Trace）（分析失败）
    
*   通过kill -6 {youtube pid}，让debuggerd来打印最后的堆栈。（找到关键信息）
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/XJ9LnWRooe38lvDe/img/5f1c2c7d-79a3-468d-8b3a-80cb196a9128.png) 从堆栈上看，应该是Java层代码发生了递归. aemm.toString() -> aemm.e() -> StringBuilder.append() -> String.valueOf() -> aemm.toString() 用jadx分析youtube

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getClass().getName().startsWith("com.google.common.util.concurrent.")) {
            sb.append(getClass().getSimpleName());
        } else {
            sb.append(getClass().getName());
        }
        sb.append('@');
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("[status=");
        if (isCancelled()) {
            sb.append("CANCELLED");
        } else if (isDone()) {
            d(sb);
        } else {
            e(sb); // 进入了这里
        }
        sb.append("]");
        return sb.toString();
    }

    private final void e(StringBuilder sb) {
        String str;
        int length = sb.length();
        sb.append("PENDING");
        Object obj = this.value;
        if (obj instanceof aeme) {
            sb.append(", setFuture=[");
            f(sb, ((aeme) obj).b);  // 进入了这里
            sb.append("]");
        } else {
            try {
                str = adsz.c(mB());
            } catch (RuntimeException | StackOverflowError e) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Exception thrown from implementation: ");
                Class<?> cls = e.getClass();
                sb2.append(cls);
                str = "Exception thrown from implementation: ".concat(String.valueOf(cls));
            }
            if (str != null) {
                sb.append(", info=[");
                sb.append(str);
                sb.append("]");
            }
        }
        if (isDone()) {
            sb.delete(length, sb.length());
            d(sb);
        }
    }

    private final void f(StringBuilder sb, Object obj) {
        try {
            if (obj == this) {
                sb.append("this future");
            } else {
                sb.append(obj); // 进入了这里，这里就是调用了toString()，导致了递归
            }
        } catch (RuntimeException | StackOverflowError e) {
            sb.append("Exception thrown from implementation: ");
            sb.append(e.getClass());
        }
    }

直接原因分析清楚了，通过hook aemm.e后，解决了该问题.

    XposedHelpers.findAndHookMethod(app.getClassLoader().loadClass("aemm"), "e", StringBuilder.class, new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            AppLogger.e("YouTube trace me %d, %s, Looper.myLooper().mLogging %s", Process.myPid(), Arrays.toString(Thread.currentThread().getStackTrace()), ReflectionHelper.getObjectField(Looper.myLooper(), "mLogging"));
            param.setResult(null);
        }
    });

### 根本原因

直接原因分析清楚了，通过hook aemm.e 这个函数，可以打印堆栈。通过堆栈反向确认来源

    dalvik.system.VMStack.getThreadStackTrace(Native Method), java.lang.Thread.getStackTrace(Thread.java:1724), com.dualapp.client.ClientService$2.beforeHookedMethod(ClientService.java:655), de.robv.android.xposed.XCMethodHook.callBeforeHookedMethod(XCMethodHook.java:51), com.swift.sandhook.xposedcompat.hookstub.HookStubManager.hookBridge(HookStubManager.java:276), com.swift.sandhook.xposedcompat.hookstub.MethodHookerStubs64.stubhook0(MethodHookerStubs64.java:200),
    aemm.toString(PG:10),
    java.lang.String.valueOf(String.java:2924),
    java.lang.StringBuilder.append(StringBuilder.java:132),
    android.os.Looper.loopOnce(Looper.java:177),
    android.os.Looper.loop(Looper.java:299),
    android.app.ActivityThread.main(ActivityThread.java:8205),
    java.lang.reflect.Method.invoke(Native Method), com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:556), com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1045)

toString() 方法的调用来源 android.os.Looper.loopOnce(Looper.java:177) [http://aospxref.com/android-12.0.0\_r3/xref/frameworks/base/core/java/android/os/Looper.java](http://aospxref.com/android-12.0.0_r3/xref/frameworks/base/core/java/android/os/Looper.java)

    159      private static boolean loopOnce(final Looper me,
    160              final long ident, final int thresholdOverride) {
    161          Message msg = me.mQueue.next(); // might block
    162          if (msg == null) {
    163              // No message indicates that the message queue is quitting.
    164              return false;
    165          }
    166  
    167          // This must be in a local variable, in case a UI event sets the logger
    168          final Printer logging = me.mLogging;
    169          if (logging != null) {
                     // 这个地方，堆栈中的177行是在小米手机上的代码版本，和谷歌原生代码会稍有差异
    170              logging.println(">>>>> Dispatching to " + msg.target + " "
    171                      + msg.callback + ": " + msg.what); 
    172          }
    173          // Make sure the observer won't change while processing a transaction.
    174          final Observer observer = sObserver;

从loopOnce代码中发现，也就是有设置mLogging这个对象，就会导致youtube递归调用，黑屏现象。 似乎这是一种新的思路来反调试，mLogging对象主要是用于帮助打印更多app运行的log，一般是不会设置这个值的。打印mLogging对象的值 Looper.myLooper().mLogging com.tencent.matrix.trace.core.LooperMonitor$LooperPrinter@766c8e9 也就是启用了tencent的matrix trace模块后，matrix设置了该属性，从而导致问题发生。

### 结论

因为启用了matrix trace模块，matrix设置了mLogging属性，从而导致Youtube黑屏，递归调用。

### 解决方案

在虚拟App进程中关闭matrix trace模块。