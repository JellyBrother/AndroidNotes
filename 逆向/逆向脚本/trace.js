// 定义一个互斥量
const mutex = {
    isLocked: false,
    lock() {
      while (this.isLocked) {}
      this.isLocked = true;
    },
    unlock() {
      this.isLocked = false;
    },
};

class IObject{
    constructor(){
        this.TAG = "mFrida";
    }
}

class Trace extends IObject
{
    static LOGGING_ENABLED = true;

    constructor(){
        super();
        this.target = "";
        this.Thread = findClass("java.lang.Thread");
    }

    
    static log(text) {
        mutex.lock()
        console.log(">>> " + text)
        Java.use("android.util.Log").w(`${this.TAG}`, text)
        mutex.unlock()
    }
    
    static baseTrace() {
        TraceClass.doInvoke("android.app.IActivityManager$Stub$Proxy");
        TraceClass.doInvoke("android.app.IActivityTaskManager$Stub$Proxy");
        TraceClass.doInvoke("android.content.pm.IPackageManager$Stub$Proxy");
        // TraceClass.doInvoke("android.view.IWindowSession$Stub$Proxy");
        TraceClass.doInvoke("android.net.IConnectivityManager$Stub$Proxy");
        TraceClass.doInvoke("android.accounts.IAccountManager$Stub$Proxy");
        TraceClass.doInvoke("android.app.admin.IDevicePolicyManager$Stub$Proxy");
        TraceClass.doInvoke("android.app.IActivityClientController$Stub$Proxy");
        TraceClass.doInvoke("android.app.INotificationManager$Stub$Proxy");
        TraceClass.doInvoke("android.app.job.IJobScheduler$Stub$Proxy");
        TraceClass.doInvoke("android.media.IAudioService$Stub$Proxy");
    
        TraceClass.doInvoke("android.content.ContentResolver");
        TraceClass.doInvoke("android.content.ContentProvider");
        TraceClass.doInvoke("android.content.ContentProviderProxy");
        TraceClass.doInvoke("android.content.ContentProvider$Transport");
    
        TraceClass.doInvoke("android.app.Instrumentation");
        TraceClass.doInvoke("android.content.IContentService$Stub$Proxy");
        
        TraceClass.doInvoke("com.android.internal.view.IInputMethodManager$Stub$Proxy");
        TraceClass.doInvoke("android.view.accessibility.IAccessibilityManager$Stub$Proxy");
        TraceClass.doInvoke("android.os.storage.IStorageManager$Stub$Proxy");
        // traceMethod("android.os.storage.StorageManager", "getStorageVolume");
        TraceClass.doInvoke("com.android.server.content.SyncManager");
        TraceClass.doInvoke("com.android.providers.media.MediaProvider");
        TraceClass.doInvoke("com.android.internal.telephony.ISub$Stub$Proxy");
        TraceClass.doInvoke("com.android.internal.telephony.ITelephony$Stub$Proxy");
        // TraceClass.doInvoke("com.google.android.apps.photos.localmedia.ui.LocalPhotosActivity");
        TraceClass.doInvoke("android.hardware.display.IDisplayManager$Stub$Proxy");
        TraceClass.doInvoke("android.os.IUserManager$Stub$Proxy");
        // TraceClass.doInvoke("android.app.ActivityThread");
        // traceMethod("android.util.Trace.log");
        // TraceClass.doInvoke("android.app.Activity");
    
        /* -> 下载管理器 <- */
        // TraceClass.doInvoke("android.app.DownloadManager");
        // TraceClass.doInvoke("android.app.DownloadManager$Query");
        // TraceClass.doInvoke("android.app.DownloadManager$Request");
    }

    static findClass(className){
        var cls = null;
        try {
            cls = Java.classFactory.use(className);
        } catch (error) {
            Java.enumerateClassLoaders({
                "onMatch": (loader) => 
                {
                    if (cls == null) 
                    {
                        var origLoader = Java.classFactory.loader;
                        try 
                        {
                            Java.classFactory.loader = loader
                            cls = Java.classFactory.use(className);
                        }
                        catch (error) 
                        {
                            Java.classFactory.loader = origLoader;
                        }
                    }
                },
                "onComplete": () => 
                {
                }
            });
        }
        
        
        return cls;
    }

    static printStack(){
        var cls = Java.use("android.util.Log");
        var throwable = Java.use("java.lang.Throwable");
        Trace.log(cls.getStackTraceString(throwable.$new()));
    }

