/**
 * 
 * frida -U -f com.google.android.youtube -l trace_all_method_of_class.js
 * frida -U -p $(adb shell ps -ef|grep line | awk '{print $2}') -l trace_all_method_of_class.js
 */
function log(text) {
    console.log(">>>" + text)
    var Log = Java.use("android.util.Log");
    Log.w("moe_frida_10295", text);
}

function logStrace() {
    console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
    Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new());
}

function getTid() {
    var Thread = Java.use("java.lang.Thread")
    return Thread.currentThread().getId();
}

function getTName() {
    var Thread = Java.use("java.lang.Thread")
    return Thread.currentThread().getName();
}

function funcHandler(methodName, retval) {
    if (methodName == "queryIntentActivities") {
        var ParceledListSlice = Java.use("android.content.pm.ParceledListSlice");
        var list = ParceledListSlice["getList"].apply(retval);

        log("queryIntentActivities list : " + list);

        var ListClass = Java.use("java.util.ArrayList");
        var iterator = ListClass["iterator"].apply(list);

        log("queryIntentActivities size : " + ListClass["size"].apply(list));

        var iteratorClass = Java.use("java.util.Iterator");
        // var hasNext = iteratorClass["hasNext"].apply(iterator);

        while (iteratorClass["hasNext"].apply(iterator)) {
            var noti = iteratorClass["next"].apply(iterator);
            log("IntentActivities = " + noti);
        }
        log("queryIntentActivities return : " + list + " hasNext : " + iteratorClass["hasNext"].apply(iterator));

        console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
    } else if (methodName == "getRunningAppProcesses") {
        var ListClass = Java.use("java.util.ArrayList");
        var iterator = ListClass["iterator"].apply(retval);

        log("getRunningAppProcesses size : " + ListClass["size"].apply(retval));

        var iteratorClass = Java.use("java.util.Iterator");
        // var hasNext = iteratorClass["hasNext"].apply(iterator);

        while (iteratorClass["hasNext"].apply(iterator)) {
            var procs = iteratorClass["next"].apply(iterator);
            var procsClass = Java.use("android.app.ActivityManager$RunningAppProcessInfo");
            log("procs = " + procs + " processName = " + procsClass.processName);
        }
        log("getRunningAppProcesses return : " + list + " hasNext : " + iteratorClass["hasNext"].apply(iterator));

    } else if (methodName == "getServices") {
        // var ServiceInfoClass = Java.use("android.app.ActivityManager$RunningServiceInfo");

        var ListClass = Java.use("java.util.ArrayList");
        var iterator = ListClass["iterator"].apply(retval);

        log("getServices size : " + ListClass["size"].apply(retval));

        var iteratorClass = Java.use("java.util.Iterator");
        // var hasNext = iteratorClass["hasNext"].apply(iterator);

        while (iteratorClass["hasNext"].apply(iterator)) {
            var itService = iteratorClass["next"].apply(iterator);
            var ComponentNameClass = Java.use("android.content.ComponentName");
            var RunningServiceInfoClass = Java.use("android.app.ActivityManager$RunningServiceInfo");
            
            // log("service: "+ ComponentNameClass["getClassName"].apply(itService.service));
            log("service: "+ itService);
            var itService2 = Java.cast(itService, RunningServiceInfoClass);
            log("service: service = "+ itService2.service);
            log("service: service = "+ itService2.service[0]);
            log("service: service2 = "+ itService2["h"]);
            // var serviceCmp = Java.cast(itService2["h"], ComponentNameClass);
            // log("service: service3 = "+ serviceCmp);
            // log("service: class.service = "+ RunningServiceInfoClass.service.apply[itService.service]);
        }
        console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
        // log("getServices return : " + list + " hasNext : " + iteratorClass["hasNext"].apply(iterator));
    }
}


function funcHandler2(methodName, retval) {
    if (methodName == "isLoggable") {
        return true;
    }
    return retval;
}


遍历classloader，寻找目标类hook
function replaceClassLoder(className){
    log("ClassLoader Replacing.");

    Java.enumerateClassLoaders({
        "onMatch": function(loader) {
            log("enumerateClassLoaders : " + loader);
            var origLoader = Java.classFactory.loader;
            try {
                // if(loader.findClass(className)) {
                Java.classFactory.loader = loader
                Java.classFactory.use(className);
            } catch (error) {
                log("moe_not_find_class");
                log(error);
                Java.classFactory.loader = origLoader;
            }
        },
        "onComplete": function() {
            log("ClassLoader Replace done.!");
        }
    });
}

