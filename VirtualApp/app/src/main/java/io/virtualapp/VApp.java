package io.virtualapp;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.helper.utils.VLog;

import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
public class VApp extends Application {

    private static VApp gDefault;

    public static VApp getApp() {
        return gDefault;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        gDefault = this;
        super.onCreate();
        if (VirtualCore.get().isMainProcess()) {
            Once.initialise(this);
        } else if (VirtualCore.get().isVAppProcess()) {
            VirtualCore.get().setComponentDelegate(new MyComponentDelegate());
            VirtualCore.get().setPhoneInfoDelegate(new MyPhoneInfoDelegate());
        }
    }

}
