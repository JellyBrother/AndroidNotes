[参考网站](https://jspang.com/posts/2019/01/20/flutter-base.html#%E7%AC%AC04%E8%8A%82%EF%BC%9A%E5%AE%89%E8%A3%85avd%E8%99%9A%E6%8B%9F%E6%9C%BA-flutter%E8%B7%91%E8%B5%B7%E6%9D%A5)
[flutter的开源地址](https://github.com/flutter/flutter)
[flutter的开源控件地址](https://github.com/Solido/awesome-flutter)

### 搭建JAVA环境
Windows x86 32位系统
Windows x64 64位系统，是兼容32位系统的。
[java的jdk下载地址](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
下载jdk，解压后配置环境变量。  可以输入 java 和 java -version 来检测是否搭建成功。

### 搭建flutter环境
[flutter的jdk下载地址](https://flutter.dev/docs/development/tools/sdk/releases#windows)
下载jdk，解压后配置环境变量。  可以输入 flutter doctor 来检测是否搭建成功。

### 搭建安卓环境
[下载地址](https://developer.android.google.cn/)
- 下载Android Studio，搭建安卓sdk和开发环境。
- 打开Android Stuido 软件，然后找到Plugin的配置，搜索Flutter插件,然后点击安装。安装完成后，你需要重新启动一下Android Studio软件。
- 运行flutter doctor --android-licenses 来安装证书。全部yes。
- Android Stuido新建一个flutter工程，启动模拟器，点击运行。

### 安装dart环境（flutter的sdk就已经带有dart环境了，在flutter\bin\cache\dart-sdk目录下，一般不用额外配置）
- 安装Chocolatey
- 以管理员方式运行cmd，然后粘贴命令回车：@"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"  或者以管理员方式运行PowerShell，然后粘贴命令回车：Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
- 然后粘贴命令回车：choco install dart-sdk  升级命令：choco upgrade dart-sdk
[下载地址](https://chocolatey.org/docs/installation#non-administrative-install)
[下载地址](https://chocolatey.org/install)
[下载地址](https://dart.dev/get-dart#install)


### VSCode下如何玩转Flutter
[VSCode下载地址](https://code.visualstudio.com/)
1、下载安装VSCode
2、在VSCode里面下载安装flutter插件
3、需要Android Stuido新建一个flutter工程。
4、启动模拟器或者手机：
- 新建.bat文件手动启动,路径跟安卓sdk路径有关，内容：E:\android\sdk\tools\emulator.exe -netdelay none -netspeed full -avd AndroidTV
- 如果模拟器名字有空格，用下划线替换。
- 命令启动有问题  emulator -netdelay none -netspeed full -avd AndroidTV
5、在VSCode的命令窗（ctrl+~）执行 flutter run 即可。
6、按 r 显示热更新，按p显示网格，再按p不显示网格,按q退出，按o切换安卓和ios系统。















