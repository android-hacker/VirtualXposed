package io.virtualapp;

import android.app.Application;
import android.content.Context;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.LocalProcessManager;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.interfaces.IProcessObserver;

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
        if (VirtualCore.getCore().isMainProcess()) {
            Once.initialise(this);
            LocalProcessManager.registerProcessObserver(new IProcessObserver.Stub() {
                @Override
                public void onProcessCreated(String pkg, String processName) throws RemoteException {
                    VLog.d("VProcess", "Process created: %s -> %s.", pkg, processName);
                }

                @Override
                public void onProcessDied(String pkg, String processName) throws RemoteException {
                    VLog.d("VProcess", "Process died: %s -> %s.", pkg, processName);
                }
            });
        }
    }

}
