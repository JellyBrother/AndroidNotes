/**
 *  Date:2023.4.21
 *  Version 1.0
 *  
 *  Use:
 *  自启动: 
 * 
 *      frida -U -f 包名 -l 当前脚本名.js
 * 
 *  程序运行中启动:
 * 
 *      frida -UF -l 当前脚本名.js
 * 
 * 
 *  Contributors:
 *      LiGuoFeng
 *      YangTing
 *  ...
 *
 */


/**
 * 该脚本提供方法如下:
 * 工具相关:打印堆栈、打印 list 元素
 * 
 * 线程进程相关:Threadid、ThreadName、CallingPid
 * 
 * 数据处理:byte[]转字符串、byte[]转16进制字符串、Map对象转String、byte[]进行base64编码、string字符串转换为byte[]、将js object转Java String、
 * 
 * 动态加载Dex：java对象转JSON格式(gson)、java对象转JSON格式(r0gson)
 * 
 * 四大组件相关:hook Activity、service 等相关重要参数
 * 
 * 其他信息：获取ApplicationContext、Hook Intent方法、 Hook Uri 参数、替换类加载器
 *  
 */

//辅助堆栈打印方法，别调用
function LogPrint(log) {
    var theDate = new Date();
    var hour = theDate.getHours();
    var minute = theDate.getMinutes();
    var second = theDate.getSeconds();
    var mSecond = theDate.getMilliseconds();
 
    hour < 10 ? hour = "0" + hour : hour;
    minute < 10 ? minute = "0" + minute : minute;
    second < 10 ? second = "0" + second : second;
    mSecond < 10 ? mSecond = "00" + mSecond : mSecond < 100 ? mSecond = "0" + mSecond : mSecond;
    var time = hour + ":" + minute + ":" + second + ":" + mSecond;
    var threadid = Process.getCurrentThreadId();
    console.log("[" + time + "]" + "->threadid:" + threadid + "--" + log);
 
}
 
/**
 * 描述:打印堆栈
 * @param {*} name 
 */
function VMprintJavaStack(name) {
    Java.perform(function () {
        var Exception = Java.use("java.lang.Exception");
        var ins = Exception.$new("Exception");
        var straces = ins.getStackTrace();
        if (straces != undefined && straces != null) {
            var strace = straces.toString();
            var replaceStr = strace.replace(/,/g, " \n ");
            LogPrint("=============================" + name + " Stack strat=======================");
            LogPrint(replaceStr);
            LogPrint("=============================" + name + " Stack end======================= \n ");
            Exception.$dispose();
        }
    });
}

/**
 * 描述:获取当前线程ID 
 * 参数:无
 * @returns:当前线程ID
 */
var Thread = Java.use("java.lang.Thread")
function VMGetTid() {
    return Thread.currentThread().getId();
}
/**
 * 描述:获取当前线程名
 * 参数:无
 * @returns 当前线程名
 */
function VMGetTName(){
    return Thread.currentThread().getName();
}

/**
 * 描述:获取当前Binder的CallingPid
 * 参数:无
 * 
 */
function VMgetCallingPid(){
    var binder = Java.use("android.os.Binder");
    return binder.getCallingPid();
}

/**
 * frida js版本，byte[]数组转换为字符串
 * @param arr byte[]
 * @returns {string} 字符串
 */
function VMbyteToString(arr) {
    if (typeof arr === 'string') {
        return arr;
    }
    var str = '',
        _arr = arr;
    for (var i = 0; i < _arr.length; i++) {
        var one = _arr[i].toString(2),
            v = one.match(/^1+?(?=0)/);
        if (v && one.length == 8) {
            var bytesLength = v[0].length;
            var store = _arr[i].toString(2).slice(7 - bytesLength);
            for (var st = 1; st < bytesLength; st++) {
                store += _arr[st + i].toString(2).slice(2);
            }
            str += String.fromCharCode(parseInt(store, 2));
            i += bytesLength - 1;
        } else {
            str += String.fromCharCode(_arr[i]);
        }
    }
    return str;
}
    
/**
 * 描述：byte[]转换为hex String
 * @param arr byte[]
 * @returns {string} String
 */
