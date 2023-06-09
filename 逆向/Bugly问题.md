# Bugly问题

### 无效问题判断

1.1、没有应用名，并且使用的是1.3.36及以下版本的，视为无效问题。确认是低版本独立包问题，改成无效问题。

1、Process: com.facebook.orca, PID: 27004出现空指针，但是没有显示包名，使用的sdk版本在1.3.36以下。 

2、在此之前，还有其他崩溃，但是几次崩溃的应用的进程id居然是一样的。 

3、而最新的sdk，崩溃后的进程id是改变的，应该是老版本sdk的问题。

4、gbox的日志com.gbox.android.activities.LaunchAppActivity#prepareEnv

prepare environment \[mainPackageMinVersionCode=1325, channel=unknown, packageName=com.gbox.com.microsoft.teams, sdkVersionCode=1336, sdkVersionName=1.3.36\]

prepare environment \[mainPackageMinVersionCode=1325, channel=unknown, packageName=com.gbox.com.whatsapp, sdkVersionCode=10613, sdkVersionName=1.0.6-beta13

appPackageName=com.whatsapp, versionCode=1430, versionName=1.4.30\_11.51.0704, \]

// 因为在1.4.10以前是本地依赖打包的方式 sdkVersionCode实际是独立包自身的versionCode

// 1.4.10开始远程依赖打包的方式 独立包也增加了versionCode 所以直接取独立包自身的versionCode

### java.lang.SecurityException  invalid package "com.google.android.gms" for uid 10211

    java.lang.SecurityException: invalid package "com.google.android.gms" for uid 10173
    2
    at android.os.Parcel.createExceptionOrNull(Parcel.java:2390)
    3
    at android.os.Parcel.createException(Parcel.java:2374)
    4
    at android.os.Parcel.readException(Parcel.java:2357)
    5
    at android.os.Parcel.readException(Parcel.java:2299)
    6
    at android.location.ILocationManager$Stub$Proxy.addGnssMeasurementsListener(ILocationManager.java:1555)
    7
    at java.lang.reflect.Method.invoke(Native Method)
    8
    at o.Annotation.invoke(SourceFile:177)
    9
    at java.lang.reflect.Proxy.invoke(Proxy.java:1006)
    10
    at $Proxy11.addGnssMeasurementsListener(Unknown Source)
    11
    at android.location.LocationManager$GnssMeasurementsListenerManager.registerService(LocationManager.java:3021)
    12
    at android.location.LocationManager$GnssMeasurementsListenerManager.registerService(LocationManager.java:3010)
    13
    at android.location.AbstractListenerManager.addInternal(AbstractListenerManager.java:156)
    14
    at android.location.AbstractListenerManager.addInternal(AbstractListenerManager.java:129)
    15
    at android.location.AbstractListenerManager.addListener(AbstractListenerManager.java:107)
    16
    at android.location.LocationManager.registerGnssMeasurementsCallback(LocationManager.java:2236)
    17
    at android.location.LocationManager.registerGnssMeasurementsCallback(LocationManager.java:2194)
    18
    at bulz.b(:com.google.android.gms@214858028@21.48.58 (100400-425989856):8)
    19
    at bunh.k(:com.google.android.gms@214858028@21.48.58 (100400-425989856):3)
    20
    at bunh.j(:com.google.android.gms@214858028@21.48.58 (100400-425989856):0)
    21
    at bukx.b(:com.google.android.gms@214858028@21.48.58 (100400-425989856):2)
    22
    at bunh.k(:com.google.android.gms@214858028@21.48.58 (100400-425989856):3)
    23
    at bunt.a(:com.google.android.gms@214858028@21.48.58 (100400-425989856):38)
    24
    at bunt.run(:com.google.android.gms@214858028@21.48.58 (100400-425989856):2)
    25
    Caused by: android.os.RemoteException: Remote stack trace:
    26
    at com.android.server.location.CallerIdentity.fromBinder(CallerIdentity.java:98)
    27
    at com.android.server.location.CallerIdentity.fromBinder(CallerIdentity.java:86)
    28
    at com.android.server.location.gnss.GnssManagerService.addGnssDataListenerLocked(GnssManagerService.java:387)
    29
    at com.android.server.location.gnss.GnssManagerService.addGnssMeasurementsListener(GnssManagerService.java:503)
    30
    at com.android.server.location.LocationManagerService.addGnssMeasurementsListener(LocationManagerService.java:2622)

无效的包名校验，原生包通过gbox请求gms的api，传入的是原生包的包名，需要使用gbox的包名才行。

需要反射修改addGnssMeasurementsListener方法的入参。

对比安卓各个版本的差异：

\* 安卓12和安卓13： \* void addGnssMeasurementsListener(in GnssMeasurementRequest request, in IGnssMeasurementsListener listener, String packageName, @nullable String attributionTag, String listenerId); \* 安卓11： \* boolean addGnssMeasurementsListener(in GnssRequest request, in IGnssMeasurementsListener listener, String packageName, String featureId); \* 安卓8-安卓10： \* boolean addGnssMeasurementsListener(in IGnssMeasurementsListener listener, in String packageName);

在LocationManagerStub中修改：

addMethodProxy("addGnssMeasurementsListener", **new** MethodHandler.HandleFirstLeftPkgMethodProxy());

### 基线和指标

1、以现有的崩溃数据作为稳定性基线。---用当前最新数据的平均值做基线，会不断变化

2、异常崩溃率阈值：7天的平均崩溃率不能超过基线的15%，30天的平均崩溃率不能超过10%

3、严重异常崩溃率阈值：7天的平均崩溃率不能超过基线的80%，30天的平均崩溃率不能超过70%

新版本崩溃的标准：

稳定性基线：2.1%

7天异常崩溃率阈值：2.5% ，30天异常崩溃率阈值：2.4%

7天严重异常崩溃阈值：4.0%,30天严重异常崩溃阈值：3.8%    

整体崩溃的标准：

稳定性基线：3.7%

7天异常崩溃率阈值：4.5% ，30天异常崩溃率阈值：4.3%

7天严重异常崩溃阈值：7.0%,30天严重异常崩溃阈值：6.7%    

### 版本日志分析

6906-08 10:50:53 4701 4851 E falco\_29: prepare environment \[mainPackageMinVersionCode=1325, appPackageName=com.google.android.apps.docs, versionCode=1420, versionName=1.4.20\_11.51.0704, channel=unknown, packageName=com.gbox.com.google.android.apps.docs, sdkVersionCode=10530, sdkVersionName=1.0.5-beta30\]

手机要安装gbox和独立包，独立包启动由gbox上报的日志。

gbox本身克隆是不会上报日志的。

versionCode独立包版本号，versionName独立包版本名称，packageName包名，sdkVersionCode使用sdk的版本号

独立包的版本号和gbox的版本号不是对齐的，不对齐的时候也会有各种问题。

独立包的sdk和gbox的sdk不是通用的，所以也可能不一样。