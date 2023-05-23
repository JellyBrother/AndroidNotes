# Vlite中使用xposed

## 为什么选择这个做分享?

*   这个机器没有root,我用不了frida,给我换个Bug吧! 
    
*   害,用不了Frida要用xposed好不习惯啊,脑壳痛,给我换个Bug吧!
    
*   这个bug刚点进去就闪退了,用Frida来不及附加.我还是换个Bug吧!
    

## Xposed原理

Android 运行的核心是 zygote 进程，所有 app 的进程都是通过 zygote fork 出来的。通过替换 system/bin/ 下面的 app\_process 等文件，相当于替换了 zygote 进程，实现了控制手机上的所有 APP。基本原理是修改了 ART/Davilk 虚拟机，将需要 hook 的函数注册为 Native 层函数，当执行到该函数时，虚拟机会先执行 Native 层函数，然后执行 Java 层函数，这样完成 hook。

## 在Vlite中使用Xposed Hook

### 如何使用?

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBPLwgD7OP13/img/f518dba5-82b0-4a2d-a78c-ea23fcfa194a.png)

    public class mytestxposed  extends AppXposedHook {
        @Override
        public String getPackageName() {
            return "com.mytestxposed";
        }
    
        @Override
        public void onMakeApplication(Application app) {
            try {
                ··· 代码 ···
            } catch (Throwable e) {
                XposedBridge.log("ZZh Xposed异常 "+ e);
            }
        }
    }

1.  打开vmos-lite-sdk\vmos-lite-client\src\debug\java\com\vlite\sdk\debug.java
    
2.  打开registerAppXposedInterface函数中的某个class的注释
    
3.  在被打开注释的类中有一个 getPackageName函数,将返回的包名改为要hook 的包名,然后就可以在该类的onMakeApplication函数开始写Xposed hook代码了.
    

### 基础的使用操作

#### 被Hook代码

    abstract class Animal{
        int anonymoutInt = 500;
        public abstract void eatFunc(String value);
    }
    
    public class HookDemo {
        private String Tag = "HookDemo";
        private static  int staticInt = 100;
        public  int publicInt = 200;
        private int privateInt = 300;
    
        public HookDemo(){
            this("NOHook");
            Log.d(Tag, "HookDemo() was called|||");
        }
    
        private HookDemo(String str){
            Log.d(Tag, "HookDemo(String str) was called|||" + str);
        }
    
        public void hookDemoTest(){
            Log.d(Tag, "staticInt = " + staticInt);
            Log.d(Tag, "PublicInt = " + publicInt);
            Log.d(Tag, "privateInt = " + privateInt);
            publicFunc("NOHook");
            Log.d(Tag, "PublicInt = " + publicInt);
            Log.d(Tag, "privateInt = " + privateInt);
            privateFunc("NOHook");
            staticPrivateFunc("NOHook");
    
            String[][] str = new String[1][2];
            Map map = new HashMap<String, String>();
            map.put("key", "value");
            ArrayList arrayList = new ArrayList();
            arrayList.add("listValue");
            int ret = complexParameterFunc("NOHook", str, map, arrayList);
            Log.d(Tag,"ret PublicInt = " + ret);
    
            repleaceFunc();
            anonymousInner(new Animal() {
                @Override
                public void eatFunc(String value) {
                    Log.d(Tag, "eatFunc(String value)  was called|||" + value);
                    Log.d(Tag, "anonymoutInt =  " + anonymoutInt);
                }
            }, "NOHook");
    
            InnerClass innerClass = new InnerClass();
            innerClass.InnerFunc("NOHook");
        }
    
        
        public void publicFunc(String value){
            Log.d(Tag, "publicFunc(String value) was called|||" + value);
        }
    
        private void privateFunc(String value){
            Log.d(Tag, "privateFunc(String value) was called|||" + value);
        }
    
        static private void staticPrivateFunc(String value){
            Log.d("HookDemo", "staticPrivateFunc(Strin value) was called|||" + value);
        }
    
        private int complexParameterFunc(String value, String[][] str, Map<String,String> map, ArrayList arrayList)
        {
            Log.d("HookDemo", "complexParameter(Strin value) was called|||" + value);
            return publicInt;
        }
    
        private void repleaceFunc(){
            Log.d(Tag, "repleaceFunc will be replace|||");
        }
    
        public void anonymousInner(Animal dog, String value){
            Log.d(Tag, "anonymousInner was called|||" + value);
            dog.eatFunc("NOHook");
        }
    
        private void hideFunc(String value){
            Log.d(Tag, "hideFunc was called|||" + value);
        }
    
        class InnerClass{
            public int innerPublicInt = 10;
            private int innerPrivateInt = 20;
            public InnerClass(){
                Log.d(Tag, "InnerClass constructed func was called");
            }
            public void InnerFunc(String value){
                Log.d(Tag, "InnerFunc(String value) was called|||" + value);
                Log.d(Tag, "innerPublicInt = " + innerPublicInt);
                Log.d(Tag, "innerPrivateInt = " + innerPrivateInt);
            }
        }
    }

