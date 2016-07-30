给`微商双开神器`的警告
----------------------
经过验证发现，`微商双开神器`将VirtualApp的`演示App`的界面改为绿色，并添加`微信支付`（售价28元），
广州市比目网络科技有限公司的这一敛财行为，严重侵害了作者的利益。请在1个月内下架你们的产品。
再次申明，VA可以使用于商业项目中，但这种赤裸裸的敛财行为，是严格禁止的。

[![VA banner](https://raw.githubusercontent.com/asLody/VirtualApp/master/banner.png)](https://github.com/asLody/VirtualApp)

[中文](CHINESE.md "中文")

About
-----
Likes `LBE Parallel Space`, **VirtualApp** is an open platform for Android that allows you to create a `Virtual Space`,
you can install and run apk inside. Beyond that, VirtualApp is also a `Plugin Framework`,
the plugins running on VirtualApp does not require any constraints.
VirtualApp does **not** require root, it is running on the `local process`.


Background
----------

VirtualApp was born in early 2015, Originally, it is just a simple plugin framework, 
But as time goes on,
the compatibility of it is getting better and better.
in the end, it evolved into a `Virtual Container`.


Get started
-----------

1. VirtualApp use the `@hide API`, 
so you must use our `android.jar` replace the old one **(Android-SDK/platforms/android-23/{android.jar})**. 

2. Add **all permissions** your host and your plugins need to use.

3. Goto your Application and insert the following code:
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

4. For **Install a virtual App**, use this function:
```java
    VirtualCore.getCore().installApp({APK PATH}, flags);
    
```

5. For **Launch a virtual App**, use this function:
```java
    VirtualCore.getCore().launchApp({PackageName});
```

6. For **uninstall a virtual App**, use this function:
```java
    VirtualCore.getCore().uninstallApp({PackageName});
```

7. If you need to get the `details of App`, use this function:
```java
    VirtualCore.getCore().findApp({PackageName});
```

Documentation
-------------

VirtualApp currently has **no documentation**, If you are interested in VirtualApp, please send email to me.

License
-------
LGPL 3.0

About Author
------------

    Lody (imlody@foxmail.com)
