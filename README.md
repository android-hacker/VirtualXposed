[![Build Status](https://travis-ci.org/android-hacker/VirtualXposed.svg?branch=exposed)](https://travis-ci.org/android-hacker/VirtualXposed)

[中文文档](CHINESE.md "中文")

Introduction
------------
**VirtualXposed** is a simple APP based on [VirtualApp](https://github.com/asLody/VirtualApp) and [epic](https://github.com/tiann/epic) that lets you use the Xposed Module without needing to root, unlock the bootloader, or flash a system image. (Support Android 5.0~8.1)

Warning
-----------

No use for Commercial Purposes!!!  Please refer to VirtualApp's [declaration](https://github.com/asLody/VirtualApp).

Usage
-------

### Preparation

Download the latest apk from the [Release page](https://github.com/android-hacker/VirtualXposed/releases), and install it on your Android device.

### Install APP and Xposed module

Open VirtualXposed, Click the ➕ in home page, add the APP and Xposed module to VirtualXposed's virtual environment.

Notice: **All operation（install Xposed module, APP）must be done in VirtualXposed**, Otherwise, the Xposed module won't take effect!! For example, If you install YouTube APP on your system (Your phone's original system, not in VirtualXposed), and then install YouTube AdAway (A YouTube Xposed module) in VirtualXposed; or you install YouTube in VirtualXposed, and install YouTube AdAway on original system; or both of them are installed on original system, **These three cases won't take effect!**

![How to install](https://raw.githubusercontent.com/tiann/arts/master/vxp_install.gif)

There are three ways to install an APP or Xposed module to VirtualXposed:

1. Clone the original system's installed apps. Click Button at bottom of home page, then click Add App, the first page are installed apps.
2. Install via an apk file. (Click Button at bottom of home page, then click Add App, the second page are apks found in your sdcard)
3. Install via the external file chooser. (Click Button at bottom of home page home page, then click App App, use the float button to choose apk file to install)

For Xposed module, You can install it from XposedInstaller, too.

### Active the Xposed module

Open XposedInstaller in VirtualXposed, go to the module fragment, check the module you want to use:

![How to active module](https://raw.githubusercontent.com/tiann/arts/master/vxp_active.gif)

### Reboot

Reboot the VirtualXposed, **No need to reboot your phone**; Just click Settings in home page of VirtualXposed, click `Reboot` button, and VirtualXposed will reboot like a shot. 

![How to reboot](https://raw.githubusercontent.com/tiann/arts/master/vxp_reboot.gif)

Supported Modules
--------------------

- [微X模块][wx]
- [微信巫师][wxws]
- [MDWechat][mdwechat]
- [应用变量][yybl]
- [音量增强器][ylzqq]
- [微信学英语][wxxyy]
- [冲顶助手][cdzs]
- [情迁抢包][qqqb]
- [微信跳一跳助手][ttzs]
- [步数修改器][bsxg]
- [模拟位置][mnwz]
- [指纹支付][zwzf]
- [微信增强插件][wxzqcj]

Far more than above.

Others
-------

### GameGuardian

VirtualXposed also supports GameGuardian, **use version 0.9.1 for best compatibility**.(You can download it in release page).

[Video Tutorial](https://gameguardian.net/forum/gallery/image/437-no-root-via-virtualxposed-without-error-105-gameguardian/)

### VirusTotal

VirusTotal may say VirtualXposed has malware, it is stupid, you can refer to my [explanation](https://github.com/android-hacker/VirtualXposed/issues/10).

And VirtualXposed is obviously open source, so you can refer to the source code. I am sure it is safe.

If you still do not believe me, you can install version [0.8.7](https://github.com/android-hacker/VirtualXposed/releases/tag/0.8.7); VirusTotal says this version is safe.


Known Issues
-------------

1. Cannot modify system, so the Module used to modify system can never take effect.
2. Do not support Resource hooks now.
3. Some modules may not be compatible with VirtualXposed now.

Support
-----------

Welcome to contributing for VirtualXposed!!

For Developer
--------------

- [Fire a bug](https://github.com/android-hacker/exposed/issues)
- [Wiki](https://github.com/android-hacker/VirtualXposed/wiki)

Credit
-------

1. [VirtualApp](https://github.com/asLody/VirtualApp)
2. [Xposed](https://github.com/rovo89/Xposed)

[wx]: https://pan.baidu.com/s/1hrOzCnq#list/path=%2Freleases%2Fapk&parentPath=%2Freleases
[wxws]: https://github.com/Gh0u1L5/WechatMagician/releases
[yybl]: https://www.coolapk.com/apk/com.sollyu.xposed.hook.model
[ylzqq]: https://github.com/bin456789/Unblock163MusicClient-Xposed/releases
[wxxyy]: https://www.coolapk.com/apk/com.hiwechart.translate
[cdzs]: https://www.coolapk.com/apk/com.gy.xposed.cddh
[qqqb]: http://repo.xposed.info/module/cn.qssq666.redpacket
[ttzs]: http://repo.xposed.info/module/com.emily.mmjumphelper
[mnwz]: https://www.coolapk.com/apk/com.rong.xposed.fakelocation
[zwzf]: https://github.com/android-hacker/Xposed-Fingerprint-pay/releases
[bsxg]: https://www.coolapk.com/apk/com.specher.sm
[mdwechat]: https://github.com/Blankeer/MDWechat
[wxzqcj]:https://github.com/firesunCN/WechatEnhancement
