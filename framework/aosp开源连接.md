# aosp开源连接

用VSCode的**Remote - SSH**插件，ssh aosp\_guest@192.168.22.220

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/W4j6OJkWdNQ7l3p8/img/73343b7d-19dc-478c-adcd-8f46a749947f.gif)

连接后**打开文件夹**，进入到 /home/data/aosp，选择要查看的Android版本，

会提示输入密码aosp123

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/W4j6OJkWdNQ7l3p8/img/3fd809ed-4c3a-4ec1-9263-08ce6519f61e.gif)

也可以登录网站进行查看

[http://aospxref.com/android-13.0.0\_r3/](http://aospxref.com/android-13.0.0_r3/)

# 源码编译

### VMOS编译机连接信息

### 修改代码 （Windows也能正常使用）

可以通过VSCode  SSH Remote 插件，远程修改编译机上的代码. /data/android-4.4.4-vmos /data/android-5.1.3-vmos /data/android-7.1.2-vmos /data/android-9.0.0-vmos ![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/JZWGl07eyp7eO34Y/img/fb3a9b9e-7561-4af0-960f-5a8b2d3007b1.png)

### 编译代码

使用docker 编译容器对指定vmos系统进行编译 举个例子，比如编译 vmos 7.1.2

    $ docker start vmos-7
    $ docker attach vmos-7  # 附加到指定容器
    . build/envsetup.sh     # 初始化环境变量
    lunch aosp_arm64-user   # 编译64位或者 lunch aosp_arm-user 这是编译32位
    export USER=android
    make -j24  # 开始编译
    
    mmm -j16 frameworks/base/  # 单独编译某个模块，确认一下该目录下是否有Android.mk
    
    ls -l out/target/product/generic_arm64_user/  # 编译输出目录

### ROM打包

从git仓库将线上的rom包clone到服务器, 例如android71  64位ylinker

    mkdir /data/vmos_rom_pack_dir/android71
    git clone https://git.vmos.pro/vmos-android/rootfs71.git -b android71_ylinker

克隆下载后，和编译输出目录使用 beyond compare 进行对比, 同步修改项 以下示例图，我使用本地的rom包目录和远程编译机的结果进行对比，也可以两边都使用远程编译机的目录，编译结果和rom包目录稍微有点区别，要对比两个目录 rom包根路径/rootfs/system  <->  out/target/product/generic_arm64_user/system![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/JZWGl07eyp7eO34Y/img/7e7be9be-e4d8-4bff-83bc-1be43ff3ec70.png) rom包根路径/rootfs/  <->  out/target/product/generic_arm64_user/root ![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/JZWGl07eyp7eO34Y/img/ae37a1de-52f2-4f9c-80a7-93f36d8037c2.png) 最后使用7z命令直接在编译机上进行打包

    7za a 71.zip ./rootfs/* -xr\!.gitkeep   # 测试时可以使用zip压缩方式, 时间更快
    7za a 71.7z ./rootfs/* -xr\!.gitkeep    # 使用7z压缩，包体更小