function VMbytesToHex(arr) {
var str = '';
var k, j;
for (var i = 0; i < arr.length; i++) {
    k = arr[i];
    j = k;
    if (k < 0) {
        j = k + 256;
    }
    if (j < 16) {
        str += "0";
    }
    str += j.toString(16);
}
return str;
};
    
/**
 * 描述：Map对象转String
 * @param map
 * @returns {string}
 */
function VMmapToString(map) {
var keyset = map.keySet();
var it = keyset.iterator();
var str = "{";
while (it.hasNext()) {
    var keystr = it.next().toString();
    var valuestr = map.get(keystr);
    str += '"' + keystr + '":"' + valuestr + '",';
}
return str.trim(',') + "}";
}
    
/**
 * 描述：打印 list 元素
 * @param list
 * @returns {string}
 */
function VMprintList(list){
    for(var i=0;i<list.size();i++){
        console.log(list.get(i));
    }
}
    
/**
 * 描述:byte[]进行base64编码
 * @param bytes
 * @returns {*}
 */
function VMbyte2Base64(bytes) {
    var jBase64 = Java.use('android.util.Base64');
    return jBase64.encodeToString(bytes, 2);
}


//动态加载Dex文件


/**
 * 描述:java对象转JSON格式
 * 需要gson
 * @param javaObj
 * @returns {*}
 */
function VMobjectToJsonNeedGson(javaObj) {
    //手机有该dex文件，可把加载代码提出函数外调用
    //避免每次调用该方法都调用一次加载
    Java.openClassFile("/data/local/tmp/gson.dex").load();

    var gson = Java.use('com.google.gson.Gson').$new();
    return gson.toJson(javaObj);
}

/**
 * 描述:java对象转JSON格式
 * 需要r0gson.dex
 * @param javaObj
 * @returns {*} 
 */
function VMobjectToJsonNeedr0Gson(javaObj) {
    //手机有该dex文件，可把加载代码提出函数外调用
    //避免每次调用该方法都调用一次加载
    Java.openClassFile("/data/local/tmp/r0gson.dex").load();

    var gson = Java.use('com.r0ysue.gson.Gson').$new();
    return gson.toJson(javaObj);
}


/**
 * frida js脚本 string字符串转换为byte[]
 * @param str String
 * @returns {any[]} byte[]
 */
function VMstringToByte(str) {
    var bytes = new Array();
    var len, c;
    len = str.length;
    for (var i = 0; i < len; i++) {
        c = str.charCodeAt(i);
        if (c >= 0x010000 && c <= 0x10FFFF) {
            bytes.push(((c >> 18) & 0x07) | 0xF0);
            bytes.push(((c >> 12) & 0x3F) | 0x80);
            bytes.push(((c >> 6) & 0x3F) | 0x80);
            bytes.push((c & 0x3F) | 0x80);
        } else if (c >= 0x000800 && c <= 0x00FFFF) {
            bytes.push(((c >> 12) & 0x0F) | 0xE0);
            bytes.push(((c >> 6) & 0x3F) | 0x80);
            bytes.push((c & 0x3F) | 0x80);
        } else if (c >= 0x000080 && c <= 0x0007FF) {
            bytes.push(((c >> 6) & 0x1F) | 0xC0);
            bytes.push((c & 0x3F) | 0x80);
        } else {
            bytes.push(c & 0xFF);
        }
    }
    return bytes;
}


/**
 * 将 js object 转换成 Java String
 * @param res
 * @returns {any}
 */
function VMnewString(res) {
    if (null == res) {
        return null;
    }
    const String = Java.use('java.lang.String');
    return String.$new(res);
}


function parseObject(data) {
    try {
        const declaredFields = data.class.getDeclaredFields();
        let res = {};
        for (let i = 0; i < declaredFields.length; i++) {
            const field = declaredFields[i];
            field.setAccessible(true);
            const type = field.getType();
            let fdata = field.get(data);
            if (null != fdata) {
                if (type.getName() != "[B") {
                    fdata = fdata.toString();
                }
                else {
                    fdata = Java.array('byte', fdata);
                    fdata = JSON.stringify(fdata);
                }
            }
            // @ts-ignore
            res[field.getName()] = fdata;
        }
        return JSON.stringify(res);
    } catch (e) {
        return "parseObject except: " + e.toString();
    }

}

