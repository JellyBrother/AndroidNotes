# AR Service应用无法下载

### 现象

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/LAJdl6gXdXBQnke1/img/868854c5-fbba-47d2-8267-d6f34fae4f08.png)

### 原因

以前分析过类似问题，一般是AndroidManifest.xml中声明的feature当前设备不支持。

通过分析ar.apk （com.google.ar.core）中的AndroidManifest.xml发现问题

    <!-- 需要设备具备如下属性 -->
    <uses-feature android:name="android.hardware.camera.ar" android:required="true" />

通过 adb shell pm list features 查看当前设备的特性

    255|HWANA:/ $ pm list features                                                                                                                                                                             
    feature:reqGlEsVersion=0x30002
    feature:android.hardware.audio.low_latency
    feature:android.hardware.audio.output
    feature:android.hardware.bluetooth
    feature:android.hardware.bluetooth_le
    feature:android.hardware.camera
    feature:android.hardware.camera.any
    feature:android.hardware.camera.autofocus
    feature:android.hardware.camera.capability.manual_post_processing
    feature:android.hardware.camera.flash
    feature:android.hardware.camera.front
    feature:android.hardware.faketouch
    feature:android.hardware.fingerprint
    feature:android.hardware.location
    feature:android.hardware.location.gps
    feature:android.hardware.location.network
    feature:android.hardware.microphone
    feature:android.hardware.nfc
    feature:android.hardware.nfc.any
    feature:android.hardware.nfc.hce
    feature:android.hardware.nfc.hcef
    feature:android.hardware.nfc.uicc
    feature:android.hardware.opengles.aep
    feature:android.hardware.ram.normal
    feature:android.hardware.screen.landscape
    feature:android.hardware.screen.portrait
    feature:android.hardware.sensor.accelerometer
    feature:android.hardware.sensor.compass
    feature:android.hardware.sensor.gyroscope
    feature:android.hardware.sensor.light
    feature:android.hardware.sensor.proximity
    feature:android.hardware.sensor.stepcounter
    feature:android.hardware.sensor.stepdetector
    feature:android.hardware.telephony
    feature:android.hardware.telephony.gsm
    feature:android.hardware.telephony.ims
    feature:android.hardware.touchscreen
    feature:android.hardware.touchscreen.multitouch
    feature:android.hardware.touchscreen.multitouch.distinct
    feature:android.hardware.touchscreen.multitouch.jazzhand
    feature:android.hardware.usb.accessory
    feature:android.hardware.usb.host
    feature:android.hardware.vulkan.compute
    feature:android.hardware.vulkan.level=1
    feature:android.hardware.vulkan.version=4198400
    feature:android.hardware.wifi
    feature:android.hardware.wifi.direct
    feature:android.hardware.wifi.passpoint
    feature:android.software.activities_on_secondary_displays
    feature:android.software.app_widgets
    feature:android.software.autofill
    feature:android.software.backup
    feature:android.software.cant_save_state
    feature:android.software.companion_device_setup
    feature:android.software.connectionservice
    feature:android.software.cts
    feature:android.software.device_admin
    feature:android.software.file_based_encryption
    feature:android.software.freeform_window_management
    feature:android.software.home_screen
    feature:android.software.input_methods
    feature:android.software.ipsec_tunnels
    feature:android.software.live_wallpaper
    feature:android.software.managed_users
    feature:android.software.midi
    feature:android.software.picture_in_picture
    feature:android.software.print
    feature:android.software.secure_lock_screen
    feature:android.software.securely_removes_users
    feature:android.software.verified_boot
    feature:android.software.voice_recognizers
    feature:android.software.webview
    feature:android.sofware.nfc.beam
    feature:com.huawei.emui.api.23
    feature:com.huawei.software.features.china
    feature:com.huawei.software.features.full
    feature:com.huawei.software.features.handset
    feature:com.huawei.software.features.huawei
    feature:com.huawei.system.feature