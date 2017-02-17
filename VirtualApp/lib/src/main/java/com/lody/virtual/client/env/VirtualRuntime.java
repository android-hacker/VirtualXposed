package com.lody.virtual.client.env;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.VLog;

import mirror.android.ddm.DdmHandleAppName;
import mirror.android.ddm.DdmHandleAppNameJBMR1;

/**
 * @author Lody
 *         <p>
 *         <p/>
 *         Runtime Environment for App.
 */
public class VirtualRuntime {

    private static final Handler sUIHandler = new Handler(Looper.getMainLooper());

    private static String sInitialPackageName;
    private static String sProcessName;

    public static Handler getUIHandler() {
        return sUIHandler;
    }

    public static String getProcessName() {
        return sProcessName;
    }

    public static String getInitialPackageName() {
        return sInitialPackageName;
    }

    public static void setupRuntime(String processName, ApplicationInfo appInfo) {
        if (sProcessName != null) {
            return;
        }
        sInitialPackageName = appInfo.packageName;
        sProcessName = processName;
        mirror.android.os.Process.setArgV0.call(processName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DdmHandleAppNameJBMR1.setAppName.call(processName, 0);
        } else {
            DdmHandleAppName.setAppName.call(processName);
        }
    }

    public static <T> T crash(RemoteException e) throws RuntimeException {
        e.printStackTrace();
        if (VirtualCore.get().isVAppProcess()) {
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
        throw new DeadServerException(e);
    }


    public static boolean isArt() {
        return System.getProperty("java.vm.version").startsWith("2");
    }
}
