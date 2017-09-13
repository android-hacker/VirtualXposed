[![VA banner](https://raw.githubusercontent.com/asLody/VirtualApp/master/Logo.png)](https://github.com/asLody/VirtualApp)

简介
---
**VirtualApp**是一个**App虚拟化引擎**（简称`VA`）。

**VirtualApp已兼容Android 0(8.0 Preview)。**

VirtualApp在你的App内创建一个`虚拟空间`，你可以在虚拟空间内任意的`安装`、`启动`和`卸载`APK，这一切都与外部隔离，如同一个`沙盒`。

运行在`VA`中的APK无需在外部安装，即VA支持**免安装运行APK**。

VA目前被广泛应用于双开/多开，但它决不仅限于此，Android本身就是一个极其开放的平台，免安装运行APK这一Feature打开了太多太多的可能--------这都取决于你的想象力。

申明
---
**您无权将VirtualApp（原名ILoader）的APP模块作为您自己的APP上传到软件市场，一经发现，我们将起诉或报警。当您需要将VirtualApp（原名ILoader）用于商业用途时，请务必与授权负责人联系 QQ/微信：10890
购买授权是对我们最大的支持和鼓励，您将得到我们1vs1技术支持和帮助，并获得未开放的商业版本。**

请注意
-----
VirtualApp代码的更新频率非常快（`以小时为单位`），每一次代码的更新都有可能修复重大BUG，所以请 `watch` 本项目，并注意随时更新代码，以免给您带来损失！


已支持的加固(不断更新)
----------
* 360加固
* 腾讯加固
* 梆梆加固
* 梆梆企业版(12306客户端 Pass)
* 爱加密
* 百度加固
* 娜迦加固
* 乐变加固
* 网易易盾
* 通付盾
* (已支持的加固均可通过VA来脱壳，本技术不公开)


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

QQ群：562814070
