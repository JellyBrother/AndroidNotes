# VirtualApp对Activity和Service的处理

# 一、VritualApp 介绍

VirtualApp是一款运行在Android系统的沙盒App，VirtualApp内部维护着一个虚拟空间，在虚拟空间内可以任意安装、启动、卸载APP，每一个App处于独立的运行环境。

沙盒内部与外部隔离，沙盒内部的应用程序会感觉运行在沙盒中和运行在真实环境没什么区别，而沙盒App可以方便管理沙盒内部的应用程序，提高应用程序的安全性，保护用户的隐私。

APP想要在Android手机中运行，必须经过安装后被系统承认才可以运行，正常情况下是无法运行的。

VirtualApp通过欺骗Android系统，把沙盒内部的应用程序营造出已经安装了的假象就可以运行了。而欺骗的过程就是对Java框架层Hook，对底层方法和IO重定向native Hook。

# 二、VritualApp 初步认识

## （1）AndroidManifest.xml

AndroidManifest.xml 是 Android 应用程序的清单文件，它是 Android 应用程序开发的重要组成部分。

Android 应用程序启动时，系统会读取应用程序的 AndroidManifest.xml 文件，以获取应用程序的基本信息、组件、权限等配置信息。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/0c1d711d-1ac5-4c29-b4cf-a952618569ad.png)

清单文件中 Application 标签的 name 字段指明 VApp 是自定义的 Application 类，这个类的的生命周期会贯穿整个应用程序的生命周期，Android在程序初始化和销毁时调用Application重写的函数，可以用于资源的初始化和释放。

清单文件中 Activity 标签的 name 字段 和 intent-filter 字段指明 APP 启动的 Activity 根组件，它会第一个显示到屏幕上。

## （2）Java Framework 初始化 Hook

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/ee58d34a-0f6c-4fd8-acca-59ebfd2c9ee8.png)

应用程序启动时，如果指明了自定义的 Application 类，就会分先后的调用attachBaseContext和onCreate进行初始化。

attachBaseContext函数中调用VirtualCore的内部函数进行虚拟环境初始化。

    VirtualCore.get().startup(base);

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/b4e44634-f0bd-4502-bce8-c9dd78fc9371.png)

invocationStubManager.init()函数内保存需要Hook的目标，在injectAll函数内全部Hook。

服务和沙盒内的应用程序的框架层都会被Hook。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/8a592ab3-9220-44af-91f4-675d6a7a4618.png)

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/42ea9d7e-162b-4a84-8a77-8de1fa7aa7be.png)

## （3）Java Framework 通知 Hook 案例

**通知的简要流程**：

1.  应用程序可以调用NotificationManagerCompat类的notify发起通知，该类提供了跨版本的通知支持。
    
2.  NotificationManagerCompat类会将通知请求传递给系统的NotificationManager服务，服务会将通知请求封装成一个NotificationRecord对象。
    
3.  NotificationManager服务会调用enqueueNotificationWithTag()方法将NotificationRecord对象加入到通知队列，并传递对象到NotificationManagerService服务类，服务会负责处理所有通知。
    

**VirtualApp 通知处理流程**：

enqueueNotificationWithTag函数是被Hook的函数之一，客户端发起通知时只会调用Hook后的函数做一些处理，处理之后再发起通知。

1.  判断是否是VirtualApp发送的通知，如果是VirtualApp的通知则直接使用未Hook前的通知管理器发送通知，若是沙盒内客户端发送的通知，就把参数暂时保存到变量。
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/cd695fc2-7f65-477a-83d4-b993139c06ea.png)

2.  调用dealNotificationId/Tag对通知ID和通知TAG做处理。
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/b6e6cb94-fecd-4001-9898-142dd29ae92b.png)

通知Id，对于能否发起一个通知影响不大，所以VirtualApp并未做处理。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/072f661e-d090-4dda-8121-77ab69984473.png)

通知Tag，为了区分沙盒内部与外部环境下同一个应用程序的TAG，此处做了字符串拼接处理，只要和外面的应用程序的TAG不一样就行。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/44d5985b-f025-4daa-9276-b4acdb510824.png)

3.  调用dealNotification 主要对系统通知做了一些处理。
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/e43ddf7d-016d-41b9-8d11-a5d7ec754c50.png)

VirtualApp 库中，存在两个不同的类实现 dealNotification 函数的情况，分别为NotificationCompatCompatV21和NotificationCompatCompatV14。