function traceClass(clsname) {
    var target;
    try {
        target = Java.use(clsname);
    } catch (e1) {
        replaceClassLoder(clsname);
        try {
            target = Java.classFactory.use(clsname);
        } catch (e2) {
            log(e2)
        }
    }
    traceClassCommon(target)
}

// 递归hook父类
function traceClassForeachParent(clsname) {
    var target;
    try {
        target = Java.use(clsname);
    } catch (e1) {
        replaceClassLoder(clsname);
        try {
            target = Java.classFactory.use(clsname);
        } catch (e2) {
            log("traceClassForeachParent e2 : " + e2)
        }
    }
    try {
        while (target !== undefined) {
            if (target.$className == "android.app.Activity") {
                break;
            }
            traceClassCommon(target);
            target = target.$super;
        }
    } catch (e3) {
        log("traceClassForeachParent e3 : " + e3)
    }
    
}


function traceClassCommon(target) {
    var clsname = target.$className;
    try {
        log("clsname" + clsname + " target: " + target.class);
        var methods = target.class.getDeclaredMethods();
        log("methods : " + methods);
        methods.forEach(function (method) {
            var methodName = method.getName();
            if (typeof(target[methodName]) == 'undefined') {
                log("moe_err : methodName " + methodName + " is undefined")
                return;
            }
            if (methodName == 'getDetailFragment') {
                return;
            }
            var overloads = target[methodName].overloads;
            overloads.forEach(function (overload) {
                var proto = "(";
                overload.argumentTypes.forEach(function (type) {
                    proto += type.className + ", ";
                });
                if (proto.length > 1) {
                    proto = proto.substr(0, proto.length - 2);
                }
                proto += ")";
                log("hooking: " + clsname + "." + methodName + proto);
                overload.implementation = function () {
                    var args = "";
                    var tid = getTid();
                    var tName = getTName();
                    for (var j = 0; j < arguments.length; j++) {
                        args += arguments[j] + ", "
                    }
                    var start = (new Date()).valueOf();
                    log(tName + " " + clsname + "." + methodName + "(" + args + ") beforeInvoke");
                    var retval = this[methodName].apply(this, arguments);
                    funcHandler(methodName, retval);
                    log(tName + " " + clsname + "." + methodName + "(" + args + ") = " + retval + " afterInvoke cost " + ((new Date()).valueOf() - start) + " ms");
                    return retval;
                }
            });
        });
    } catch (e) {
        log("'" + clsname + "' hook fail: " + e)
    }
}

function traceMethod(clsname, methodName) {
    try {
        var target = Java.use(clsname);
        log("clsname" + clsname + " target: " + target.class);
        var overloads = target[methodName].overloads;
        overloads.forEach(function (overload) {
            var proto = "(";
            overload.argumentTypes.forEach(function (type) {
                proto += type.className + ", ";
            });
            if (proto.length > 1) {
                proto = proto.substr(0, proto.length - 2);
            }
            proto += ")";
            log("hooking: " + clsname + "." + methodName + proto);
            overload.implementation = function () {
                var args = [];
                var tid = getTid();
                var tName = getTName();
                for (var j = 0; j < arguments.length; j++) {
                    args[j] = arguments[j] + ""
                }

                var retval = this[methodName].apply(this, arguments);
                funcHandler2();
                log(tName + " " + clsname + "." + methodName + "(" + args + ") = " + retval);
                return retval;
            }
        });
    } catch (e) {
        log("'" + clsname + "' hook fail: " + e)
    }
}