    static getTid()
    {
        return this.Thread.currentThread().getId();
    }
    static getName()
    {
        return this.Thread.currentThread().getName();
    }

    static printMethods(className)
    {
        const targetClass = findClass(className);
        if (targetClass == null) return;

        targetClass.class.getDeclaredMethods().forEach(function (method) {
            Trace.log("find method: " + className + "." + method.getName());
        });
    }

    static resolveClass(arg_className = "", arg_object = null) {
        try {
            let targetClass = findClass(arg_className);
            // 检查 class 是否为 null
            if (targetClass == null) return;
            let fields = targetClass.class.getDeclaredFields();
            let object = Java.cast(arg_object, targetClass);
            fields.forEach(function(field) {
                try {
                    field.setAccessible(true);
                    let fieldType = field.getType().getName();
                    let variableName = field.getName();
                    let variableValue = "";
                    if (object != null && field.get(object) != null) {
                        let obj = Java.cast(field.get(object), findClass(fieldType));
                        variableValue = obj.toString();
                    }
                    Trace.log(`Variable type: ${fieldType}, Variable name: ${variableName}, Variable value: ${variableValue}`);
                } catch (error) {
                    Trace.log(`Error: ${error}, Type: ${getJavaClassType(fieldType)}`);
                }
            });
        } catch (error) {
            Trace.log(`resolveClass error: ${error}`);
        }
    }
}

class TraceClass extends Trace
{
    constructor(target){
        super();
        this.target = target;
    }

    invoke() {
        const className = this.target;
        const targetClass = findClass(className);
        if (targetClass == null) return;

        // 遍历 method
        var methods = targetClass.class.getDeclaredMethods();
        methods.forEach(function (method) {
            Trace.log("member method:  " + method.getName());
            var methodName = method.getName();
            if (typeof(targetClass[methodName]) == 'undefined') {
                Trace.log("find method : methodName " + methodName + " is undefined")
                return;
            }
            Trace.log("find method:  " + methodName);
            
            var overloads = targetClass[methodName].overloads;
            overloads.forEach((overload) => {

                var proto = "(";
                overload.argumentTypes.forEach(function(type){
                    proto += type.className + ", ";
                });
                if (proto.length > 1) {
                    proto = proto.substr(0, proto.length - 2);
                }
                proto += ")";
                Trace.log(proto);

                Trace.log("hooking: " + className + "." + methodName + proto);
                overload.implementation = function () {
                    var args = [];
                    var tid = findClass("java.lang.Thread").currentThread().getId();
                    var tName = findClass("java.lang.Thread").currentThread().getName();
                    for (var j = 0; j < arguments.length; j++) {
                        args[j] = arguments[j] + ""
                    }
                    var start = (new Date()).valueOf();
                    Trace.log(tName + " " + className + "." + methodName + "(" + args + ") beforeInvoke");
                    // printStack();
                    var retval = this[methodName].apply(this, arguments);
                    Trace.log(tName + " " + className + "." + methodName + "(" + args + ") = " + retval + " afterInvoke cost " + ((new Date()).valueOf() - start) + " ms");
                    return retval;
                }
                Trace.log("\n");

            });

        });

        
        // Traverse inner classes and hook their methods
        // var innerClasses = targetClass.class.getDeclaredClasses();
        // innerClasses.forEach(function (innerClass) {
        //     var innerClassName = innerClass.getName();
        //     Trace.log("Traversing inner class: " + innerClassName);
        //     TraceClass.doInvoke(innerClassName);
        // });
        return;
    }

    static doInvoke(target)
    {
        new TraceClass(target).invoke()
    }
}

class TraceMethod extends Trace
{
    constructor(target, bfunc, efunc){
        super();
        this.target = target
        this.bfunc = bfunc
        this.efunc = efunc
        this.resolvePackageAndClass(target);
    }

    resolvePackageAndClass(target)
    {
        this.className = "";
        this.methodName = "";
        try {
            let string = new String(target);
        
            let lastIndex = string.lastIndexOf('.');
            let length = string.length;
        
            this.className = string.substring(0, lastIndex);
            if (lastIndex + 1 < length){
                this.methodName = string.substring(lastIndex + 1);
            }
        } catch (error) {
            this.Trace.log("resolvePackageAndClass:  " + error);
        }

    }