#### 修改类中的私有静态变量staticInt

    public void onMakeApplication(Application app) 
    {
    	try {
            ClassLoader classLoader = app.getClassLoader();
            final Class<?> clazz = XposedHelpers.findClass("com.mytestxposed.HookDemo", classLoader);
        	XposedHelpers.setStaticIntField(clazz, "staticInt", 88);
    	} catch (Throwable e) {
                XposedBridge.log("ZZh Xposed异常 "+ e);
        }
    }
    

#### Hook 有参构造函数和无参构造函数

    public void onMakeApplication(Application app) 
    {
    	try {
            ClassLoader classLoader = app.getClassLoader();
            final Class<?> clazz = XposedHelpers.findClass("com.mytestxposed.HookDemo", classLoader);
        	//Hook无参构造函数
            XposedHelpers.findAndHookConstructor(clazz, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("Haha, HookDemo constructed was hooked" );
                }
            });
            
            //Hook有参构造函数，修改参数
            XposedHelpers.findAndHookConstructor(clazz, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = "Haha, HookDemo(str) are hooked";
                }
            });
    	} catch (Throwable e) {
                XposedBridge.log("ZZh Xposed异常 "+ e);
        }
    }

#### Hook 一般函数

    public void onMakeApplication(Application app) 
    {
    	try {
            ClassLoader classLoader = app.getClassLoader();
            final Class<?> clazz = XposedHelpers.findClass("com.mytestxposed.HookDemo", classLoader);
            //Hook public方法，修改参数
        	XposedHelpers.findAndHookMethod(clazz, "publicFunc", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // 修改参数
                    param.args[0] = "Haha, publicFunc are hooked";
                    
                    // 修改该对象内的属性值
                    XposedHelpers.setIntField(param.thisObject, "publicInt", 188);
                    XposedHelpers.setIntField(param.thisObject, "privateInt", 288);
                    
                    // 主动调用 让hook的对象本身去执行流程
                    Method md = clazz.getDeclaredMethod("hideFunc", String.class);
                    md.setAccessible(true);
                    XposedHelpers.callMethod(param.thisObject, "hideFunc", "Haha, hideFunc was hooked");
    
                    //实例化对象，然后再调用HideFunc方法
                    Constructor constructor = clazz.getConstructor();
                    XposedHelpers.callMethod(constructor.newInstance(), "hideFunc", "Haha, hideFunc was hooked");
                }
            });
    
            //Hook私有方法privateFunc，修改参数
            // 也可以下面这种写法
            //XposedHelpers.findAndHookMethod("com.example.xposedhooktarget.HookDemo", clazz.getClassLoader(), "privateFunc", String.class, new XC_MethodHook()
            XposedHelpers.findAndHookMethod(clazz, "privateFunc", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = "Haha, privateFunc are hooked";
                }
            });
    
            //Hook私有静态方法staticPrivateFunc, 修改参数
            XposedHelpers.findAndHookMethod(clazz, "staticPrivateFunc", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = "Haha, staticPrivateFunc are hooked";
                }
            });
    	} catch (Throwable e) {
                XposedBridge.log("ZZh Xposed异常 "+ e);
        }
    }
    
    

#### Hook 复杂参数函数

    public void onMakeApplication(Application app) 
    {
    	try {
            ClassLoader classLoader = app.getClassLoader();
            final Class<?> clazz = XposedHelpers.findClass("com.mytestxposed.HookDemo", classLoader);
        	//Hook复杂参数函数complexParameterFunc
            Class fclass1 = XposedHelpers.findClass("java.util.Map", classLoader);
            Class fclass2 = XposedHelpers.findClass("java.util.ArrayList", classLoader);
            XposedHelpers.findAndHookMethod(clazz, "complexParameterFunc", String.class,
                    "[[Ljava.lang.String;", fclass1, fclass2, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = "Haha, complexParameterFunc are hooked";
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    // 修改返回值
                    int result = 398;
                    param.setResult(result);
                }
            });
    
            //Hook方法, anonymousInner， 参数是抽象类，先加载所需要的类即可
            Class animalClazz  = classLoader.loadClass("com.mytestxposed.Animal");
            XposedHelpers.findAndHookMethod(clazz, "anonymousInner", animalClazz, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("HookDemo This is test");
                    param.args[1] = "Haha, anonymousInner are hooked";
                }
            });
    	} catch (Throwable e) {
                XposedBridge.log("ZZh Xposed异常 "+ e);
        }
    }

