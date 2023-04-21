
console.log("frida start ---->");
console.log("frida end ---->");

//打印java堆栈
console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Exception").$new()));

// 打印堆栈
function printStack() {
  Java.perform(function () {
      var Exception = Java.use("java.lang.Exception");
      var ins = Exception.$new("Exception");
      var straces = ins.getStackTrace();
      if (straces != undefined && straces != null) {
          var strace = straces.toString();
          var replaceStr = strace.replace(/,/g, "\n");
          console.log("=============================Stack strat=======================");
          console.log(replaceStr);
          console.log("=============================Stack end=========================");
          Exception.$dispose();
      }
  });
}

// 打印成员变量的值
console.log(`Activity onCreate is called this.k:+ ${this.k.value}`  + "  this.i:" + this.i.value);

// 打印类名和方法入参
console.log(`${this.getClass()} -->ActivityonCreate is called -->' + ${bundle}`);

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
    Activity["finish"].overload().implementation = function () {
      console.log(`${this.getClass()} -->Activity finish is called`);
      let ret = this.finish();
      console.log('Activity finish ret value is ' + ret);

      printStack();
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
Java.perform(function () {
  try {
    let Service = Java.use("android.app.Service")
    Service["onCreate"].implementation = function () {
      console.log(`${this.getClass()} -->Service onCreate is called -->`);
      let ret = this.onCreate();
      return ret;
    }
    Service["onBind"].overload('android.content.Intent').implementation = function (intent) {
      console.log(`${this.getClass()} -->Service onBind is called -->' + ${intent}`);
      let ret = this.onBind(intent);
      return ret;
    }
    Service["onDestroy"].implementation = function () {
      console.log(`${this.getClass()} -->Service onDestroy is called`);
      let ret = this.onDestroy();
      return ret;
    }  
  } catch (error) {
    console.log('Java.perform error:' + error);
  }
});

// 强转
var ArrayList = Java.use('java.util.ArrayList');
var list = Java.cast(callbacks, ArrayList);   //类似这样，但你boolean不用强转吧，0就是false，1就是true

// 打印binder通信
let cvc = Java.use("cvc");
cvc["onTransact"].overload('int', 'android.os.Parcel', 'android.os.Parcel', 'int')
  .implementation = function (i, parcel, parcel2, i2) {
    let desc = this.getInterfaceDescriptor()
    console.log('cvc onTransact is called' + ', ' + 'i: ' + i + ', ' + 'parcel: ' + parcel + ', ' + 'parcel2: ' + parcel2 + ', ' + 'i2: ' + i2
      + ', ' + 'parcel2.getClass().getName(): ' + parcel2.getClass().getName()
      + ', ' + 'desc: ' + desc
      + ',time:' + Date.now());
    // parcel2.setDataPosition(0)
    let ret = this.onTransact(i, parcel, parcel2, i2);
    console.log('cvc onTransact ret value is ' + ret);
    return ret;
  };

// 打印binder通信的序列化读取角标
let gah = Java.use("gah");
gah["createFromParcel"].implementation = function (parcel) {
  console.log('gah createFromParcel is called' + ', ' + 'parcel: ' + parcel + ', ' + 'parcel.dataPosition(): ' + parcel.dataPosition()
    + ', ' + 'this.a.value: ' + this.a.value
    + ',time:' + Date.now());
  let ret = this.createFromParcel(parcel);
  console.log('gah createFromParcel ret value is ' + ret);

  printStack();
  return ret;
};

// 音乐播放
let MediaPlayer = Java.use("android.media.MediaPlayer");
MediaPlayer["start"].implementation = function () {
  console.log('MediaPlayer start is called');
  let ret = this.start();
  console.log('MediaPlayer start ret value is ' + ret);
  return ret;
};
MediaPlayer["pause"].implementation = function () {
  console.log('MediaPlayer pause is called');
  let ret = this.pause();
  console.log('MediaPlayer pause ret value is ' + ret);
  return ret;
};
let AudioTrack = Java.use("android.media.AudioTrack");
AudioTrack["pause"].implementation = function () {
  console.log('AudioTrack pause is called');
  let ret = this.pause();
  console.log('AudioTrack pause ret value is ' + ret);

  printStack();
  return ret;
};

// 哪个控件点击了
let View = Java.use("android.view.View");
View["performClick"].implementation = function () {
  let Parent1 = this.getParent();
  // var Color = Java.use('android.graphics.Color');
  // Parent1.setBackgroundColor(Color.argb(127, 2, 2, 2));
  console.log('View performClick is called getClass：'+this.getClass()
  + ', ' + 'Parent1: ' + Parent1
  );
  let ret = this.performClick();
  console.log('View performClick ret value is ' + ret);
  return ret;
};

// 播放器
let AudioManager = Java.use("android.media.AudioManager");
AudioManager["requestAudioFocus"].overload('android.media.AudioFocusRequest').implementation = function (build2) {
  console.log('AudioManager requestAudioFocus is called' + ', ' + 'build2: ' + build2);
  let ret = this.requestAudioFocus(build2);
  console.log('AudioManager requestAudioFocus ret value is ' + ret);
  return ret;
};
let AudioDriver = Java.use("com.spotify.playback.playbacknative.AudioDriver");
AudioDriver["setPaused"].implementation = function (z) {
  console.log('AudioDriver setPaused is called' + ', ' + 'z: ' + z);
  let ret = this.setPaused(z);
  console.log('AudioDriver setPaused ret value is ' + ret);
  return ret;
};
AudioDriver["addListener"].implementation = function (audioDriverListener) {
  console.log('AudioDriver addListener is called' + ', ' + 'audioDriverListener: ' + audioDriverListener);
  let ret = this.addListener(audioDriverListener);
  console.log('AudioDriver addListener ret value is ' + ret);

  printStack();
  return ret;
};
AudioDriver["removeListener"].implementation = function (audioDriverListener) {
  console.log('AudioDriver removeListener is called' + ', ' + 'audioDriverListener: ' + audioDriverListener);
  let ret = this.removeListener(audioDriverListener);
  console.log('AudioDriver removeListener ret value is ' + ret);
  return ret;
};
AudioDriver["stopDuckingAudioSession"].overload('int').implementation = function (i) {
  console.log('AudioDriver stopDuckingAudioSession is called' + ', ' + 'i: ' + i);
  let ret = this.stopDuckingAudioSession(i);
  console.log('AudioDriver stopDuckingAudioSession ret value is ' + ret);

  printStack();
  return ret;
};
AudioDriver["destroy"].implementation = function () {
  console.log('AudioDriver destroy is called');
  let ret = this.destroy();
  console.log('AudioDriver destroy ret value is ' + ret);
  return ret;
};
AudioDriver["onMarkerReached"].implementation = function (audioTrack) {
  console.log('AudioDriver onMarkerReached is called' + ', ' + 'audioTrack: ' + audioTrack);
  let ret = this.onMarkerReached(audioTrack);
  console.log('AudioDriver onMarkerReached ret value is ' + ret);
  return ret;
};
AudioDriver["startPlayback"].implementation = function () {
  console.log('AudioDriver startPlayback is called');
  let ret = this.startPlayback();
  console.log('AudioDriver startPlayback ret value is ' + ret);

  printStack();
  return ret;
};
AudioDriver["stopPlayback"].implementation = function () {
  console.log('AudioDriver stopPlayback is called');
  let ret = this.stopPlayback();
  console.log('AudioDriver stopPlayback ret value is ' + ret);
  return ret;
};
AudioDriver["open"].overload('int', 'int', 'int', 'int').implementation = function (i, i2, i3, i4) {
  console.log('AudioDriver open is called' + ', ' + 'i: ' + i + ', ' + 'i2: ' + i2 + ', ' + 'i3: ' + i3 + ', ' + 'i4: ' + i4);
  let ret = this.open(i, i2, i3, i4);
  console.log('AudioDriver open ret value is ' + ret);
  return ret;
};
AudioDriver["getCurrentAudioSession"].implementation = function () {
  console.log('AudioDriver getCurrentAudioSession is called');
  let ret = this.getCurrentAudioSession();
  console.log('AudioDriver getCurrentAudioSession ret value is ' + ret);
  printStack();
  return ret;
};
let AudioTrack = Java.use("android.media.AudioTrack");
AudioTrack["pause"].implementation = function () {
  console.log('AudioTrack pause is called');
  let ret = this.pause();
  console.log('AudioTrack pause ret value is ' + ret);
  return ret;
};

let MediaPlayer = Java.use("android.media.MediaPlayer");
MediaPlayer["start"].implementation = function () {
  console.log('MediaPlayer start is called');
  let ret = this.start();
  console.log('MediaPlayer start ret value is ' + ret);
  return ret;
};
MediaPlayer["pause"].implementation = function () {
  console.log('MediaPlayer pause is called');
  let ret = this.pause();
  console.log('MediaPlayer pause ret value is ' + ret);

  printStack();
  return ret;
};
let MediaPlayer = Java.use("android.media.MediaPlayer");
MediaPlayer["start"].implementation = function () {
  console.log('MediaPlayer start is called');
  let ret = this.start();
  console.log('MediaPlayer start ret value is ' + ret);
  return ret;
};
MediaPlayer["pause"].implementation = function () {
  console.log('MediaPlayer pause is called');
  let ret = this.pause();
  console.log('MediaPlayer pause ret value is ' + ret);

  printStack();
  return ret;
};

//电量管理
let PowerManager = Java.use("android.os.PowerManager");
PowerManager["isDeviceIdleMode"].implementation = function () {
  console.log('PowerManager isDeviceIdleMode is called');
  let ret = this.isDeviceIdleMode();
  console.log('PowerManager isDeviceIdleMode ret value is ' + ret);

  printStack();
  return ret;
};
PowerManager["isPowerSaveMode"].implementation = function () {
  console.log('PowerManager isPowerSaveMode is called');
  let ret = this.isPowerSaveMode();
  console.log('PowerManager isPowerSaveMode ret value is ' + ret);

  printStack();
  return ret;
};
PowerManager["goToSleep"].implementation = function (time,  reason,  flags) {
  console.log('PowerManager goToSleep is called'
  + ', ' + 'time: ' + time
  + ', ' + 'reason: ' + reason
  + ', ' + 'flags: ' + flags
  );
  let ret = this.goToSleep(time,  reason,  flags);
  console.log('PowerManager goToSleep ret value is ' + ret);

  printStack();
  return ret;
};
let PowerManagerWakeLock = Java.use("android.os.PowerManager$WakeLock");
PowerManagerWakeLock["setReferenceCounted"].implementation = function (bbfVar) {
  console.log('PowerManagerWakeLock setReferenceCounted is called' + ', ' + 'bbfVar: ' + bbfVar);
  let ret = this.setReferenceCounted(bbfVar);
  console.log('PowerManagerWakeLock setReferenceCounted ret value is ' + ret);

  printStack();
  return ret;
};
PowerManagerWakeLock["acquire"].overload().implementation = function () {
  console.log('PowerManagerWakeLock acquire is called');
  let ret = this.acquire();
  console.log('PowerManagerWakeLock acquire ret value is ' + ret);

  printStack();
  return ret;
};
PowerManagerWakeLock["acquire"].overload('long').implementation = function (bbfVar) {
  console.log('PowerManagerWakeLock acquire is called' + ', ' + 'bbfVar: ' + bbfVar);
  let ret = this.acquire(bbfVar);
  console.log('PowerManagerWakeLock acquire ret value is ' + ret);

  printStack();
  return ret;
};
PowerManagerWakeLock["release"].overload('int').implementation = function (bbfVar) {
  console.log('PowerManagerWakeLock release is called' + ', ' + 'bbfVar: ' + bbfVar);
  let ret = this.release(bbfVar);
  console.log('PowerManagerWakeLock release ret value is ' + ret);

  printStack();
  return ret;
};

function findClass(className){
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