    show(target)
    {
        Trace.log(`{this.className}` + this.className)
        Trace.log(`{this.methodName}` + this.methodName)
        let cls = findClass(this.className)
        if (cls == null) return

        let idx = 0
        cls.class.getDeclaredMethods().forEach((method) => {
            if (method.getName() != this.methodName){
                return;
            }        
            Trace.log("idx:" + idx + "  method:" + method)
            idx++
        })
        return this
    }

    invoke()
    {
        const pthisObject = this;
        const className = this.className;
        const methodName = this.methodName;
        
        const targetClass = findClass(className);
        if (targetClass == null) return;
        
        targetClass.class.getDeclaredMethods().forEach((method) => {
            // Trace.log("method.getName():  " + method.getName());

            if (method.getName() != methodName) return;

            if (typeof(targetClass[methodName]) == 'undefined') {
                Trace.log("undefined methodName:  " + methodName + " is undefined")
                return;
            }
            Trace.log("hook method:  " + targetClass.class.getName() + "." + methodName);

            let overloads = targetClass[methodName].overloads;
            overloads.forEach(function(overload){
                // 获取参数原型
                let proto = "(";
                overload.argumentTypes.forEach((type) => {
                    proto += type.className + ", ";
                });
                if (proto.length > 1) {
                    proto = proto.substr(0, proto.length - 2);
                }
                proto += ")";

                // hook 函数
                overload.implementation = function() {
                    let args = [];
                    for (let i = 0; i < arguments.length; i++) {
                        args[i] = arguments[i] + "";
                    }

                    let param = { thisObject:this, args:arguments, apply:true, className:className, methodName:methodName };
                    try {
                        Trace.log("");
                        Trace.log("beforeInvoke");
                        Trace.log("thread id  : " + findClass("java.lang.Thread").currentThread().getId());
                        Trace.log("thread Name: " + findClass("java.lang.Thread").currentThread().getName());
                        Trace.log("method Name: " + className + "." + methodName);
                        Trace.log("arguments  : " +  "(" + args + ") ");
                        Trace.log("");

                        if (pthisObject.bfunc)
                        {
                            pthisObject.bfunc(param);
                        }
                    } catch (error) {
                        Trace.log(methodName + " beginInvoke:  " + error);
                    }

                    try
                    {
                        if (param.apply == true){
                            param.ret = this[methodName].apply(this, arguments);
                        }else{
                            Trace.log(methodName + " refuse call done.")
                        }
                    }
                    catch (error) 
                    {
                        Trace.log("The source caller's exception methodname:" + methodName + " apply:  " + error);
                        // throw error;// 抛异常把 独立包 抛没了
                        return null
                    }

                    try 
                    {
                        if (pthisObject.efunc)
                        {
                            pthisObject.efunc(param);
                        }

                        Trace.log("");
                        Trace.log("afterInvoke");
                        Trace.log("thread id  : " + findClass("java.lang.Thread").currentThread().getId());
                        Trace.log("thread Name: " + findClass("java.lang.Thread").currentThread().getName());
                        Trace.log("method Name: " + className + "." + methodName);
                        Trace.log("arguments  : " +  "(" + args + ") = " + param.ret);
                        Trace.log("");
                    } catch (error) 
                    {
                        Trace.log(methodName + " afterInvoke:  " + error);
                    }
                    // Trace.log("ret:  " + param.ret);
                    return param.ret;
                }
            });
        });
    }

