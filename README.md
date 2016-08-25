[![VA banner](https://raw.githubusercontent.com/asLody/VirtualApp/master/banner.png)](https://github.com/asLody/VirtualApp)

[中文](CHINESE.md "中文")

About
-----
**VirtualApp** is an open platform for Android that allows you to create a `Virtual Space`,
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

1. Add **all permissions** your host and your plugins need to use.

2. Goto your Application and insert the following code:
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

3. For **Install a virtual App**, use this function:
```java
    VirtualCore.getCore().installApp({APK PATH}, flags);
    
```

4. For **Launch a virtual App**, use this function:
```java
    VirtualCore.getCore().launchApp({PackageName});
```

5. For **uninstall a virtual App**, use this function:
```java
    VirtualCore.getCore().uninstallApp({PackageName});
```

6. If you need to get the `details of App`, use this function:
```java
    VirtualCore.getCore().findApp({PackageName});
```

Documentation
-------------

VirtualApp currently has **no documentation**, If you are interested in VirtualApp, please send email to me.

License
-------
GPL 3.0

About Author
------------

    Lody (imlody@foxmail.com)
