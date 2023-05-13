# Activity Stack 类型问题分析

# 目标：试图总结出一个规律来解决Activity Stack 类型问题

Activity Stack 类型问题是我们GBox的BUG单里出现频率还挺高的一个类别，也是比较难解决的一类bug。

这类bug为什么这么重量级的原因：

1.  我们对Activity的改动比较多；（也需要自己维护很多activity的相关类，比如ActivityRecord之类的，维护的类一多，不同的变量在不同的时期应该设置不同的值）
    
2.  Activity的生命周期足够复杂；（从onCreate到onDestroy，可能在activity的每一个生命周期函数上都有需要处理的数据，同时也需要维护GBox自己的activity的栈，并且与真机的栈保持同步）
    
3.  Activity牵扯的面比较广；（就因为牵涉过于多，所以很难做到精准改动，往往改一个activity的bug会导致出现10个新的bug，所以我们对于这类bug的改动就是能判断包名就判断包名，尽量把影响降到最低）
    

但是复杂归复杂，Activity的bug还是有它的特征存在的，很容易被我们挖掘到。下面就通过一个简单的bug来开启今天的分享。

# Teams 登录问题

#### 问题现象：

登录teams应用，完成登录到应用首页后，点击返回又回到了登录页面。

（[此处应该有Bug视频](https://www.teambition.com/project/63d66827ce0b9d520e27531e/works/63d66827eebaab00177cc3dd/work/6412bd4a2306bd00182134e5)）

##### 解题思路：

1.  分析bug现象，找出方向
    
2.  查看activity stack
    

    使用命令查看activity stack的情况
    adb shell dumpsys activity
    
    宿主机登录完成
    Hist #0: ActivityRecord{f3380f2 u0 com.microsoft.teams/.mobile.views.activities.MainActivity t7265}
      Intent { flg=0x10000000 cmp=com.microsoft.teams/.mobile.views.activities.MainActivity (has extras) }
      ProcessRecord{6b0753f 10909:com.microsoft.teams/u0a244}
    
    虚拟机登录完成
    Hist #1: ActivityRecord{1d9b65a u0 com.gbox.android/com.vlite.sdk.proxy.ComponentProxyStubs$Activity02 t7400}
      Intent { typ=com.microsoft.teams/com.microsoft.skype.teams.views.activities.MainActivity cmp=com.gbox.android/com.vlite.sdk.proxy.ComponentProxyStubs$Activity02 (has extras) }
      ProcessRecord{a2c25e0 25242:com.gbox.android:vlapp02/u0a165}
    Hist #0: ActivityRecord{8a5d744 u0 com.gbox.android/com.vlite.sdk.proxy.ComponentProxyStubs$Activity02 t7400}
      Intent { typ=com.microsoft.teams/com.microsoft.skype.teams.views.activities.FreAuthActivity flg=0x80000 cmp=com.gbox.android/com.vlite.sdk.proxy.ComponentProxyStubs$Activity02 (has extras) }
      ProcessRecord{a2c25e0 25242:com.gbox.android:vlapp02/u0a165}

思考：本该结束生命的Activity为何还在栈中

3.  分析目标activity的生命周期；（为什么要分析生命周期呢，因为生命周期是最能说明activity状态的一些函数了）
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/AXNkOMNyzEDRlY74/img/a468699f-fefa-4d63-802d-59c2110cf83f.png)

左边是虚拟机，右边是真机。hook的是目标activity所有的函数，虽然图里飘红的很多，跟真机有很多流程不一致，但是我们主要只关心生命周期函数就行了。

抓onMAMDestroy的堆栈，看是从哪里调用过来的。

    java.lang.Throwable
    	at com.microsoft.skype.teams.views.activities.FreAuthActivity.onMAMDestroy(Native Method)
    	at com.microsoft.intune.mam.client.app.offline.OfflineActivityBehavior.onDestroy(Unknown Source:6)
    	at com.microsoft.intune.mam.client.app.MAMActivity.onDestroy(Unknown Source:2)
    	at android.app.Activity.performDestroy(Activity.java:8315)
    	at android.app.Instrumentation.callActivityOnDestroy(Instrumentation.java:1364)
    	at android.app.ActivityThread.performDestroyActivity(ActivityThread.java:5374)
    	at android.app.ActivityThread.handleDestroyActivity(ActivityThread.java:5420)
    	at android.app.servertransaction.DestroyActivityItem.execute(DestroyActivityItem.java:47)
    	at android.app.servertransaction.ActivityTransactionItem.execute(ActivityTransactionItem.java:45)
    	at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:176)
    	at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:97)
    	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2210)
    	at android.os.Handler.dispatchMessage(Handler.java:106)
    	at android.os.Looper.loopOnce(Looper.java:201)
    	at android.os.Looper.loop(Looper.java:288)
    	at android.app.ActivityThread.main(ActivityThread.java:7839)
    	at java.lang.reflect.Method.invoke(Native Method)
    	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)
    	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1003)

