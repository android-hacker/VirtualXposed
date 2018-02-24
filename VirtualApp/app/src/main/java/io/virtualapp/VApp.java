package io.virtualapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.utils.DeviceUtil;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.fabric.sdk.android.Fabric;
import io.virtualapp.delegate.MyAppRequestListener;
import io.virtualapp.delegate.MyComponentDelegate;
import io.virtualapp.delegate.MyPhoneInfoDelegate;
import io.virtualapp.delegate.MyTaskDescriptionDelegate;
import io.virtualapp.update.VAVersionService;
import jonathanfinerty.once.Once;
import me.weishu.exposed.LogcatService;

/**
 * @author Lody
 */
public class VApp extends MultiDexApplication {

    private static final String TAG = "VApp";

    public static final String XPOSED_INSTALLER_PACKAGE = "de.robv.android.xposed.installer";

    private static VApp gApp;
    private SharedPreferences mPreferences;

    public static VApp getApp() {
        return gApp;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mPreferences = base.getSharedPreferences("va", Context.MODE_MULTI_PROCESS);
        VASettings.ENABLE_IO_REDIRECT = true;
        VASettings.ENABLE_INNER_SHORTCUT = false;
        try {
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        gApp = this;
        super.onCreate();
        VirtualCore virtualCore = VirtualCore.get();
        virtualCore.initialize(new VirtualCore.VirtualInitializer() {

            @Override
            public void onMainProcess() {
                Once.initialise(VApp.this);

                Fabric.with(VApp.this, new Crashlytics());

                boolean isXposedInstalled = false;
                try {
                    isXposedInstalled = VirtualCore.get().isAppInstalled(XPOSED_INSTALLER_PACKAGE);
                    File oldXposedInstallerApk = getFileStreamPath("XposedInstaller_1_24.apk");
                    if (oldXposedInstallerApk.exists()) {
                        VirtualCore.get().uninstallPackage(XPOSED_INSTALLER_PACKAGE);
                        oldXposedInstallerApk.delete();
                        isXposedInstalled = false;
                        Log.d(TAG, "remove xposed installer success!");
                    }
                } catch (Throwable e) {
                    VLog.d(TAG, "remove xposed install failed.", e);
                }

                if (!isXposedInstalled) {
                    File xposedInstallerApk = getFileStreamPath("XposedInstaller_1_31.apk");
                    if (!xposedInstallerApk.exists()) {
                        InputStream input = null;
                        OutputStream output = null;
                        try {
                            input = getApplicationContext().getAssets().open("XposedInstaller_3.1.5.apk_");
                            output = new FileOutputStream(xposedInstallerApk);
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = input.read(buffer)) > 0) {
                                output.write(buffer, 0, length);
                            }
                        } catch (Throwable e) {
                            VLog.e(TAG, "copy file error", e);
                        } finally {
                            FileUtils.closeQuietly(input);
                            FileUtils.closeQuietly(output);
                        }
                    }

                    if (xposedInstallerApk.isFile() && !DeviceUtil.isMeizuBelowN()) {
                        VirtualCore.get().installPackage(xposedInstallerApk.getPath(), InstallStrategy.TERMINATE_IF_EXIST);
                    }
                }

                // check for update
                new android.os.Handler().postDelayed(() ->
                        VAVersionService.checkUpdate(VApp.this), 10000);
            }

            @Override
            public void onVirtualProcess() {
                //listener components
                virtualCore.setComponentDelegate(new MyComponentDelegate());
                //fake phone imei,macAddress,BluetoothAddress
                virtualCore.setPhoneInfoDelegate(new MyPhoneInfoDelegate());
                //fake task description's icon and title
                virtualCore.setTaskDescriptionDelegate(new MyTaskDescriptionDelegate());
                virtualCore.setCrashHandler((t, e) -> {
                    Log.i(TAG, "uncaught :" + t, e);
                    if (t == Looper.getMainLooper().getThread()) {
                        System.exit(0);
                    } else {
                        Log.e(TAG, "ignore uncaught exception of thread: " + t);
                    }
                });
                // ensure the logcat service alive when every virtual process start.
                LogcatService.start(VApp.this, VEnvironment.getDataUserPackageDirectory(0, XPOSED_INSTALLER_PACKAGE));
            }

            @Override
            public void onServerProcess() {
                virtualCore.setAppRequestListener(new MyAppRequestListener(VApp.this));
                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqq");
                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqqi");
                virtualCore.addVisibleOutsidePackage("com.tencent.minihd.qq");
                virtualCore.addVisibleOutsidePackage("com.tencent.qqlite");
                virtualCore.addVisibleOutsidePackage("com.facebook.katana");
                virtualCore.addVisibleOutsidePackage("com.whatsapp");
                virtualCore.addVisibleOutsidePackage("com.tencent.mm");
                virtualCore.addVisibleOutsidePackage("com.immomo.momo");
            }
        });
    }

    public static SharedPreferences getPreferences() {
        return getApp().mPreferences;
    }

}
