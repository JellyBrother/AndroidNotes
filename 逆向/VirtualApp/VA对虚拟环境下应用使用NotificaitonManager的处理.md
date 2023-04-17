# VA对虚拟环境下应用使用NotificaitonManager的处理

#### VA Hook初始化

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/32M9qPKZp8xmn015/img/533a4d05-ee56-48b9-bb01-45fc7901ede8.png)

在VA中所有的clientApp进程都是在宿主应用的AndroidManifest.xml中注册的StubProvider，clientApp在启动前VA会加载一个空闲的StubProvider来带起一个进程，而后在该进程中加载clientApp中的组件。

在Android的世界中的应用在启动任何组件之前需要先初始化应用的Applicaiton，所有应用都有一个默认的Applicaiton类，也可以自定义继承application并在manifest.xml中注册，并且多进程的应用中注册的进程启动时都会加载调用一次应用Application，因此应用对他所包含的任何进程有完全控制能力。

VA通过自定义应用的application并在attachBaseContext函数中调用VirtualCore.get().startup()对进程的环境进行初始化，其中包括对android各种ManagerService的hook，以及底层libc的文件系统函数的hook。

      综上条件，因此所有的clientApp在进程启动阶段都会调用VA的Application完成环境初始化。

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/32M9qPKZp8xmn015/img/08c553a8-1bf3-4f09-988d-4a4531b90bbb.png)

在VA的初始化函数中已经完成所有的JAVA Hook和NATIVE HOOK，我们可以看到startup中调用了invocationStubManager.init()函数，init中进行的了大量的API Hook，每一组函数的hook都被封装成了一个MethodInvocationProxy类，如所有通知的api都被封装到了NotificationManagerStub。我们以NotificationManagerStub类为例，主要包含三个功能：

1.  构造函数获取原service接口并生成用来hook用的代理实例，用来代替原本对AMS中函数的调用，都会走到这个代理实例。
    

    super(new MethodInvocationStub<IInterface>(NotificationManager.getService.call()));

2.  inject()函数执行实际的注入操作，通过反射将本地保存Service binder的变量替换成上面构造函数生成的代理实例。
    

    public void inject() throws Throwable {
        NotificationManager.sService.set(getInvocationStub().getProxyInterface());
    	  Toast.sService.set(getInvocationStub().getProxyInterface());
    }

3.  onBindMethods()添加代理函数，所有的代理函数都被封装成了一个MethodProxy类，在这里将所有实现好的MethodProxy类实例保存到一个HashMap中，实际执行到代理实例的时候会根据函数名查表，如果函数没有被添加进来，则跳转到原本的service调用。
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/32M9qPKZp8xmn015/img/7bf6df13-05de-4884-8291-1a7d60a11f0d.png)

#### NotificationManagerService Hook

一般的我们发送一个通知的步骤，需要先通过getSystemService获得一个NotificationManager，然后通过manager.notify接口将创建好的Notification类通知发送给系统。geSystemService获得的这个NotificationManager类只是在我们本地进程的一个代理类，最终也是将通知发送给系统进程中的NotificationManagerService进行处理。

    //创建manager
    NotificationManager manager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    //创建通知
    NotificationCompat.Builder nc = new NotificationCompat.Builder(current)
                            .setContentTitle("it my test notify")
                            .setContentText("this is my test notify")
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setSmallIcon(android.R.drawable.ic_dialog_alert);
    //发送通知
    manager.notify(1,nc.build());

由于需要和系统进程跨进程通信，NotificationManager类中存在一个静态变量sService保存着系统进程中NotificationManagerService的binder，静态变量不论创建多少个实例静态变量只存在一份，也就是说所有从getSystemService获取的NotificationManager类中的sService都是一样的，VA通过反射直接修改NotificationManager.sService的值为自己实现的代理对象既可实现对client app通知的hook。（在VirtualCore.startup() -> invocationStubManager.init() -> injectInternal() 进行了大量的初始化hook，其中NotificationManagerStub对应通知服务的hook）

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/32M9qPKZp8xmn015/img/5aee7e3b-5a24-4b42-b1e3-23c9473e00c5.png)

#### Notification 处理

manager.notify()接口在处理完notification的一些检查后最终会调用service.enqueueNotificaitonWithTag()，于是来到VA的hook代码处，VA的代码中主要有三处对通知的参数进行了处理，处理完成后最终传递给原本的系统接口函数

*   其中id的处理函数实际上就是直接返回原来的id没有处理；
    
*   tag则是简单字符串拼接：packageName + ":" + tag + "@" + userId;
    
*   第三处的addNotification,保存通知的信息用于调用VA的接口可以统一清除所有VA中app的通知。主要的通知信息处理在第二处的dealNotification函数中。
    

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/32M9qPKZp8xmn015/img/ef6d256c-3fd7-4909-a837-432a254f84ed.png)

dealNotification又会继续调用resolveRemoteViews对notificaiton和notificaiton.publicVersion进行处理，并且getAppContext()会通过virtualApp宿主的context调用createPackageContext()获得真实的目标App的context。

#### resolveRemoteViews 修复

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/32M9qPKZp8xmn015/img/69910bf0-558c-4136-a91d-6192a05ed436.png)

1.  fixNotificationRemoteViews() 修复notification中的tickerView/contentView等一些remoteView, 因为在notifyAsUser函数中会将notification中的所有的remoteView剔除，这里重新创建一个notification取出默认的remoteview修复
    
2.  修复notification中icon的mObj1和mString1，mOjb1保存的是获取icon图像资源的对象：context.getResoure()，mString1保存的是icon所属的包名。
    
3.  notification.icon 是通知在的状态栏中显示的一个小图标（没有下拉状态下的状态栏），不过好像目前已经被弃用了。
    
4.  修复notification中所有remoteViews中的applicationInfo信息，以及notification.EXTRA\_BUILDER\_APPLICATION\_INFO中保存的applicationinfo。
    

  不过经测试VA中对Notification中remoteViews的处理修复其实是非必要的，直接删除这部分代码也是可以正常发送通知的，所以这部分代码可能只是做一些VA个性化的样式以及为了兼容，如：替换状态栏图标等。

#### 唯一重要的处理

实际上在VA的enqueueNotificationWithTag Hook函数代码中唯一不能缺少的处理其实只有一句：args\[0\] = getHostPkg();就是替换为io.virtualApp的包名，因为实际上clientApp运行的进程属于VA，包名不一致会导致AMS报错终止clientApp的进程：

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/32M9qPKZp8xmn015/img/17c6154d-6f9d-4bbb-b562-983ff1ada515.png)

  查看android源码中的enqueueNotificationInternal()，可以看到有一处检查pkg包名是否和调用者的appInfo中包名一致，因此必须要将pkg替换为io.virtualApp，包名不一致会出现如图错误：

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/32M9qPKZp8xmn015/img/f57ee5d8-cb4b-457d-85c8-61cf518b9e9e.png)