对比发现生命周期里少了一个onDestroy。（当然这里比对activity生命周期的方法太不优雅了）

（遍历打印目标activity的所有生命周期函数，它没实现？没关系，递归往上找它的父类，只要被调用过肯定能打印出来）

（那我们就只需要找到就是缺少的这一步 ondestroy 是应该在哪里调用的了，是应用自己调的还是系统调的呢。思路好像是清晰了一点，但不多）

（想要找到调用的地方得从源码里面去看了，对着源码一通乱翻）

在Android 9以上的代码里，activity生命周期函数都是发送 EXECUTE\_TRANSACTION 消息给到 ActivityThread$H 来执行的了

找到发送ClientLifecycleManager.scheduleTransaction，hook它，打印堆栈

应该很快就能找到最终调用的地方在 com.android.server.wm.Task.performClearTask()。

（应该是这里，我猜的，上面只是讲解了一种逻辑非常完备的方法去排查到 onDestroy 是从哪里调用的，其实我们日常改bug的过程中肯定是文思泉涌，灵感四射的，机智的你可能只需要一点线索就早怀疑到栈的问题了。直接从步骤2跳到步骤4）

4.  查看我们打的日志，查找startactivity的记录，发现有clear task的操作
    

    03-21 14:19:31.289 10165 14891 14891 D falco_31: android.app.IActivityTaskManager$Stub$Proxy.startActivity([android.app.ActivityThread$ApplicationThread@9beb194, com.gbox.android, null, Intent { flg=0x10008000 cmp=com.microsoft.teams/com.microsoft.skype.teams.views.activities.Fre4vActivity (has extras) }, null, android.os.BinderProxy@c208739, null, -1, 0, null, null]) | beforeInvokeMethod isActive = true

发现在点击登录后，应用又起了一个 Fre4vActivity ，它的目的就是为了把栈清掉。

flg=0x10008000 是由 Intent.FLAG\_ACTIVITY\_CLEAR\_TASK 和 Intent.FLAG\_ACTIVITY\_NEW\_TASK

下面是 Intent.FLAG\_ACTIVITY\_CLEAR\_TASK 的注释

    If set in an Intent passed to Context.startActivity(), this flag will cause any existing task that would be associated with the activity to be cleared before the activity is started. That is, the activity becomes the new root of an otherwise empty task, and any old activities are finished. This can only be used in conjunction with FLAG_ACTIVITY_NEW_TASK.
    如果在传递给 Context.startActivity() 的 Intent 中设置，此标志将导致在活动启动之前清除与该活动关联的任何现有任务。 也就是说，该活动成为空任务的新根，并且所有旧活动都已完成。 这只能与 FLAG_ACTIVITY_NEW_TASK 结合使用。

其实看到这里已经大概知道bug出在哪里了，就是我们系统里面在碰到clear task这个flag的时候没有处理好，导致栈里的activity没有被清掉.

这里就可以写一个简单的demo来做测试

    val intent = Intent(this, ContactsActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)

[此处应该有 demo 演示视频](https://www.teambition.com/project/63d66827ce0b9d520e27531e/works/63d66827eebaab00177cc3dd/work/641d48cd3d2d89391332368f)

demo已经加在 [http://192.168.22.202:3000/VMOSLite/vmos-lite-case.git](http://192.168.22.202:3000/VMOSLite/vmos-lite-case.git)

建议大家改activitybug的时候都把自己的demo给加上，最终的愿想就是写一个activity stack 测试大全，以后改了相关的bug跑一遍测试用例就能测全，就不用担心改一个bug又出10个新的bug。

# 一点点总结

上面最容易形成套路的就是拿activity的生命周期与真机去对比，这一点颠扑不破，碰到这类问题没有头绪，就先去对比生命周期，总能在其上面找到一些端倪，从而形成突破口。