    overload(idx)
    {
        const pthisObject = this;
        const className = this.className;
        const methodName = this.methodName;
        
        const targetClass = findClass(className);
        if (targetClass == null) {
            Trace.log("can't find class:" + className)
        }
        
        let index = 0
        targetClass.class.getDeclaredMethods().forEach((method) => {
            index = index + 1

            if (method.getName() != methodName) {
                return
                Trace.log("can't find method:" + methodName)
            }
            if (idx != index) {
                return
                Trace.log("can't find index:" + idx)
            }
            Trace.log("hook method:  " + targetClass.class.getName() + "." + methodName);
            targetClass[methodName].overloads[idx].implementation = function() {

                let args = [];
                for (let i = 0; i < arguments.length; i++) {
                    args[i] = arguments[i] + "";
                }
                let param = { thisObject:this, args:arguments, apply:true, className:className, methodName:methodName };
                try {
                    Trace.log("");
                    Trace.log("beforeInvoke");
                    Trace.log("thread id  : " + findClass("java.lang.Thread").currentThread().getId());
                    Trace.log("thread Name: " + findClass("java.lang.Thread").currentThread().getName());
                    Trace.log("method Name: " + className + "." + methodName);
                    Trace.log("arguments  : " +  "(" + args + ") ");
                    Trace.log("");
                    if (pthisObject.bfunc)
                    {
                        pthisObject.bfunc(param);
                    }
                } catch (error) {
                    Trace.log(methodName + " beginInvoke error:  " + error);
                }
                try
                {
                    if (param.apply == true){
                        param.ret = this[methodName].apply(this, arguments);
                    }else{
                        Trace.log(methodName + " refuse call done.")
                    }
                }
                catch (error) 
                {
                    Trace.log("The source caller's exception methodname:" + methodName + " apply:  " + error);
                    // throw error;// 抛异常把 独立包 抛没了
                    return null
                }
                try 
                {
                    if (pthisObject.efunc)
                    {
                        pthisObject.efunc(param);
                    }
                    Trace.log("");
                    Trace.log("afterInvoke");
                    Trace.log("thread id  : " + findClass("java.lang.Thread").currentThread().getId());
                    Trace.log("thread Name: " + findClass("java.lang.Thread").currentThread().getName());
                    Trace.log("method Name: " + className + "." + methodName);
                    Trace.log("arguments  : " +  "(" + args + ") = " + param.ret);
                    Trace.log("");
                } catch (error) 
                {
                    Trace.log(methodName + " afterInvoke error:  " + error);
                }
                // Trace.log("ret:  " + param.ret);
                return param.ret;
            }
        });
    }

    static doInvoke(target, bfunc=null, efunc=null)
    {
        new TraceMethod(target, bfunc, efunc).invoke();
    }

    static doOverload(target, idx, bfunc=null, efunc=null)
    {
        new TraceMethod(target, bfunc, efunc).overload(idx);
    }

    static doShow(target){
        new TraceMethod(target).show()
    }
}

class TraceConstructor extends Trace{
    constructor(target, bfunc, efunc){
        super();
        this.target = target
        this.bfunc = bfunc
        this.efunc = efunc
    }

    invoke()
    {
        const pthisObject = this;
        const className = this.target;
        
        const targetClass = findClass(className);
        if (targetClass == null) return;
        
        let overloads = targetClass.$init.overloads;

        if (typeof(overloads) == 'undefined') {
            Trace.log("undefined $init is undefined")
            return;
        }
        Trace.log("hook constructor:  " + targetClass.class.getName());

        overloads.forEach(function(overload){
            // 获取参数原型
            let proto = "(";
            overload.argumentTypes.forEach((type) => {
                proto += type.className + ", ";
            });
            if (proto.length > 1) {
                proto = proto.substr(0, proto.length - 2);
            }
            proto += ")";

            // hook 函数
            overload.implementation = function() {
                let args = [];
                for (let i = 0; i < arguments.length; i++) {
                    args[i] = arguments[i] + "";
                }

                let param = { thisObject:this, args:arguments, className:className };
                try {
                    Trace.log("");
                    Trace.log("beforeInvoke");
                    Trace.log("thread id  : " + findClass("java.lang.Thread").currentThread().getId());
                    Trace.log("thread Name: " + findClass("java.lang.Thread").currentThread().getName());
                    Trace.log("method Name: " + className);
                    Trace.log("arguments  : " +  "(" + args + ") ");
                    Trace.log("");
                    if (pthisObject.bfunc)
                    {
                        pthisObject.bfunc(param);
                    }
                } catch (error) {
                    Trace.log(className + " beginInvoke error:  " + error);
                }
                try
                {                        
                    param.ret = this.$init.apply(this, arguments);
                } 
                catch (error) 
                {
                    Trace.log("frida apply call error:  " + error);
                    return null;
                }
                try 
                {
                    if (pthisObject.efunc)
                    {
                        pthisObject.efunc(param);
                    }
                    Trace.log("");
                    Trace.log("afterInvoke");
                    Trace.log("thread id  : " + findClass("java.lang.Thread").currentThread().getId());
                    Trace.log("thread Name: " + findClass("java.lang.Thread").currentThread().getName());
                    Trace.log("method Name: " + className);
                    Trace.log("arguments  : " +  "(" + args + ") = " + param.ret);
                    Trace.log("");
                } catch (error) 
                {
                    Trace.log(className + " afterInvoke error:  " + error);
                }
                return param.ret;
            }
        });
    }

