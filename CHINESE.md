[![VA banner](https://raw.githubusercontent.com/asLody/VirtualApp/master/banner.png)](https://github.com/asLody/VirtualApp)

关于
---
类似`LBE平行空间`， **VirtualApp**是一个**App虚拟引擎**的开源实现。
VirtualApp在你的App进程内创建一个虚拟空间，你可以在虚拟空间内任意的`安装`、`启动`和`卸载`APK，
这一切都与外部隔离，就如同一个`沙盒`。VirtualApp亦是一个`插件化框架`，运行在VirtualApp的插件**不需要任何的约束**。

背景
---

VirtualApp最早诞生于2015年初，最早它只是一个简单的插件化框架，但是随着Author对Android Framework层的感悟，
它最终发展成了一个`虚拟容器`。

讨论技术话题
----------

QQ Group: **553070909**

快速开始
------

1. VirtualApp 使用了 `@hide API`, 
因此你必须使用我们的 `android.jar` 来替换你已有的那个 **(Android-SDK/platforms/android-23/{android.jar})**. 

2. 在你的 `AndroidManifest.xml` 添加如下代码:
```xml
    <permission
        android:name="com.lody.virtual.permission.VIRTUAL_BROADCAST"
        android:protectionLevel="signature" />
    <uses-permission android:name="com.lody.virtual.permission.VIRTUAL_BROADCAST" />
    <service android:name="com.lody.virtual.client.stub.KeepService" android:process=":x"/>
    <provider
            android:process=":x"
            android:authorities="virtual.service.BinderProvider"
            android:name="com.lody.virtual.service.BinderProvider"
            android:exported="false" />
    <activity
            android:theme="@android:style/Theme.Translucent.NoTitleBar"
            android:name="com.lody.virtual.client.stub.ShortcutHandleActivity" android:exported="true"/>        
    <activity
            android:configChanges="mcc|mnc|locale|touchscreen|keyboard|keyboardHidden|navigation|orientation|screenLayout|uiMode|screenSize|smallestScreenSize|fontScale"
            android:name="com.lody.virtual.client.stub.StubActivity$C0" android:process=":p0" >
            <meta-data android:name="X-Identity" android:value="Stub-User"/>
    </activity>
    <provider
            android:process=":p0"
            android:authorities="virtual.client.stub.StubContentProvider0"
            android:name="com.lody.virtual.client.stub.StubContentProvider$C0"
            android:exported="false">
            <meta-data android:name="X-Identity" android:value="Stub-User"/>
    </provider>
    <!--and so on-->
```
3. 将你的Host和Plugins需要的**所有权限**加入到你的`AndroidManifest.xml`.

4. 前往你的Application并添加如下代码:
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

    @Override
    public void onCreate() {
        super.onCreate();
        VirtualCore.getCore().handleApplication(this);
        if (!VirtualCore.getCore().isVAppProcess()) {
            // Do some thing...
        }
    }
```

5. For **Install a virtual App**, use this function:
```java
    VirtualCore.getCore().installApp({APK PATH}, flags);
```

6. For **Launch a virtual App**, use this function:
```java
    VirtualCore.getCore().launchApp({PackageName});
```

7. For **uninstall a virtual App**, use this function:
```java
    VirtualCore.getCore().uninstallApp({PackageName});
```

8. If you need to get the `details of App`, use this function:
```java
    VirtualCore.getCore().findApp({PackageName});
```


文档
-------------

VirtualApp 目前暂时**没有文档**，Please read the fucking source code。

License
-------
LGPL 3.0

关于Author
------------

    Lody (imlody@foxmail.com)
