[![Build Status](https://travis-ci.org/android-hacker/VirtualXposed.svg?branch=exposed)](https://travis-ci.org/android-hacker/VirtualXposed)

[中文文档](CHINESE.md "中文")

Introduction
------------
**VirtualXposed** is a simple App based on [VirtualApp](https://github.com/asLody/VirtualApp) and [epic](https://github.com/tiann/epic) that allows you to use an Xposed Module without needing to root, unlock the bootloader, or flash a custom system image. (Supports Android 5.0~9.0) 

The only two restriction of VirtualXposed are:

1. Unable to modify system, so any Module which modifies system won't be able to work properly.
2. Currently resource hooks are not supported. (Theming modules use Resource Hooks).

Warning
-----------

Usage for Commercial Purposes are not allowed!!!  Please refer to VirtualApp's [declaration](https://github.com/asLody/VirtualApp).

Usage
-------

### Preparation

Download the latest APK from the [release page](https://github.com/android-hacker/VirtualXposed/releases), and install it on your Android device.

### Install APP and Xposed Module

Open VirtualXposed, Click on the **Drawer Button** at the bottom of home page(Or long click the screen), add your desired APP and Xposed Module to VirtualXposed's virtual environment.

Note: **All operations（installation of Xposed Module, APP）must be done in VirtualXposed**, otherwise the Xposed Module installed won't take effect. For example, if you install the YouTube app on your system (Your phone's original system, not in VirtualXposed), and then install YouTube AdAway (A YouTube Xposed Module) in VirtualXposed; or you install YouTube in VirtualXposed, and install YouTube AdAway on original system; or both of them are installed on original system, **neither of these three cases will work!**

![How to install](https://raw.githubusercontent.com/tiann/arts/master/vxp_install.gif)

There are three ways to install an APP or Xposed Module to VirtualXposed:

1. **Clone an installed app from your original system.** (Click Button at bottom of home page, then click Add App, the first page shows a list of installed apps.)
2. **Install via an APK file.** (Click Button at bottom of home page, then click Add App, the second page shows APKs found in your sdcard)
3. **Install via an external file chooser.** (Click Button at bottom of home page, then click Add App, use the floating action button to choose an APK file to install)

For Xposed Module, You can install it from Xposed Installer, too.

### Activate the Xposed Module

Open Xposed Installer in VirtualXposed, go to the module fragment, check the module you want to use:

![How to activate module](https://raw.githubusercontent.com/tiann/arts/master/vxp_activate.gif)

### Reboot

You only need to reboot VirtualXposed, **There's no need to reboot your phone**; Just click Settings in home page of VirtualXposed, click `Reboot` button, and VirtualXposed will reboot in a blink. 

![How to reboot](https://raw.githubusercontent.com/tiann/arts/master/vxp_reboot.gif)

Supported Modules 
-------------------------

Almost all modules except system-relevant are supported, please try it by yourself :)

Others
-------

### GameGuardian

VirtualXposed also supports GameGuardian, **you should use the separate version for GameGuardian**.(Download it in release page).

[Video Tutorial](https://gameguardian.net/forum/gallery/image/437-no-root-via-virtualxposed-without-error-105-gameguardian/)

### VirusTotal

VirusTotal might report VirtualXposed as a malware, it is stupid, you can refer to my [explanation](https://github.com/android-hacker/VirtualXposed/issues/10).

And obviously, VirtualXposed is open source, so you can refer to the source code. I am sure that it is safe to use.

If you still couldn't believe in me, you can install version [0.8.7](https://github.com/android-hacker/VirtualXposed/releases/tag/0.8.7); VirusTotal reports this version as safe.

Support
-----------

Contributions to VirtualXposed are always welcomed!!

For Developers
--------------

- [File a bug](https://github.com/android-hacker/exposed/issues)
- [Wiki](https://github.com/android-hacker/VirtualXposed/wiki)

Credits
-------

1. [VirtualApp](https://github.com/asLody/VirtualApp)
2. [Xposed](https://github.com/rovo89/Xposed)
