package io.virtualapp;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.github.moduth.blockcanary.BlockCanary;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.StubManifest;

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
            StubManifest.ENABLE_IO_REDIRECT = true;
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
            VirtualCore.get().setAppRequestListener(new VirtualCore.AppRequestListener() {
                @Override
                public void onRequestInstall(String path) {
                    Toast.makeText(VApp.this, "Install: " + path, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRequestUninstall(String pkg) {
                    Toast.makeText(VApp.this, "Uninstall: " + pkg, Toast.LENGTH_SHORT).show();

                }
            });
        } else if (VirtualCore.get().isVAppProcess()) {
            BlockCanary.install(this, new AppBlockCanaryContext());
            VirtualCore.get().setComponentDelegate(new MyComponentDelegate());
            VirtualCore.get().setPhoneInfoDelegate(new MyPhoneInfoDelegate());
            VirtualCore.get().setTaskDescriptionDelegate(new MyTaskDescriptionDelegate());
        }
    }

}
