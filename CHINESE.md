[![VA banner](https://raw.githubusercontent.com/asLody/VirtualApp/master/Logo.png)](https://github.com/asLody/VirtualApp)

简介
---
**VirtualApp**是一个**App虚拟化引擎**（简称`VA`）。

VirtualApp在你的App内创建一个`虚拟空间`，你可以在虚拟空间内任意的`安装`、`启动`和`卸载`APK，这一切都与外部隔离，如同一个`沙盒`。

运行在`VA`中的APK无需在外部安装，即VA支持**免安装运行APK**。

VA目前被广泛应用于双开/多开，但它决不仅限于此，Android本身就是一个极其开放的平台，免安装运行APK这一Feature打开了太多太多的可能--------这都取决于你的想象力。

谁在使用本项目
-------------
* 地铁跑酷
* 骑士助手
* X-Phone
* Dual app
* 全民点游

已支持的加固
----------
* 360加固
* 腾讯加固
* 梆梆加固
* 爱加密
* (非VMP的加固都可以通过VA来脱壳，但目前本技术尚不公开)

注意
-----
您无权免费使用项目，VirtualApp已申请国家专利, 并获得软件著作权保护, 当你的行为对项目或是项目作者构成利益冲突时,我们将追究法律责任。若需使用本项目，请与作者联系。

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