    static doInvoke(target, bfunc=null, efunc=null)
    {
        new TraceConstructor(target, bfunc, efunc).invoke();
    }
}

class RefStaticObject{
    constructor(cls, field){
        try {
            this.field = cls.class.getDeclaredField(field.getName())
            this.field.setAccessible(true)
        } catch (e) {
            // NoSuchFieldException
            throw e   
        }
    }

    get object(){
        let obj = null
        try {
            obj = this.field.get(null)
        } catch (e) {
            // Ignore
        }
        return obj
    }

    set object(newvalue){
        try {
            this.field.set(null, newvalue)
        } catch (e) {
            // Ignore
        }
    }
}

function log(text){
    Trace.log(text)
}

function findClass(className){
    var cls = null;
    try {
        cls = Java.classFactory.use(getJavaClassType(className));
    } catch (error) {
        Java.enumerateClassLoaders({
            "onMatch": (loader) => 
            {
                if (cls == null) 
                {
                    var origLoader = Java.classFactory.loader;
                    try 
                    {
                        Java.classFactory.loader = loader
                        cls = Java.classFactory.use(className);
                    }
                    catch (error) 
                    {
                        Java.classFactory.loader = origLoader;
                    }
                }
            },
            "onComplete": () => 
            {
            }
        });
    }
    
    if (cls == null){
        Trace.log("can't find classname: " + className);
    }
    return cls;
}

function resolveIntent(intent_in)
{
    Trace.log("")
    Trace.log("")
    Trace.log("")
    Trace.log("================== resoluationIntent ==================")
    try {
        let intent = Java.cast(intent_in, Java.use("android.content.Intent"))
        Trace.log("intent.getAction:  " + intent.getAction())
        Trace.log("intent.getType:  " + intent.getType())
        Trace.log("intent.getData:  " + intent.getData())
        Trace.log("intent.getFlags:  " + intent.getFlags().toString(16))
        Trace.log("intent.getScheme:  " + intent.getScheme())

        let component = intent.getComponent()
        Trace.log("intent.getComponent:  " + component)
        if (component != null){
            Trace.log("intent.getComponent.getPackageName:  " + component.getPackageName())
            Trace.log("intent.getComponent.getClassName:  " + component.getClassName())
        }

        Trace.log("intent.getSelector:  " + intent.getSelector())

        let categories = intent.getCategories()
        if (categories != null)
        {
            categories.toArray().forEach(function(category){
                Trace.log("intent.getCategory:  " + category)
            })
        }

        let bundle = intent.getExtras();
        if (bundle != null)
        {
            bundle.keySet().toArray().forEach(function(key){
                let value = bundle.get(key);
                if ("android.support.customtabs.extra.SESSION_ID" == key)
                {
                    // const pendingIntent = cast(value,"android.app.PendingIntent")
                    // Trace.log("PendingIntent.getIntent():" + pendingIntent.getIntent());
                    // // resolveIntent(pendingIntent.getIntent())
                }
                Trace.log("string key:  " + key + "  value:  " + value)
            });   
        }     
        
    } catch (error) {
        Trace.log("resoluationIntent catch:" + error)
    }
    Trace.log("================== resoluationIntent end ==================")
    Trace.log("")
    Trace.log("")
    Trace.log("")
}

function resoluationBundle(bundle_in){
    try {
        var bundle = Java.cast(bundle_in, Java.use("android.os.Bundle"));
        
        Trace.log("bundle class:  " + bundle.getClass());
        Trace.log("bundle:  " + bundle);

        bundle.keySet().toArray().forEach(function(key){
            Trace.log("" + key + "  value:  " + bundle.get(key));
        });
        
    } catch (error) {
        Trace.log("" + error + "catch");
    }
}

function getField(thisObject, clsName, fieldName, fieldType){
    try {        
        let rawField = findClass(clsName).class.getDeclaredField(fieldName);
        rawField.setAccessible(true);
        var object = rawField.get(thisObject)
        if (object != null){
            return Java.cast(object, findClass(fieldType));
        }
        return object;
    } catch (error) {
        Trace.log("getField error:  " + error);
    }
}

function setFieldBoolean(thisObject, clsName, fieldName, newvalue){
    try {        
        let rawField = findClass(clsName).class.getDeclaredField(fieldName);
        rawField.setAccessible(true);
        rawField.setBoolean(thisObject,newvalue)
    } catch (error) {
        Trace.log("setField error:  " + error);
    }
}

