[![VA banner](https://raw.githubusercontent.com/asLody/VirtualApp/master/Logo.png)](https://github.com/asLody/VirtualApp)

简介
---
**VirtualApp**是一个**App虚拟化引擎**（简称`VA`）。

**VirtualApp已兼容Android 0(8.0 Preview)。**

VirtualApp在你的App内创建一个`虚拟空间`，你可以在虚拟空间内任意的`安装`、`启动`和`卸载`APK，这一切都与外部隔离，如同一个`沙盒`。

运行在`VA`中的APK无需在外部安装，即VA支持**免安装运行APK**。

VA目前被广泛应用于双开/多开、应用市场、模拟定位、一键改机、隐私保护、游戏修改、自动化测试、无感知热更新等技术领域，但它决不仅限于此，Android本身就是一个极其开放的平台，免安装运行APK这一Feature打开了无限可能--------这都取决于您的想象力。

申明
---
当您需要将VirtualApp用于**商业用途**时，请务必联系QQ：10890 购买商业授权。您如果未经授权将VirtualApp的App模块作为您自己的App用于牟利或上传软件市场，我们取证后将直接报警（侵犯著作权罪）。购买商业授权是对我们最大的支持和认可，我们将投入更多精力和时间来不断完善优化VirtualApp，作为购买商业授权的回报，您可以获得未开放的商业版本和1vs1的支持（技术、运营、预警）！同时我们也支持基于VirtualApp的APP订制开发，请联系：QQ：10890 洽谈。

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
QQ群：598536
