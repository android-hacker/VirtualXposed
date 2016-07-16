package io.virtualapp;

import android.app.Application;
import android.content.Context;

import com.lody.virtual.client.core.VirtualCore;

import io.virtualapp.ui.NotificationHandler;
import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
public class VApp extends Application {

    private static VApp gDefault;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            VirtualCore.getCore().startup(base);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static VApp getApp() {
        return gDefault;
    }


    @Override
    public void onCreate() {
        gDefault = this;
        super.onCreate();
        VirtualCore.getCore().setINotificationHandler(new NotificationHandler());
//        BlockCanary.install(this, new AppBlockCanaryContext()).start();
        VirtualCore.getCore().handleApplication(this);
        if (VirtualCore.getCore().isMainProcess()) {
            Once.initialise(this);
        }
    }
}
