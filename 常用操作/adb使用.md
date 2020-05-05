显示帮助信息：adb  help
查看设备： adb  devices
adb install com.sina.weibo	
安装软件：adb install com.sina.weibo	
参数“-r”,它是更新安装的意思，
参数 -s ,安装到sdcard.
卸载软件：adb uninstall com.sina.weibo
把电脑上的文件复制到手机：adb push D:\Download /sdcard/Bluetooth/
把手机上的文件复制到电脑：adb pull /sdcard/Bluetooth/ D:\Download
同步更新：adb sync
重新挂载：adb  remount
启动server：adb start-server
关闭server：adb kill-server 
重启设备：adb reboot 
查看Log：adb logcat
将日志保存到文件test.log：adb logcat > c:\test.log 
查看bug报告：adb bugreport

登录设备shell：adb  shell 
按键事件
input text <string>   input a string to device
input keyevent
am命令
adb shell am start -n com.android.settings/com.android.settings.Settings
adb shell am start - com.android.settings/com.android.settings.Settings
pm 命令
pm install [options]	安装应用
pm uninstall [options]	卸载应用
adb shell pm clear 清除包数据
C:\Users\72077879>adb shell "dumpsys activity | grep mResume" 查看当前页面
    mResumedActivity: ActivityRecord{8466ad6 u0 com.google.android.gm/com.google.android.libraries.eas.onboarding.OnboardingActivity d0 s139 t139}

Monkey命令
说明：com.htc.Weather为包名，100是事件计数（即让Monkey程序模拟100次随机用户事件）。
指定多个包：com.htc.Weather\com.htc.pdfreadercom.htc.photo.widgets
adb shell monkey -pcom.htc.Weather –p com.htc.pdfreader  -pcom.htc.photo.widgets 100



### 查看设备
adb  devices 这个命令是查看当前连接的设备, 连接到计算机的android设备或者模拟器将会列出显示。
### 查看版本
adb version
### 安装、卸载apk
* 安装
如果在我电脑D盘下面有个a.apk文件，我只需要一行命令就安装到手机上：
adb install d:/a.apk
如果你已经安装了这个程序，可以通过以下命令覆盖安装：
adb install -r d:/a.apk
当然你可以安装指定位置，如我这里安装到sdcard：
adb install -s d:/a.apk
保留数据和缓存文件，重新安装 apk
adb install -r a.apk  
d:/a.apk 表示你 .apk 的存储位置。
* 卸载
adb uninstall com.xxx.xx.apk
com.xxx.xx.apk表示你项目的包名。
-k 参数，为卸载软件但是保留配置和缓存文件.
adb uninstall -k com.github.ws.apk  
### 接收电脑文件、上传文件到电脑
* 接收电脑文件
adb push d:/a.txt /sdcard/
把电脑 D 盘下的 a.txt 文件推送到手机 sdcard 目录下。这里以 sdcard 目录为例，你可以随便替换成你存放的目录，后文就不再累诉。
* 上传文件
上传位于/sdcard/目录下的 a.txt 文件到电脑的 D 盘根目录下：
adb pull /sdcard/a.txt d:/
d:/ 代表文件的存放目录，你可以随意替换成你的存放目录，如果文件存当前目录下也可以省略。
### adb shell
通过adb shell 可以操作你项目的数据库。需要root权限
* 操作数据库
adb shell
cd  data 
cd  data 
cd  xx    //你项目的包名
cd  databases
sqlite3   xxx  //你数据库名
* 使用adb进行屏幕录像
adb shell screenrecord /sdcard/a.mp4
通过ctrl+c停止录制，你可以在 sdcard 目录下查看。妈妈再也不担心我到处找录制视频的软件了。
* 截屏
adb shell /system/bin/screencap -p /sdcard/a.png
同样在 sdcard 目录下查看。
* 输入文本
选中你要输入文本的输入框，输入下面的指令：
adb shell 
input text abc123
* 退出adb shell
exit
### 同步更新
adb sync /data/
如果不指定目录，将同时更新 /data 和 /system/
### 显示帮助信息
adb  help 
### 重新挂载
adb  remount
重新挂载系统 ，分区，用于读写
### 启动，停止，重启，消亡
* 启动
adb start-server 
* 停止
adb stop-server
* 重启
adb restart-server
* 消亡
adb kill-server
### 查看bug报告
adb bugreport 这个打印出来一大推。
### 查看Log
adb logcat
### 包管理
* 列出手机装的所有 app 的包名：
adb shell pm list packages 
* 列出系统应用的所有包名：
adb shell pm list packages -s 
* 列出除了系统应用的第三方应用包名：
adb shell pm list packages -3
* 清除应用数据与缓存
adb shell pm clear com.github.mvp
com.github.mvp 应用包名
### 启动应用
通过 adb 来启动应用
adb shell am start -n com.github.mvp/.MainActivity
### 强制停止应用
* 有些时候应用卡死了，需要强制停止，则执行以下命令：
adb shell am force-stop com.github.mvp
### 重启
adb reboot
### 获取序列号
adb get-serialno
### 获取 MAC 地址
adb shell  cat /sys/class/net/wlan0/address
7c:7d:**:**:**:**
### 查看设备型号
adb shell getprop ro.product.model
HUAWEI RIO-AL00
### 查看 Android 系统版本
adb shell getprop ro.build.version.release
9.0
### 查看屏幕分辨率
adb shell wm size
Physical size: 1080x1920
### 查看屏幕密度
adb shell wm density
Physical density: 480
### 查看permissions
adb shell pm list permissions 
### 查看系统的危险权限dangerous permissions
adb shell pm list permissions -d -g


















