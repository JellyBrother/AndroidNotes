
console.log("frida start ---->");
console.log("frida end ---->");

//打印java堆栈
console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Exception").$new()));

//列出加载的类
Java.enumerateLoadedClasses(
  {
    "onMatch": function (className) {
      console.log(className)
    },
    "onComplete": function () { }
  }
)

//初始化日志打印
setImmediate(function () {
  console.log("[*] Starting script");
});

//监控
Java.perform(function () {
  try {

  } catch (error) {
    console.log('Java.perform error:' + error);
  }
});

//生命周期
Java.perform(function () {
  try {
    let Activity = Java.use("android.app.Activity")
    Activity["startActivity"].overload('android.content.Intent').implementation = function (intent) {
      console.log(`${this.getClass()} -->startActivity is called -->' + ${intent}`);
      let ret = this.startActivity(intent);
      return ret;
    }
    Activity["onCreate"].overload('android.os.Bundle').implementation = function (bundle) {
      console.log(`${this.getClass()} -->ActivityonCreate is called -->' + ${bundle}`);
      let ret = this.onCreate(bundle);
      return ret;
    }
    Activity["onResume"].implementation = function () {
      console.log(`${this.getClass()} -->ActivityonResume is called`);
      let ret = this.onResume();
      return ret;
    }
    Activity["onPause"].implementation = function () {
      console.log(`${this.getClass()} -->ActivityonPause is called`);
      let ret = this.onPause();
      return ret;
    }
    Activity["onDestroy"].implementation = function () {
      console.log(`${this.getClass()} -->ActivityonDestroy is called`);
      let ret = this.onDestroy();
      console.log('ActivityonDestroy ret value is ' + ret);
      return ret;
    }  
  } catch (error) {
    console.log('Java.perform error:' + error);
  }
});
Java.perform(function () {
  try {
    let Fragment = Java.use("androidx.fragment.app.Fragment")
    Fragment["onCreate"].overload('android.os.Bundle').implementation = function (bundle) {
      console.log(`${this.getClass()} -->FragmentonCreate is called -->' + ${bundle}`);
      let ret = this.onCreate(bundle);
      return ret;
    }
    Fragment["onResume"].implementation = function () {
      console.log(`${this.getClass()} -->FragmentonResume is called`);
      let ret = this.onResume();
      return ret;
    }
    Fragment["onPause"].implementation = function () {
      console.log(`${this.getClass()} -->FragmentonPause is called`);
      let ret = this.onPause();
      return ret;
    }
    Fragment["onDestroy"].implementation = function () {
      console.log(`${this.getClass()} -->FragmentonDestroy is called`);
      let ret = this.onDestroy();
      console.log('FragmentonDestroy ret value is ' + ret);
      return ret;
    }    
  } catch (error) {
    console.log('Java.perform error:' + error);
  }
});