function setFieldObject(thisObject, clsName, fieldName, newvalue){
    try {        
        let rawField = findClass(clsName).class.getDeclaredField(fieldName);
        rawField.setAccessible(true);
        rawField.set(thisObject,newvalue)
    } catch (error) {
        Trace.log("setField error:  " + error);
    }
}
//Interceptor.attach(Module.findExportByName("libc.so", "open"), {
//    onEnter: function(args) {
//        console.Trace.log("open():", args[0].readCString())
//    },
//    onLeave: function(retval)
//    {
//    }
//});

/** 打印 mHistroy */

function getJavaClassType(typeName) {
    switch (typeName) {
      case "boolean":
        return "java.lang.Boolean";
      case "byte":
        return "java.lang.Byte";
      case "char":
        return "java.lang.Character";
      case "short":
        return "java.lang.Short";
      case "int":
        return "java.lang.Integer";
      case "long":
        return "java.lang.Long";
      case "float":
        return "java.lang.Float";
      case "double":
        return "java.lang.Double";
      default:
        return typeName;
    }
  }
  

function lookupHistroy(histroy)
{
    try {
        // 获取成员变量
        var mHistory_cast = Java.cast(histroy, findClass("android.util.SparseArray"));
        Trace.log("this.mHistory.size() = " + mHistory_cast.size());
        // Trace.log("this.mHistory = " + mHistory_cast);

        // 遍历 mArray 数组元素并打印
        for (var i = 0; i < mHistory_cast.size(); i++) {
            var taskRecord = Java.cast(mHistory_cast.valueAt(i), findClass("com.vlite.sdk.server.virtualservice.am.TaskRecord"));
            Trace.log("");
            Trace.log("");
            Trace.log("");
            Trace.log("TaskRecord[" + i + "]:  " + taskRecord);

            var activities = getField(taskRecord, "com.vlite.sdk.server.virtualservice.am.TaskRecord", 'activities', "java.util.ArrayList");
            // Trace.log("activities.length: " + activities.size());

            for (let index = 0; index < activities.size(); index++) {
                let class_ActivityRecord = findClass("com.vlite.sdk.server.virtualservice.am.ActivityRecord");
                let element = Java.cast(activities.get(index), class_ActivityRecord);
                Trace.log("");
                Trace.log("ActivityRecord[" + index + "]:  " + element);
                
                var fields = class_ActivityRecord.class.getDeclaredFields();
                fields.forEach(function(field){
                    try {
                        field.setAccessible(true);
                        var fieldType = field.getType().getName();
                        if (field.get(element) != null){
                            let obj = Java.cast(field.get(element), findClass(fieldType));
                            Trace.log("field:  " + field.getName() + "  value:  " + obj);
                        }
                    } catch (error) {
                        Trace.log("field:  " + field.getName() + "  error:  " + error + "  Type:" + fieldType);   
                    }
                });
                // let component = Java.cast(element.component, findClass("android.content.ComponentName"));
                // let marked = Java.cast(element.marked, findClass("java.lang.Boolean"));
                // const activityRecord = Java.cast(activities[index], findClass("com.vlite.sdk.server.virtualservice.am.ActivityRecord"));
            }

        }

    } catch (error) {
        Trace.log("" + error);
    }
}

function printHistory()
{
    Trace.log("func:  printHistory");
    try {
        // 获取 ActivityManagerService 实例
        var cls_VAMS = findClass("com.vlite.sdk.server.virtualservice.am.VirtualActivityManagerService");
        var mIntance = cls_VAMS.getDefault();

        // 获取 ActivityStack 对象
        var class_mActivityStack = findClass("com.vlite.sdk.server.virtualservice.am.ActivityStack");
        var field_mActivityStack = cls_VAMS.class.getDeclaredField("StateListAnimator");
        // var field_mActivityStack = cls_VAMS.class.getDeclaredField("TaskDescription");
        // var field_mActivityStack = cls_VAMS.class.getDeclaredField("mActivityStack");
        field_mActivityStack.setAccessible(true);
        var object_mActivityStack = Java.cast(field_mActivityStack.get(mIntance), class_mActivityStack);

        // 获取 mHistory 对象
        var class_mHistory = findClass("android.util.SparseArray");
        var field_mHistory = class_mActivityStack.class.getDeclaredField("AssistContent");
        // var field_mHistory = class_mActivityStack.class.getDeclaredField("LoaderManager");
        // var field_mHistory = class_mActivityStack.class.getDeclaredField("mHistory");
        field_mHistory.setAccessible(true);
        var object_mHistory = Java.cast(field_mHistory.get(object_mActivityStack), class_mHistory);

        lookupHistroy(object_mHistory);
    } catch (error) {
        Trace.log("printHistory:  " + error);
    }
}
/**
 * frida -U -f com.google.android.youtube -l trace_all_method_of_class.js
 * frida -U -p $(adb shell ps -ef|grep line | awk '{print $2}') -l trace_all_method_of_class.js
 * frida -UF -l ./hook.js
 * Trace.logcat -v color -v uid |grep 
 * adb shell pm path com.xxx
 */

