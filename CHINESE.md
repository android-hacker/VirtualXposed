[![VA banner](https://raw.githubusercontent.com/asLody/VirtualApp/master/banner.png)](https://github.com/asLody/VirtualApp)
关于
---
**VirtualApp**是一个**App虚拟引擎**的完整实现（简称`VA`）。
VirtualApp允许你在App内创建一个虚拟空间，你可以在虚拟空间内任意的`安装`、`启动`和`卸载`APK，这一切都与外部隔离，就如同一个`沙盒`。

运行在`VA`中的APK无需在外部安装，即VA支持**免安装运行APK**。

注意
-----
VirtualApp已申请国家专利, 并获得软件著作权保护, 当你的行为对项目或是项目作者构成利益冲突时,我们将追究法律责任.
使用说明
----------

1. 将你的Host和Plugins需要的**所有权限**加入到你的`AndroidManifest.xml`.

2. 前往你的Application并添加如下代码:
```java
    @Override
    protected void attachBaseContext(Context base) {
        StubManifest.STUB_CP_AUTHORITY = BuildConfig.APPLICATION_ID + "." + StubManifest.STUB_DEF_AUTHORITY;
        ServiceManagerNative.SERVICE_CP_AUTH = BuildConfig.APPLICATION_ID + "." + ServiceManagerNative.SERVICE_DEF_AUTH;
        super.attachBaseContext(base);
        try {
            VirtualCore.getCore().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
```
3. 将App添加到虚拟引擎:
```java
    VirtualCore.getCore().installApp({APK PATH}, flags);
```
4. 启动App:
```java
    VirtualCore.getCore().launchApp({PackageName});
```
5. 移除App:
```java
    VirtualCore.getCore().uninstallApp({PackageName});
```
6. 该App有关的信息:
```java
    VirtualCore.getCore().findApp({PackageName});
```


文档
-------------

<https://github.com/prife/VirtualAppDoc>
 

License
-------
GPL 3.0

关于Author
------------

    Lody (imlody@foxmail.com)
