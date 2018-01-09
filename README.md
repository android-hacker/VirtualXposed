[![VA banner](https://raw.githubusercontent.com/asLody/VirtualApp/master/Logo.png)](https://github.com/asLody/VirtualApp)

[中国人猛戳这里](CHINESE.md "中文")

About
-----
**VirtualApp** is an open platform for Android that allows you to create a `Virtual Space`,
you can install and run apk inside. Beyond that, VirtualApp is also a `Plugin Framework`,
the plugins running on VirtualApp does not require any constraints.
VirtualApp does **not** require root, it is running on the `local process`.

NOTICE
-------
**This project has been authorized by the business.**

**You are not allowed to modify the app module and put to the software market, if you do that, The consequences you know :)**

**VirtualApp is not free, If you need to use the lib code, please send email to me :)**

Background
----------

VirtualApp was born in early 2015, Originally, it is just a simple plugin framework, 
But as time goes on,
the compatibility of it is getting better and better.
in the end, it evolved into a `Virtual Container`.


Get started
-----------
If you use latest android studio (version 2.0 or above), please disable `Instant Run`.
Open `Setting | Build,Exception,Deployment`, and disable `Enable Instant Run to hot swap...`

**Goto your Application and insert the following code:**
```java
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
```

**Install a virtual App:**
```java
    VirtualCore.get().installPackage({APK PATH}, flags);
    
```

**Launch a virtual App:**
```java
    //VirtualApp support multi-user-mode which can run multiple instances of a same app.
    //if you don't need this feature, just set `{userId}` to 0.
    Intent intent = VirtualCore.get().getLaunchIntent({PackageName}, {userId});
    VActivityManager.get().startActivity(intent, {userId});
```

**Uninstall a virtual App:**
```java
    VirtualCore.get().uninstallPackage({PackageName});
```

More details, please read the source code of demo app, :-)

Documentation
-------------

VirtualApp currently has **no documentation**, If you are interested in VirtualApp, please send email to me.

Contact us
------------

    zl@aluohe.com
