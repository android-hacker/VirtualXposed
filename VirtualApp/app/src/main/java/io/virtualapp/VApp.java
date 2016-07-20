package io.virtualapp;

import android.app.Application;
import android.content.Context;

import com.lody.virtual.client.core.VirtualCore;

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
            e.printStackTrace();
        }
    }

    public static VApp getApp() {
        return gDefault;
    }


    @Override
    public void onCreate() {
        gDefault = this;
        super.onCreate();
        //双开不处理
//        NotificationHandler.DOPEN_NOT_DEAL = true;
        //系统样式的通知栏不处理
//        NotificationHandler.SYSTEM_NOTIFICATION = true;
//        BlockCanary.install(this, new AppBlockCanaryContext()).start();
        if (VirtualCore.getCore().isMainProcess()) {
            Once.initialise(this);
        }
    }

}