#### 替换被Hook的方法

    public void onMakeApplication(Application app) 
    {
    	try {
            ClassLoader classLoader = app.getClassLoader();
            final Class<?> clazz = XposedHelpers.findClass("com.mytestxposed.HookDemo", classLoader);
            //Hook私有方法repleaceFunc, 替换打印内容
            XposedHelpers.findAndHookMethod(clazz, "repleaceFunc", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    Log.d("HookDemo", "Haha, repleaceFunc are replaced");
                    return null;
                }
            });
    	} catch (Throwable e) {
                XposedBridge.log("ZZh Xposed异常 "+ e);
        }
    }

#### Hook内部类

    public void onMakeApplication(Application app) 
    {
    	try {
            ClassLoader classLoader = app.getClassLoader();
            final Class<?> clazz = XposedHelpers.findClass("com.mytestxposed.HookDemo", classLoader);
            //Hook匿名类的eatFunc方法，修改参数，顺便修改类中的anonymoutInt变量
            XposedHelpers.findAndHookMethod("com.mytestxposed.HookDemo$1", clazz.getClassLoader(),
                    "eatFunc", String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = "Haha, eatFunc are hooked";
                            XposedHelpers.setIntField(param.thisObject, "anonymoutInt", 499);
                        }
                    });
    
            //Hook内部类InnerClass的InnerFunc方法，修改参数，顺便修改类中的innerPublicInt和innerPrivateInt变量
            final Class<?> clazz1 = XposedHelpers.findClass("com.mytestxposed.HookDemo$InnerClass", classLoader);
            XposedHelpers.findAndHookMethod(clazz1, "InnerFunc", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = "Haha, InnerFunc was hooked";
                    XposedHelpers.setIntField(param.thisObject, "innerPublicInt", 9);
                    XposedHelpers.setIntField(param.thisObject, "innerPrivateInt", 19);
                }
            });
    	} catch (Throwable e) {
                XposedBridge.log("ZZh Xposed异常 "+ e);
        }
    }

#### 乘凉

AppXposedHook类封装好的函数

    XposedHelpers.findAndHookMethod("com.google.firebase.iid.FirebaseInstanceIdReceiver", classLoader, "b", Context.class.getName(), " com.google.android.gms.cloudmessaging.CloudMessage", new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            final Object message = param.args[0];
            final Field[] declaredFields = message.getClass().getDeclaredFields();
    
            final Intent intent = (Intent) getFieldValueBasedOnClass(declaredFields, Intent.class, message);
            AppLogger.dt(AppXposedDebug.TAG, "FirebaseInstanceIdReceiver.b " + (intent == null ? "null" : bundleToString(intent.getExtras())));
        }
    });

    # hook指定类中的函数
    protected void hookMemberToPrintArgsAndStack(Application app, final String className, String methodName, Class... parameterTypes);
    hookMemberToPrintArgsAndStack(app, "HookDemo", "publicFunc",String.class);
    
    protected void hookMemberToPrintArgsAndStack(Application app, final String className, String methodName, boolean printRet, Class... parameterTypes);
    hookMemberToPrintArgsAndStack(app, "HookDemo", "publicFunc",String.class, true);
    

### Hook时机

部分堆栈如下

VirtualClient.bindApplication ->VirtualClient.bindApplicationInMainThread -> VirtualClient.callAppDebug -> Debug.onMakeApplication -> mytestxposed.onMakeApplication

在bindApplicationInMainThread函数中调用了虚拟应用的makeApplication

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/NybEnBPLwgD7OP13/img/9fd24b47-f3bf-417e-a606-df1d10b1eb69.png)

makeApplication中实例化了Application

    @UnsupportedAppUsage
        public Application makeApplication(boolean forceDefaultAppClass,
                Instrumentation instrumentation) {
            if (mApplication != null) {
                return mApplication;
            }
    
            ···
    
            try {
                ···
                //newApplication里面调用了app.attach函数,attach里面调用了attachBaseContext函数
                app = mActivityThread.mInstrumentation.newApplication里面(
                        cl, appClass, appContext);
                appContext.setOuterContext(app);
            } catch (Exception e) {
                if (!mActivityThread.mInstrumentation.onException(app, e)) {
                    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                    throw new RuntimeException(
                        "Unable to instantiate application " + appClass
                        + " package " + mPackageName + ": " + e.toString(), e);
                }
            }
            mActivityThread.mAllApplications.add(app);
            mApplication = app;
    
            if (instrumentation != null) {
                try {
                    // 这里面调用了 app.onCreate();函数
                    instrumentation.callApplicationOnCreate(app);
                } catch (Exception e) {
                    if (!instrumentation.onException(app, e)) {
                        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                        throw new RuntimeException(
                            "Unable to create application " + app.getClass().getName()
                            + ": " + e.toString(), e);
                    }
                }
            }
    
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
    
            return app;
        }

可以知道VirtualClient.callAppDebug的调用在这个makeApplication之后，就可知在应用的Application的attachBaseContext 和Oncreate跑完之后