这是因为 Android 系统在不同版本上的通知处理方式存在差异。在 Android 5.0 及以上版本中，使用NotificationCompatCompatV21类来兼容这些新的通知特性。在 Android 5.0 以下版本中，通知处理采用了旧的通知样式，因此需要使用NotificationCompatCompatV14类来兼容这些旧的通知特性。

4.  最后一步，也是最主要的一部，通过调用替换包名发起通知。
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/f14a9d96-0541-4003-b22a-b3348fccedeb.png)

沙盒内的应用程序并没有真正安装到Android上，所以无法发起通知。

VirtualApp是安装再Android上的，把通知相关的字段和沙盒内的应用程序发起通知的字段设置为一致，发起人从沙盒内的应用程序改为VirtualApp就可以了。

## （4）Native Hook

**Native Hook 会面临的问题：**

假设从微信保存数据到本地存储器，位置是data/user/0/wechat/xxx目录。

如果 VirtualApp 运行的微信不对路径做修改，同样会访问data/user/0/wechat/xxx 目录，两者之间的文件管理方面并没有隔离可能会出错导致程序无法正常运行。

例如一起运行时微信A读取数据，微信B写入数据，导致数据不同步，所以需要对沙盒内部启动的应用程序进行IO重定位。

data/user/0/wechat/xxx

data/user/0/virtualapp/virtual/data/user/0/wechat/xxx

**沙盒内部应用程序IO重定位的时机：**

VirtualApp 启动内部应用程序时会Hook消息回调函数，当收到LAUNCH\_ACTIVITY消息后，会调用startIOUniformer函数进行IO重定位，而最终会调用重定位的函数是处于native层的函数startUniformer。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/1815e36a-6074-450c-88bd-946e4a73f6e5.png)

Android是基于Linux内核的开源操作系统，对于文件的操作Java层最终会使用Linux提供的文件操作函数。

如：mkdir、chmod、stat等。

**真实环境应用程序目录与沙盒内部应用程序目录的对比：**

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/211cc3f6-946c-4341-a726-b8392734e5c1.png)

# 三、四大组件的插桩与架构

*   Activity
    
*   Service
    

## （1）Activity

作为四大组件的之一的Activity同样会有很多需要处理的地方。

`startActivity`函数调用到虚拟服务进程，虚拟服务进程会保存所有的沙盒内部应用程序的信息。这种好处是可以对所有客户端进行管理还利用双进程的方式提升客户端运行速度。

> 服务端处理 Activity 流程  `VActivityManagerService.startActivity`   => `ActivityStack.startActivityLocked`   => `ActivityStack.startActivityInNewTaskLocked`   => `ActivityStack.startActivityProcess`

`startActivityProcess`函数返回一个`Intent`对象。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/9ba59ca5-e5a9-4d91-a26a-fcacf1c57b5f.png)

`Intent`可以用于启动一个新活动，所以`Intent`一定经过特殊的处理，有着描述用于沙盒内应用程序的启动信息。

`ActivityStack.startActivityProcess`分三部分分析

（1）调用`startProcessIfNeedLocked`函数中获取`ProcessRecord`。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/0115f003-0ec8-439b-8746-39181429a0f2.png)

1.1 检查是否可用的Activity小于三个，如果条件成立则结束所有客户端来释放空间。![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/d072f009-36fc-4690-bd9d-acbac06ba33a.png)

1.2 获取包的配置信息与应用程序的信息，如果是第一次拉起的应用程序则发送第一次启动的广播告知其他沙盒内的应用程序，然后序列化到文件。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/fbae0edf-b5eb-40e7-9833-5229ee98d595.png)

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/65bcd245-9fad-41fd-82e6-173bbb65a91d.png)

1.3 获取应用程序的uid和虚拟pid，虚拟pid通过寻找空闲的桩位置，其索引就是虚拟pid![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/5ec4aca4-21f7-4d14-83b8-96f6417ff7a8.png)

（2）`setClassName`只有在`Manifest.xml`文件中声明过的`Activity`才可以被创建出来，而Manifest.xml中留有大量的ActivityStub就是为了给此处使用。再把意图中包名与类名替换为宿主声明过的包名和类名。主要目的是使用狸猫换太子的方式启动一个`Activity`，在启动后马上还原客户端的意图内容，`setType`意图中保存原组件信息作为类型![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/4bda0a7f-0fed-4577-9e0d-04e2e1947c5e.png)