function baseTrace() {
    traceClass("android.app.IActivityManager$Stub$Proxy");
    traceClass("android.app.IActivityTaskManager$Stub$Proxy");
    traceClass("android.content.pm.IPackageManager$Stub$Proxy");
    traceClass("android.view.IWindowSession$Stub$Proxy");
    traceClass("android.net.IConnectivityManager$Stub$Proxy");
    traceClass("com.android.internal.telephony.ITelephony$Stub$Proxy");
    traceClass("android.accounts.IAccountManager$Stub$Proxy");
    traceClass("android.content.ContentProvider");
    traceClass("android.app.admin.IDevicePolicyManager$Stub$Proxy");
    traceClass("android.app.IActivityClientController$Stub$Proxy");
    traceClass("android.app.INotificationManager$Stub$Proxy");
    traceClass("android.app.job.IJobScheduler$Stub$Proxy");
    traceClass("android.media.IAudioService$Stub$Proxy");
    traceClass("com.android.internal.telephony.ISub$Stub$Proxy");
    traceClass("android.content.ContentProviderProxy");
    traceClass("android.content.ContentProvider$Transport");
    traceClass("com.android.internal.view.IInputMethodManager$Stub$Proxy");
    traceClass("android.view.accessibility.IAccessibilityManager$Stub$Proxy");
    traceClass("android.content.ContentResolver");
    traceClass("android.os.storage.IStorageManager$Stub$Proxy");
    // traceMethod("android.os.storage.StorageManager", "getStorageVolume");
    traceClass("com.android.providers.media.MediaProvider");
    // traceClass("com.google.android.apps.photos.localmedia.ui.LocalPhotosActivity");
    traceClass("android.hardware.display.IDisplayManager$Stub$Proxy");
    traceClass("android.app.Instrumentation")
    traceClass("com.android.server.content.SyncManager");
    traceClass("android.os.IUserManager$Stub$Proxy");
    traceClass("android.content.IContentService$Stub$Proxy");
    // traceClass("android.app.ActivityThread");
           // traceMethod("android.util.Log")
        // traceClass("android.app.Activity");

}
function lineTrace() {
    traceClass("android.app.LoadedApk");
    traceClass("android.app.ContextImpl");
    traceClass("com.vlite.sdk.logger.AppLogger");
    traceClass("com.vlite.sdk.reflect.MethodDef");
}
function messengerTrace() {
    traceClass("com.facebook.push.fcm.FcmListenerService");
    traceClass("com.google.firebase.iid.FirebaseInstanceIdReceiver");
    traceClass("com.google.firebase.messaging.FirebaseMessagingService");
    traceClass("com.facebook.rti.push.service.FbnsService");
}

function teamsTrace() {
    traceClass("com.microsoft.skype.teams.views.activities.InCallShareContentActivity");
    traceClass("com.microsoft.skype.teams.views.activities.BaseActivity");
    traceClass("com.microsoft.skype.teams.views.activities.InCallActivity");
    traceClass("com.microsoft.teams.chats.views.activities.ChatsActivity");
    traceClass("com.microsoft.teams.calling.views.activities.BaseCallActivity");
}

function zoomTrace() {
    traceClass("com.zipow.videobox.ConfService");
    traceClass("com.zipow.videobox.conference.ui.ZmFoldableConfActivity");
    traceClass("us.zoom.uicommon.fragment.n$c");
    traceClass("com.zipow.videobox.ConfActivityNormal");
    traceClass("com.zipow.videobox.conference.ui.ZmConfPipActivity");
}

function contactTrace() {
    traceClass("com.android.providers.contacts.ContactsProvider2");
    traceClass("com.android.providers.contacts.AbstractContactsProvider");

    traceClass("com.android.contacts.activities.DialtactsActivity");
    traceClass("com.android.contacts.activities.ContactDetailActivity");

    traceClass("com.android.contacts.activities.ContactInfoFragment");
}

function whatsappTrace() {
    traceClass("com.whatsapp.contact.sync.ContactsSyncAdapterService");
    traceClass("X.1Em"); // 
    traceClass("android.content.AbstractThreadedSyncAdapter");
}

function slidesTrace() {
    traceClass("android.security.keystore.AndroidKeyStoreCipherSpiBase");
    traceClass("ugs");
    traceClass("ugt");
    traceClass("azc$a");
    traceClass("azc");
    traceClass("vax");
    traceClass("ugr$a");
    traceClass("android.app.SharedPreferencesImpl");
    traceClass("android.app.SharedPreferencesImpl$EditorImpl");
}



function printClassLoder(){
    log("printClassLoder.");

    Java.enumerateClassLoaders({
        "onMatch": function(loader) {
            log("printClassLoder : " + loader);
        },
        "onComplete": function() {
            log("ClassLoader onComplete!");
        }
    });
}

if (Java.available) {
    Java.perform(function () {
        // printClassLoder()
        // base
        // baseTrace();

        // twitter
        // traceClassForeachParent("com.twitter.app.main.MainActivity");
        // traceClass("android.app.Activity");
        traceClassForeachParent("com.vlite.unittest.activities.ContactsActivity");

    });
}