package com.lody.virtual;

import android.os.Build;

import com.lody.virtual.helper.utils.VLog;

/**
 * Created by Xfast on 2016/7/21.
 */
public class IOHook {

    private static final String TAG = IOHook.class.getSimpleName();

    private static boolean sLoaded;

    static {
        try {
            System.loadLibrary("iohook");
            sLoaded = true;
        } catch (Throwable e) {
            VLog.e(TAG, VLog.getStackTraceString(e));
        }
    }

    public static String getRedirectedPath(String orgPath) {
        try {
            return nativeGetRedirectedPath(orgPath);
        } catch (Throwable e) {
            VLog.e(TAG, VLog.getStackTraceString(e));
        }
        return null;
    }

    public static String restoreRedirectedPath(String orgPath) {
        try {
            return nativeRestoreRedirectedPath(orgPath);
        } catch (Throwable e) {
            VLog.e(TAG, VLog.getStackTraceString(e));
        }
        return null;
    }

    public static void redirect(String orgPath, String newPath) {
        try {
            nativeRedirect(orgPath, newPath);
        } catch (Throwable e) {
            VLog.e(TAG, VLog.getStackTraceString(e));
        }
    }

    public static void hook() {
        try {
            nativeHook(Build.VERSION.SDK_INT);
        } catch (Throwable e) {
            VLog.e(TAG, VLog.getStackTraceString(e));
        }
    }

    /**
     * this is called by JNI.
     * @param pid killed pid
     * @param signal signal
     */
    public static void onKillProcess(int pid, int signal) {
        VLog.e(TAG, "onKillProcess: pid=" + pid + ", signal=" + signal);
        if (pid == android.os.Process.myPid()) {
            VLog.e(TAG, VLog.getStackTraceString(new Throwable()));
        }
    }


    //    private static native void nativeRejectPath(String path);

    private static native String nativeRestoreRedirectedPath(String redirectedPath);

    private static native String nativeGetRedirectedPath(String orgPath);

    private static native void nativeRedirect(String orgPath, String newPath);

    private static native void nativeHook(int apiLevel);
}