（3）`saveToIntent`函数内做的事情很简单，就是保存真正与外面应用程序相关的信息保存到容器中，通过 Hook 消息处理函数，客户端在那时取出内容对意图的内容进行还原，例如进程名等。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/bd75f1a6-4bbe-430b-9e36-75847507dd08.png)

最后调用`startActivity`，传入伪造的Intent

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/3adeec18-c217-4cfe-9883-3d4c6de4d3b0.png)

系统收到`Intent`，检查宿主的`Manifest`发现存在此`Activity`，随后fork出新进程，拉起一个`Activity`，然后在我们的`Callback`拦截后，恢复`Intent`。

`HCallbackStub` => `handleMessage` => `handleLaunchActivity`

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/22b49af8-5953-4dac-9f1f-07fd2559a085.png)![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/1d6568ce-dc9e-4d12-981c-7e50bc079cb3.png)

小结：`VirtualApp`在运行时会创建一个虚拟的`Activity`，将需要运行的应用程序的`Activity` 替换为虚拟`Activity`，然后通过`Binder`通信机制将真正的`Activity`运行在一个独立的进程中，以此来实现多进程运行。

---

## （2）Service

目标：熟悉VAService的启动流程  预备知识：虚拟服务进程是通过内容提供者间接拉起的进程，服务在内容提供者类的`onCreate`初始化。

> 虚拟服务进程的启动流程  `HomeActivity.onCreate`   => `HomePresenterImpl.start`   => `HomePresenterImpl.dataChanged`   => `AppRepository.getVirtualApps`   => `VirtualCore.getInstalledApps`   => `getService()`

宿主应用程序启动后，通过上面的一系列调用流程，最后调用`getService`访问服务，第一次拉起服务进程。 ![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/0b82640e-a6d8-4b43-8476-55605d0aa48e.png)

调用`IServerCache.query`获取`Binder`对象。而`IServerCache`对象是宿主应用程序启动时在`VirtualCore.startup`函数中动态申请的。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/7f2f6989-de5f-4bb4-80e3-f16ae2ad991d.png)

从图中可以发现，`query`函数调用了`ServiceManagerNative.getService`。   内部通过调用`getServiceFetcher().getService`获取`Binder`对象。

`getServiceFetcher` 函数中构建出`ProviderCall`对象，通过这个对象和键`\_VA\_|\_binder\_`获取 `Binder`。 而传入的参数为`Context`一个`SERVICE\_CP\_AUTH`常量，值为`virtual.service.BinderProvider`。 ![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/127011e3-f0d3-4a49-a98f-9178cd45694d.png)

`ProviderCall.call`函数内构建一个`Uri`，之后可以用来启动内容提供者。 ![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/d383a09f-0364-452e-a91e-df426bec82d1.png)

`ContentProviderCompat.call`函数内调用`crazyAcquireContentProvider`获取内容提供者对象。 ![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/1dedc9e4-4c6d-4d41-b273-020bd5033689.png)

`crazyAcquireContentProvider`函数调用`acrazyAcquireContentProvider`函数。 ![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/5b75447c-5333-4710-940d-717160204a75.png)

最后根据SDK版本终于调用了两个函数之一，获取到了内容提供者对象。 调用getContentResolver函数后，系统会自动根据`Uri`的`authorities`字段去寻找内容提供者。 ![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/b59f16b7-6f74-4e22-abc2-5e4c2f66d889.png)

`SERVICE\_CP\_AUTH`之前的这个常量值就是内容提供者`authorities`字段。   `Manifest.xml`中找到符合条件的内容提供者，根据`name`字段找到了被启动的类`BinderProvider`。 ![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/1444dc84-cf12-4b79-a3ec-2c2491ce3645.png)

访问一个内容提供者，如果内容提供者尚未创建，则系统会调用`onCreate()`方法创建它，并执行初始化代码。   `BinderProvider.onCreate()`被调用后在函数内调用`DaemonService.startup()`启动服务调用。

    public final class BinderProvider extends ContentProvider {
        //xxx
    
        @Override
        public boolean onCreate() {
            //xxx
            DaemonService.startup(context);
            //xxx
        }
    
        //xxx
    }

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBx8XJR3OP13/img/597166f7-1d82-4d25-8600-01c861107109.png)