/*
* 1.1 支持解析类，获取类的所有字段和值
* 1.2 支持反射某个类，实现主动调用
*/

function cast(object, type)
{
    try {
        return Java.cast(object, findClass(type))
    } catch (error) {
        throw error
    }
}

function divide() {
    throw new Error("Cannot divide by zero");
}

// boolean Java.available; // 指定当前进程是否装载了 Java VM，即 Dalvik 或 ART。
if (Java.available) {
    // ctrl + k + t 换肤
    Java.performNow(() => {
        Trace.log("Frida server version:  " + Frida.version);
        Trace.log("Frida heapsize:        " + Frida.heapSize.toString(16));
        Trace.log("AndroidVersion:        " + Java.androidVersion);


        Instagram()
    });
}

function Instagram()
{
    TraceMethod.doInvoke("X.0Ud.A02")
    
    // 2bX 1
    // 2Yt 1

    // TraceMethod.doInvoke("X.Liy.run", (param) => {
    // });
    // TraceMethod.doInvoke("X.2bX.run", (param) => {
    // });
    // TraceMethod.doInvoke("X.2Yt.run", (param) => {
    // });
    // TraceMethod.doInvoke("X.2Ya.run", null, (param) => {
    //     let cls_2Ya = findClass(param.thisObject.getClass().getName())
    //     log("cls_2Ya: " + cls_2Ya)
    //     log("cls_2Ya.class: " + cls_2Ya.class)
    //     let field_A00 = cls_2Ya.class.getDeclaredField("A00")
    //     field_A00.setAccessible(true)
    //     let object_A00 = field_A00.get(param.thisObject)
    //     log("this.A00(Exception)" + object_A00)
    // });

    // TraceMethod.doInvoke("X.1HO.onFinish", (param) => {        
    //     let field_A06 = param.thisObject.getClass().getDeclaredField("A06");
    //     field_A06.setAccessible(true);
    //     let object_A06 = field_A06.get(param.thisObject);

    //     let field_A07 = param.thisObject.getClass().getDeclaredField("A07");
    //     field_A07.setAccessible(true);
    //     let object_A07 = field_A07.get(param.thisObject);

    //     let cls_2YZ = findClass("X.2YZ");
    //     let Object_A06_cast = cls_2YZ.class.cast(object_A06);

    //     if (
    //         object_A07 == "IgApi fb/facebook_signup/"
    //     || object_A07 == "IgApi fxcal/sso_login/"
    //     ){
            
    //         log("object_A07:" + object_A07);
    //         log("Successful, set exception completed");
    //         let field_A00 = cls_2YZ.class.getDeclaredField("A00");
    //         field_A00.setAccessible(true);
    //         field_A00.set(Object_A06_cast, findClass("java.lang.Exception").$new("My Exception"));
    //     }

    //     // Trace.printStack()
    // })
}



function Zoom()
{
    TraceMethod.doInvoke("us.zoom.libtools.model.a.n", (param) => {
        Trace.printStack()
        Trace.log("this.g:" + param.thisObject.g())
    })

    TraceMethod.doInvoke("java.io.File.exists", null, (param) => {        
        Trace.printStack()
        let field_path = findClass(param.className).class.getDeclaredField("path");
        field_path.setAccessible(true)
        
        Trace.log("this.path:" + field_path.get(param.thisObject))
        Trace.log("this.ret:" + param.ret)
    })
    // 下载表情包进度
    // com.zipow.videobox.emoji.pt.a

    // 处理receive
    // com.zipow.videobox.emoji.ZmConfEmojiBroadCastReceiver
}
// 1. 没有32位真机
// 2. 64位也存在ANR

// 28.133669950470626
// 112.94271092123229
// 1