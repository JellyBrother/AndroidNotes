# VirtualApp虚拟环境下Activity Hook分析

#### 前言

在VA中一个简单的包含一个Activity的APP，从启动进程到完成Activity Hook并加载渲染界面总共要涉及到两个Hook类ActivityManagerStub和HCallbackStub，可以在VirtualCore.startup()中找到，其中ActivityManagerStub负责AMS的接口Hook，HCallbackStub负责本地进程中ActivityThread.Handler的hook，在开始之前可能需要先大概了解Android Activity的启动流程，以及Android中常用的Binder、Handler等特性。

#### Android Activity

总体而言，我们知道不管是c还是java在进程启动之初都有一个main函数是整个程序的入口，而Android app进程的main函数就是ZygoteInit中的main，进程启动后ZygoteInit继续加载运行ActivityThread.main的代码，此时运行的依然是程序的主线程，main函数首先会通过系统的ServiceManager获取AMS的binder（AMS运行在系统进程中），调用AMS中的attachApplication函数并传入一个自己的binder： AppThread，其作用是用来给AMS回调本地进程的函数(binder函数运行在binder线程中），AMS之后会通过AppThread回调向主线程Handler发送消息，包括加载应用的Application、Activity等；而同时主线程就会进入Looper.loop()循环准备接受处理AMS发送过来的Handler消息。

也就是说AMS只负责管理，实际加载组件时由AMS封装应用包名、ApplicationInfo、Activity等参数为msg发送到本地，本地进程中ActivityThread负责接收msg并处理，实例化相应的Application/Activity等组件，调用组件的onCreate(）函数等生命周期函数。

1.  ActivityThread.main() 关键代码
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/8a0b95d6-3857-487d-a668-5fed70566e10.png)

2.  AMS源码中ActivityManagerService.java   attachApplication() -> attachApplicationLocked() 
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/cb0fbcf5-1e9a-4d97-b04b-5c9164f3928c.png)

3.  ApplicationThread.bindApplicaton()函数准备好数据后通过Handler发送msg，主线程取出msg并处理
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/d3e5c4da-33c3-4202-b1a6-4f095665431d.png)

4.  跟进sendMessage可以看到处理消息的handler类为：ActivityThread.mH；
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/8c7bd7ab-4f14-48a1-a778-6349c64b5d5f.png)

#### VA Hook AMS

了解完大概的Android Activity启动流程再来看VA对Activity的Hook和启动流程，VA的架构与系统的其实有一些的类似，他也有实现了自己的一套AMS、ServiceManager等服务运行在一个单独的进程中，并且用自己的VAMS来实现对clientApp的管理以及为了保证App正常运行必要的一些参数的替换，在VA启动app进程流程上与系统不同的是：

1.  系统中startActivity会通过socket通知zygote fork一个子进程，子进程的main入口函数啥zygoteInit，启动完成后zygoteInit会调用ActivityThread，ActivityThread.attach()会自己去调用AMS.attachApplicatoin将自己的ApplicationThread传过去，AMS准备好应用LaunchActivity需要的各种参数包括包名、ApplicationInfo、Provider等，通过ApplicationThread回调用本地进程中ActivityThread去处理；
    
2.  VA的启动一个新进程是通过调用预先注册的StubProvider来带起一个新进程，stubProvider初始化vClientImpl类以及当前进程的pid返回给VA，vClientImpl类继承自Binder作用是用来给VA获取clientApp进程的信息，其中最主要的是VA会通过vClientImpl获取进程的AppThread，于是VAMS获得了clientApp进程的AppThread后就可以向ActivityThread发送LAUNCH\_ACTIVITY、LAUNCH\_SERVICE等任务，类似于AMS也会持有进程的appThread，VA也会将app进程的appThread保存起来方便以后调用。
    

那么新的进程启动了如何在该进程中运行clientApp的Activity呢？

答案是VA会同时注册一个stubActivity和一个stubProvider 在同一个进程，启动stubProvider相应注册在同一进程的stubActivity即可。于是AMS就会向该进程发送LAUNCH\_ACTIVITY的消息加载该stubActivity（ASMS会保存进程的ApplicationThread），但是ActivityThread中的消息处理类Handler已经被VA给Hook了（在进程启动时初始化application类时会初始化VA环境），于是走到了VA的LAUNCH\_ACTIVITY处理代码，原本的stubActivity就不会被加载，intent信息被替换成了clientApp的，clientApp就的Activity就被加载到了屏幕上。

整体启动流程如图：

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/e0645a94-bc1e-4ede-a62b-bad89e300177.png)

#### VA实现代码跟进

说了这么多理论，我们可以看一下VA实际的实现代码，首先要启动一个VA中的app有两种方式：一种是在VA中点击clientApp的图标，一种是在app中调用startActivity()启动另一个app。前者VA会直接调用VAMS中的接口，而后者则需要被Hook原来的系统接口到VAMS，由于第一种情况比较直接这里我们讨论第二种情况，接下来根据startActivity接口的走向跟进VA的代码。

