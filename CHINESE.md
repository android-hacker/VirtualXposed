[![VA banner](https://raw.githubusercontent.com/asLody/VirtualApp/master/Logo.png)](https://github.com/asLody/VirtualApp)

警告
---
** 近期有不少骗子贩卖外挂，当用户付了钱以后，骗子就将VirtualApp的链接发给你，请不要相信这种骗局！作者已因为此事背了无数的锅!


简介
---
**VirtualApp**是一个**App虚拟化引擎**（简称`VA`）。

**VirtualApp已兼容Android 0(8.0 Preview)。**

VirtualApp在你的App内创建一个`虚拟空间`，你可以在虚拟空间内任意的`安装`、`启动`和`卸载`APK，这一切都与外部隔离，如同一个`沙盒`。

运行在`VA`中的APK无需在外部安装，即VA支持**免安装运行APK**。

VA目前被广泛应用于双开/多开，但它决不仅限于此，Android本身就是一个极其开放的平台，免安装运行APK这一Feature打开了太多太多的可能--------这都取决于你的想象力。

申明
---
**您没有权利将VirtualApp的app模块作为您自己的app上架到软件市场，一经发现，后果你懂的。**

**当您需要将VA用于商业途径时，需要进行授权，因此请务必与作者联系（联系方式见下）。**

请注意
-----
VirtualApp代码的更新频率非常快（`以小时为单位`），每一次代码的更新都有可能修复重大BUG，所以请 `watch` 本项目，并注意随时更新代码，以免给您带来损失！


已支持的加固
----------
* 360加固
* 腾讯加固
* 梆梆加固
* 爱加密
* 百度加固
* 娜迦加固
* (非VMP的加固都可以通过VA来脱壳，但目前本技术尚不公开)


在VA使用Google服务
-----------
VA支持运行官方的Google服务套件，同时我们也提供了对`MicroG`的支持。

您可以通过在VA中安装`MicroG`来支持`Google服务`，

这样，即使外部没有Google服务，用户也可以在VA中享受Google服务。

MicroG套件可在此下载：[Download MicroG](https://microg.org/download.html)

MicroG的必要模块：
* Services Core
* Services Framework Proxy
* Store

如果您需要在VA中使用官方的Google服务套件（外部已安装的前提下），

则可以通过 `GmsSupport.installGms(userId)` 来安装。

##### 注意，您不能同时安装MicroGms和官方的Gms。


使用说明
----------

**前往你的Application并添加如下代码:**
```java
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            VirtualCore.getCore().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
```
**安装App:**
```java
    VirtualCore.getCore().installApp({APK PATH}, flags);
```
**启动App:**
```java
    VirtualCore.getCore().launchApp({PackageName});
```
**移除App:**
```java
    VirtualCore.getCore().uninstallApp({PackageName});
```
**该App的基本信息:**
```java
    VirtualCore.getCore().findApp({PackageName});
```

License
-------
GPL 3.0

技术支持
------------
Lody (imlody@foxmail.com)
QQ/WeChat (382816028)
