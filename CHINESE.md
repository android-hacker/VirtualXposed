[![Build Status](https://travis-ci.org/android-hacker/VirtualXposed.svg?branch=exposed)](https://travis-ci.org/android-hacker/VirtualXposed)

简介
-----
**VirtualXposed** 是基于[VirtualApp](https://github.com/asLody/VirtualApp) 和 [epic](https://github.com/tiann/epic) 在**非ROOT**环境下运行Xposed模块的实现（支持5.0~9.0)。

与 Xposed 相比，目前 VirtualXposed 有两个限制：

1. 不支持修改系统（可以修改普通APP中对系统API的调用），因此重力工具箱，应用控制器等无法使用。
2. 暂不支持资源HOOK，因此资源钩子不会起任何作用；使用资源HOOK的模块，相应的功能不会生效。


警告
-------
本项目使用的 VirtualApp 不允许用于商业用途，并且其内部的 VirtualApp 版本已经过时，如果有这个需求，为了贵公司的长期稳定发展，请使用商业授权，联系 Lody (imlody@foxmail.com)即可。

使用
----------

## 准备

首先在 [发布页面](https://github.com/android-hacker/VirtualXposed/releases) 下载最新的VAExposed安装包安装到手机。

## 安装模块

打开 VirtualXposed，在里面安装要使用的APP，以及相应的Xposed模块即可。

注意：**所有的工作（安装Xposed模块，安装APP）必须在 VirtualXposed中**进行，否则Xposed模块不会有任何作用！比如，将微信直接安装在系统上（而非VirtualXposed中），防撤回安装在VirtualXposed中；或者把微信安装在VirtualXposed上，防撤回插件直接安装在系统上；或者两者都直接安装在系统上，**均不会起任何作用**。

在VirtualXposed中安装App有两种方式：

1. 直接复制已经在系统中安装好的APP，比如如果你系统中装了微信，那么可以直接复制一份。
2. 通过外置存储直接安装APK文件；点主界面的底部按钮－添加应用，然后选择后面两个TAB即可。

在VirtualXposed中安装Xposed模块，可以跟安装正常的APK一样，以上两种安装App的方式也适用于安装Xposed模块。不过，你也可以通过VirtualXposed中内置的XposedInstaller来安装和管理模块，跟通常的XposedInstaller使用方式一样；去下载页面，下载安装即可。 

## 亲测可用的模块

- [XPrivacyLua][xpl]: Really simple to use privacy manager for Android 6.0 Marshmallow and later.
- [XInsta][xinsta]: Instagram module(Feed downing, stories downloading, etc).
- [Minminguard][minminguard]: Completely remove both the ads inside apps and the empty space caused by those ads.
- [YouTube AdAway][yta]:  Get rid of ads on the official YouTube App.
- [微X模块][wx]: 微信模块，功能强大。
- [畅玩微信][cwwx]: 微信模块新秀，功能丰富。
- [微信巫师][wxws]: 微信模块，项目开源，代码优秀。
- [MDWechat][mdwechat]: 微信美化模块，可以把微信整成MD风格。
- [应用变量][yybl]: 可以用来进行机型修改，比如王者荣耀高帧率；QQ空间修改小尾巴等。
- [音量增强器][ylzqq]: 网易云音乐模块，非常好用，低调。
- [微信学英语][wxxyy]: 自动把微信消息翻译为英语，非常实用。
- [情迁抢包][qqqb]: 微信QQ抢红包模块。
- [微信跳一跳助手][ttzs]: 微信跳一跳游戏辅助模块。
- [步数修改器][bsxg]: 运动步数修改模块。
- [模拟位置][mnwz]: 虚拟定位模块，稳定好用。
- [指纹支付][zwzf]: 对不支持指纹支付但系统本身有指纹的手机开启指纹支付的模块。
- [QQ精简模块 2.0][qqjj]: QQ模块，不仅可以精简QQ，还能防撤回，防闪照。
- [微信增强插件][wxzqcj]: 微信模块，VXP内最稳定的微信模块；如无特殊需求建议用这个。
- [QX模块][qx]: QQ模块，防撤回抢红包斗图一应俱全。
- [QQ斗图神器][qqdtsq]: 各种表情，斗图神器。
- [微信斗图神器][wxdtsq]: 斗图神器，微信用的。
- [大圣净化][dsjh]: 去广告神器，推荐使用。

真正能用的模块远不止这么多，要用的话可以自己测试；如果你发现某些模块可以用但不在上面的列表中，欢迎给我发个PR。

其他
-------

### GameGuardian

VirtualXposed也支持GG修改器，如果你需要用GG，那么请使用GG专版(可以在发布页面下载，带 For_GameGuardian后缀)。

[GG修改器使用视频教程](https://gameguardian.net/forum/gallery/image/437-no-root-via-virtualxposed-without-error-105-gameguardian/)

### VirusTotal

VirusTotal 还有一些其他的杀毒引擎检测到VirtualXposed有病毒，这一点我该不承认，而且我觉得这些愚蠢的杀毒引擎是在胡扯。请看[我的说明](https://github.com/android-hacker/VirtualXposed/issues/10).

而且，VirtualXposed是开源的，你可以直接查看代码；我可以打包票，VirtualXposed本身没有做任何有害的事情（但是它确实有这个能力，所以请不要下载不明来源的Xposed插件）。

如果你还是不放心，那么你可以使用 [0.8.7版本](https://github.com/android-hacker/VirtualXposed/releases/tag/0.8.7), 这个版本杀毒引擎的检测结果是安全的（简直就是扯淡）。


支持和加入
------------

目前VirtualXposed 还不完善，如果你对非ROOT下实现Xposed感兴趣；欢迎加入！你可以通过如下方式来支持：

1. 直接贡献代码，提供Feature，修复BUG！
2. 使用你拥有的手机，安装你常用的Xposed模块，反馈不可用情况；协助帮忙解决兼容性问题！
3. 提出体验上，功能上的建议，帮助完善VirtualXposed！

致谢
------

1. [VirtualApp](https://github.com/asLody/VirtualApp)
2. [Xposed](https://github.com/rovo89/Xposed)

[wx]: http://repo.xposed.info/module/com.fkzhang.wechatxposed
[qx]: http://repo.xposed.info/module/com.fkzhang.qqxposed
[wxws]: https://github.com/Gh0u1L5/WechatMagician/releases
[yybl]: https://www.coolapk.com/apk/com.sollyu.xposed.hook.model
[ylzqq]: https://github.com/bin456789/Unblock163MusicClient-Xposed/releases
[wxxyy]: https://www.coolapk.com/apk/com.hiwechart.translate
[qqqb]: http://repo.xposed.info/module/cn.qssq666.redpacket
[ttzs]: http://repo.xposed.info/module/com.emily.mmjumphelper
[mnwz]: https://www.coolapk.com/apk/com.rong.xposed.fakelocation
[zwzf]: https://github.com/android-hacker/Xposed-Fingerprint-pay/releases
[bsxg]: https://www.coolapk.com/apk/com.specher.sm
[mdwechat]: https://github.com/Blankeer/MDWechat
[wxzqcj]:https://github.com/firesunCN/WechatEnhancement
[qqjj]: https://www.coolapk.com/apk/me.zpp0196.qqsimple
[qqdtsq]: https://www.coolapk.com/apk/x.hook.qqemoji
[wxdtsq]: https://www.coolapk.com/apk/x.hook.emojihook
[dsjh]: https://wiki.ad-gone.com/archives/32
[xpl]: https://github.com/android-hacker/VirtualXposed/wiki/Privacy-control(XPrivacyLua)
[minminguard]: http://repo.xposed.info/module/tw.fatminmin.xposed.minminguard
[yta]: http://repo.xposed.info/module/ma.wanam.youtubeadaway
[xinsta]: http://repo.xposed.info/module/com.ihelp101.instagram
[cwwx]: http://repo.xposed.info/module/com.example.wx_plug_in3