安卓四大组件由AMS管理，AMS运行在单独的进程中，为了和AMS通信会将AMS的binder代理保存在ActivityManager.IActivityManagerSingleton.mInstance变量中，所有对AMS的调用都会通过这个binder，VA就是通过反射修改mInstance这个变量指向自己的代理实例，并且在com.lody.virtual.client.hook.proxies.am.MethodProxies.java中实现了大量函数来代替原本AMS的系统调用，最终会调用到va server进程中的VAMS来代替管理组件或修改替换avtivity信息。

*   VA的java反射Hook实现位置：com.lody.virtual.client.hook.proxies.am.ActivityManagerStub.java
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/a0fcb1e1-6aa1-4ac7-bbd9-5434acfd54ec.png)

*   VA Hook进行初始化位置：
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/e7f00ab7-34d5-40ad-a240-e31f46a0af2c.png)

*   所以在app中调用startActivity时会来到VA的startActivity（代理函数都被封装为MethodProxy类），VAM startActivity实现：
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/51dad385-8a25-4bb2-9c19-f1047c26fb37.png)

*   VAMS startActivity实现：
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/91bd2815-3d74-4e75-af32-6c30b3921680.png)

*   startActivityInNewTaskLocked
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/21b0d070-a944-4968-b8ad-cd62c7a1cc8b.png)

*   startActivityProcess
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/15332abe-be4d-4a57-afc4-daa03ad29bef.png)

从上面代码可以看出VA启动一个activity由VA来创建新的进程，新的进程启动方式通过调用va预注册的stubProvider，解析provider后系统会启动该进程，用Provider的原因前面已经说了，VAMS需要获得clientApp的binder与之通信。

接着查看performStartProcessLocked的代码，首先会通过queryFreeStubProessLocked()获取现在空闲的vpid，这个vpid就是预埋的stubAcitivty/Provider的编号，接着调用ProviderCall.call()解析vpid对应的stubProvider的url，调用对应的provider从而使系统创建新进程（stubProvider注册为新进程），

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/44067b60-7598-483d-8a18-939bfeffce8d.png)

走完performStartProcessLocked此时app的进程已经启动，启动时会完成java部分hook，因为VA的初始化代码放在了Application中，启动新的进程需要初始化Application类（native hook在LAUNCH\_ACTIVITY时执行)。

接着可以回到我们刚刚的startActivityInNewTaskLocked函数，这里调用了系统的startActivity并传入一个Intent来，该intent component指向stubActivity，然后在stubActivity中VA的hook handler代码会再启动目标App的Activity，stubActivity跟上面的stubProvider注册在同一个进程所以会直接运行在该进程中；

通过上面代码可以看到启动stubActivity的Intent是在startActivityProcess函数新建的一个intent，该intent的Component被设置为stubActivity，并在Extras里保存我们需要启动的app的intent,调用系统调用启动该intent，直接通过调试我们可以看到targetIntent的内容

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/f0f713e3-3d98-4232-a5d3-778b3ac23e67.png)

在启动stubActivity时，因为在之前进程初始化时注入了进程所在ActivityThread中的Handler的Handler.Callback,所以当系统启动一个Activity时在ApplicationThread通过Handler发送LAUNCH\_ACTIVITY消息会先通过VirtualApp中的HCallbackStub.handleMesage()。Hook代码如图：

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/6241e141-cde9-4bdc-aabc-4cbd48c25f21.png)

之后AMS的消息就来到了VA自己的handleLaunchActivity()，handleLaunchActivity主要的处理就是取出之前放入intent.mExtras的intent拿到真正需要启动的Activity的intent，反射替换msg.obj中的intent为取出的intent，intent保存在msg.obj.intent，接下来就是将修改后的msg交给系统的ActivityThread的Handler去处理。

*   取出从stubActivity的intent取出属于app的intent：
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/c0e0f270-4915-4bce-b04d-a8e1c438b1da.png)

*   跟进StubActivityRecord可以看到取出intent的操作：
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/9142e168-5674-4b11-bdf7-1e1f1447db8b.png)

*   最后将intent替换原来的Intent，如图：
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/47f8527a-0aa9-4f91-839c-d469f8fbafdd.png)

*   VA的handleLaunchActivity返回后，回来到系统的msg处理函数，通过调试跟进代码：
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/a279616f-caac-44ec-9e1d-1c76fe06e5c8.png)

*   观察Handler的dispatchMessage很明显存在mCallback.handleMessage返回值的判断，返回false就进入了系统ActivityThread的处理代码。
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/8ABmOoPVGJbAlawZ/img/31995535-e285-4032-8892-7e45572a6e6c.png)

#### 思考

不使用预注册stubActivity的方式是否可以实现在VA进程加载clientApp的Activity