/**
 * 将  object 转换成 Java String
 * 需要gson Or r0gson
 * @param obj
 * @returns {String}
 */

function VMtoJSONString(obj) {
    if (null == obj) {
        return "obj is null";
    }
    let resstr = "";
    let GsonBuilder = null;
    try {
        GsonBuilder = Java.use('com.r0ysue.gson.Gson');
        log("gson" + GsonBuilder)
    } catch (e) {
        log("gson " + e)
        registGson();
        GsonBuilder = Java.use('com.r0ysue.gson.Gson');
        log("gson" + GsonBuilder)
    }
    if (null != GsonBuilder) {
        try {
            const gson = GsonBuilder.$new().serializeNulls()
                .serializeSpecialFloatingPointValues()
                .disableHtmlEscaping()
                .setLenient()
                .create();
            resstr = gson.toJson(obj);
        } catch (e) {
            log('gson.toJson', 'exceipt: ' + e.toString());
            resstr = parseObject(obj);
        }
    }

    return resstr;
}


/*
hook Android Activity相关重要参数
参数: 无
返回值:无
*/

function VMprintActivity(){

    var Activity=Java.use("android.app.Activity");
    Activity.finish.overload().implementation=function(){
        console.warn("Hooking android.app.Activity.finish() successful");
        this.finish();
    }

    Activity.finish.overload('int').implementation=function(int){
        console.warn("Hooking android.app.Activity.finish('int') successful");
        this.finish(int);
    } 

    Activity.startActivity.overload('android.content.Intent').implementation=function(p1){
        console.warn("Hooking android.app.Activity.startActivity(p1) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.startActivity(p1);
    }
    Activity.startActivity.overload('android.content.Intent', 'android.os.Bundle').implementation=function(p1,p2){
        console.warn("Hooking android.app.Activity.startActivity(p1,p2) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.startActivity(p1,p2);
    }

    Activity.startActivityForResult.overload('android.content.Intent', 'int').implementation=function(p1,p2){
        console.warn("Hooking android.app.Activity.startActivityForResult('android.content.Intent', 'int') successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.startActivityForResult(p1,p2);
        }

    Activity.startActivityForResult.overload('android.content.Intent', 'int', 'android.os.Bundle').implementation=function(p1,p2,p3){
        console.warn("Hooking android.app.Activity.startActivityForResult('android.content.Intent', 'int', 'android.os.Bundle') successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.startActivityForResult(p1,p2,p3);
    }

    Activity.startActivityForResult.overload('java.lang.String', 'android.content.Intent', 'int', 'android.os.Bundle').implementation=function(p1,p2,p3,p4){
        console.warn("Hooking android.app.Activity.startActivityForResult('java.lang.String', 'android.content.Intent', 'int', 'android.os.Bundle') successful, p1="+p2);
        console.log(decodeURIComponent(p2.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.startActivityForResult(p1,p2,p3,p4);
    }

    Activity.startService.overload('android.content.Intent').implementation=function(p1){
        console.warn("Hooking android.app.Activity.startService(p1) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        return this.startService(p1);
    }

    Activity.sendBroadcast.overload('android.content.Intent').implementation=function(p1){
        console.warn("Hooking android.app.Activity.sendBroadcast(p1) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.sendBroadcast(p1);
    }
    Activity.sendBroadcast.overload('android.content.Intent', 'java.lang.String').implementation=function(p1,p2){
        console.warn("Hooking android.app.Activity.sendBroadcast(p1,p2) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.sendBroadcast(p1,p2);
    }

    Activity.sendBroadcast.overload('android.content.Intent', 'java.lang.String', 'android.os.Bundle').implementation=function(p1,p2,p3){
        console.warn("Hooking android.app.Activity.sendBroadcast(p1,p2) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.sendBroadcast(p1,p2,3);
    }

    Activity.sendBroadcast.overload('android.content.Intent', 'java.lang.String', 'int').implementation=function(p1,p2,p3){
        console.warn("Hooking android.app.Activity.sendBroadcast(p1,p2) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.sendBroadcast(p1,p2,p3);
    }
}



/*
hook Android Service相关重要参数
参数: 无
返回值:无
*/

function VMprintService(){

    //hook Service methods
    var Service=Java.use("android.app.Service");
    Service.startActivity.overload('android.content.Intent').implementation=function(p1){
        console.warn("Hooking android.app.Service.startActivity(p1) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.startActivity(p1);
    }
    Service.startActivity.overload('android.content.Intent', 'android.os.Bundle').implementation=function(p1,p2){
        console.warn("Hooking android.app.Service.startActivity(p1,p2) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.startActivity(p1,p2);
    }

    Service.startService.overload('android.content.Intent').implementation=function(p1){
        console.warn("Hooking android.app.Service.startService(p1) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.startService(p1);
    }

    Service.sendBroadcast.overload('android.content.Intent').implementation=function(p1){
        console.warn("Hooking android.app.Service.sendBroadcast(p1) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.sendBroadcast(p1);
    }
    Service.sendBroadcast.overload('android.content.Intent', 'java.lang.String').implementation=function(p1,p2){
        console.warn("Hooking android.app.Service.sendBroadcast(p1,p2) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.sendBroadcast(p1,p2);
    }

    Service.sendBroadcast.overload('android.content.Intent', 'java.lang.String', 'android.os.Bundle').implementation=function(p1,p2,p3){
        console.warn("Hooking android.app.Service.sendBroadcast(p1,p2) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.sendBroadcast(p1,p2,3);
    }

    Service.sendBroadcast.overload('android.content.Intent', 'java.lang.String', 'int').implementation=function(p1,p2,p3){
        console.warn("Hooking android.app.Service.sendBroadcast(p1,p2) successful, p1="+p1);
        console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
        this.sendBroadcast(p1,p2,p3);
    }
}


/*
hook Android Context相关重要参数
参数: 无
返回值:无
*/
function VMprintContext(){
    //ContextWrapper
    var ContextWrapper=Java.use("android.content.ContextWrapper");
    ContextWrapper.startActivity.overload('android.content.Intent').implementation=function(p1){
    console.warn("Hooking android.content.ContextWrapper.startActivity(p1) successful, p1="+p1);
    console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
    this.startActivity(p1);
    }
    ContextWrapper.startActivity.overload('android.content.Intent', 'android.os.Bundle').implementation=function(p1,p2){
    console.warn("Hooking android.content.ContextWrapper.startActivity(p1,p2) successful, p1="+p1);
    getStackTrace();
    console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
    this.startActivity(p1,p2);
    }

    ContextWrapper.startService.overload('android.content.Intent').implementation=function(p1){
    console.warn("Hooking android.content.ContextWrapper.startService(p1) successful, p1="+p1);
    console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
    return this.startService(p1);
    }

    ContextWrapper.sendBroadcast.overload('android.content.Intent').implementation=function(p1){
    console.warn("Hooking android.app.Activity.sendBroadcast(p1) successful, p1="+p1);
    console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
    this.sendBroadcast(p1);
    }
    ContextWrapper.sendBroadcast.overload('android.content.Intent', 'java.lang.String').implementation=function(p1,p2){
    console.warn("Hooking android.content.ContextWrapper.sendBroadcast(p1,p2) successful, p1="+p1);
    console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
    this.sendBroadcast(p1,p2);
    }

    ContextWrapper.sendBroadcast.overload('android.content.Intent', 'java.lang.String', 'android.os.Bundle').implementation=function(p1,p2,p3){
    console.warn("Hooking android.content.ContextWrapper.sendBroadcast(p1,p2) successful, p1="+p1);
    console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
    this.sendBroadcast(p1,p2,3);
    }

    ContextWrapper.sendBroadcast.overload('android.content.Intent', 'java.lang.String', 'int').implementation=function(p1,p2,p3){
    console.warn("Hooking android.content.ContextWrapper.sendBroadcast(p1,p2) successful, p1="+p1);
    console.log(decodeURIComponent(p1.toUri(Java.use("android.content.Intent").URI_ALLOW_UNSAFE.value)));
    this.sendBroadcast(p1,p2,p3);
    }


}

/*
    hook Android Intent
    参数: 无
    返回值:无
*/
function VMprintIntent(){

    // hook Intent methods
    var Intent=Java.use("android.content.Intent");
    Intent.putExtra.overload('java.lang.String', 'java.lang.String').implementation=function(p1,p2){
        console.warn("Hooking android.content.Intent.putExtra('java.lang.String', 'java.lang.String') successful");
        // console.log("key ="+p1+",value ="+p2);
        // getStackTrace();
        return this.putExtra(p1,p2);
    }

    Intent.putExtra.overload('java.lang.String', 'java.lang.CharSequence').implementation=function(p1,p2){
        console.warn("Hooking android.content.Intent.putExtra('java.lang.String', 'java.lang.CharSequence') successful");
        console.log(p1);
        console.log(p2);
        // getStackTrace();
        return this.putExtra(p1,p2);
    }

    Intent.$init.overload('android.os.Parcel').implementation=function(p1){
        console.warn("Hooking android.content.Intent.$init('android.os.Parcel') successful");
        // getStackTrace();
        this.$init(p1);
    }
    Intent.$init.overload('java.lang.String').implementation=function(p1){
        console.warn("Hooking android.content.Intent.$init('java.lang.String') successful");
        console.log(p1);
        // getStackTrace();
        this.$init(p1);
    }
    Intent.$init.overload('android.content.Intent').implementation=function(p1){
        console.warn("Hooking android.content.Intent.$init('android.content.Intent') successful");
        // getStackTrace();
        this.$init(p1);
    }
    Intent.$init.overload('java.lang.String', 'android.net.Uri').implementation=function(p1,p2){
        console.warn("Hooking android.content.Intent.$init('java.lang.String', 'android.net.Uri') successful");
        console.log(p1);
        console.log(p2);
        // getStackTrace();
        this.$init(p1,p2);
    }
    Intent.$init.overload('android.content.Context', 'java.lang.Class').implementation=function(p1,p2){
        console.warn("Hooking android.content.Intent.$init('android.content.Context', 'java.lang.Class') successful");
        // getStackTrace();
        this.$init(p1,p2);
    }
    Intent.$init.overload('android.content.Intent', 'int').implementation=function(p1,p2){
        console.warn("Hooking android.content.Intent.$init('android.content.Intent', 'int') successful");
        // getStackTrace();
        this.$init(p1,p2);
    }
    Intent.$init.overload('java.lang.String', 'android.net.Uri', 'android.content.Context', 'java.lang.Class').implementation=function(p1,p2,p3,p4){
        console.warn("Hooking android.content.Intent.$init('java.lang.String', 'android.net.Uri', 'android.content.Context', 'java.lang.Class') successful");
        console.log(p1);
        console.log(p2);
        // getStackTrace();
        this.$init(p1,p2,p3,p4);
    }

}

/*
    hook Android Uri
    参数: bShowStacks
    返回值:无
*/

function VMHook_Uri(bShowStacks) {
    // android.net.Uri
    const Uri = Java.use('android.net.Uri');
    Uri.parse.implementation = function (str) {
        log('hook_uri' + 'str: ' + str);
        if (bShowStacks) {
            VMprintJavaStack("android.net.Uri Stack");
        }
        return this.parse(str);
    }
}

/*
    hook Java Uri.ctor
    参数: bShowStacks
    返回值:无
*/
function VMhookNetUrlCtor(bShowStacks) {
    // java.net.URL;
    const URL = Java.use('java.net.URL');
    URL.$init.overload('java.lang.String').implementation = function (url) {
        log('hook_url' + 'url: ' + url);
        if (bShowStacks) {
            VMprintJavaStack("java.net.URL");
        }
        return this.$init(url);
    }
}


function VMgetApplicationContext() {
    const ActivityThread = Java.use('android.app.ActivityThread');
    const Context = Java.use('android.content.Context');
    const ctx = Java.cast(ActivityThread.currentApplication().getApplicationContext(), Context);
    return ctx;
}



/**
 * 替换类加载器(ClassLoader)
 * @param {*} packageName 
 */
function VMreplaceClassLoder(packageName){
    console.log("正在找合适的类加载器...");
    Java.enumerateClassLoaders({
        onMatch:function(loader){
            //console.log("loader:"+loader);
            if(loader.toString().search(packageName)!=-1){
                
                //找到了可以加载这个类的类加载器
                //那就将当前的类加载器切换
                Java.classFactory.loader = loader;
                console.log("类加载器已切换成功!");
            }
        },onComplete:function(){}//可以什么代码都不写,但结构不能乱
    })
}