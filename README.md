简介
-----
**VAExposed** 是基于[VirtualApp](https://github.com/asLody/VirtualApp) 和 [epic](https://github.com/tiann/epic) 在**非ROOT**环境下运行Xposed模块的实现（支持4.0~8.1)。

⚠️
-------
本项目使用的 VirtualApp 不允许用于商业用途，如果有这个需求，请联系 Lody (imlody@foxmail.com)。

使用
----------

## 准备

首先在 [发布页面](https://github.com/android-hacker/VAExposed/releases) 下载最新的VAExposed安装包安装到手机。

## 安装模块

打开VAExposed，在里面安装要使用的APP，以及相应的Xposed模块即可。

注意：**所有的工作（安装Xposed模块，安装APP）必须在VAExposed中**进行，否则Xposed模块不会有任何作用！比如，将微信直接安装在系统上（而非VAEXposed中），防撤回安装在VAExposed中；或者把微信安装在VAExposed上，防撤回插件直接安装在系统上；或者两者都直接安装在系统上，**均不会起任何作用**。

在VAExposed中安装App有两种方式：

1. 直接复制已经在系统中安装好的APP，比如如果你系统中装了微信，那么可以直接复制一份。
2. 通过外置存储直接安装APK文件；点主界面的➕，然后选择后面两个TAB即可。

在VAExposed中安装Xposed模块，可以跟安装正常的APK一样，以上两种安装App的方式也适用于安装Xposed模块。不过，你也可以通过VAExposed中内置的XposedInstaller来安装和管理模块，跟通常的XposedInstaller使用方式一样；去下载页面，下载安装即可。 


已知问题
-----------

1. 使用部分插件在7.0以上会导致对应的APP黑屏，如QX模块，WX模块。
2. 微信巫师在7.0以上请使用2.5.5版本。
3. 由于暂不支持资源HOOK，部分美化插件无法生效。
4. 部分插件的兼容性有问题，比如上帝模式可以作用在QQ，支付宝上；但无法使用在微信上。

支持和加入
------------

目前VAExposed还不完善，如果你对非ROOT下实现Xposed感兴趣；欢迎加入！你可以通过如下方式来支持：

1. 直接贡献代码，提供Feature，修复BUG！
2. 使用你拥有的手机，安装你常用的Xposed模块，反馈不可用情况；协助帮忙解决兼容性问题！
3. 提出体验上，功能上的建议，帮助完善VAExposed！

致谢
------

1. [VirtualApp](https://github.com/asLody/VirtualApp)
2. [Xposed](https://github.com/rovo89/Xposed)
    

