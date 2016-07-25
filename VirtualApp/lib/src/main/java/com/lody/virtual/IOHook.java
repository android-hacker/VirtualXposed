package com.lody.virtual;

import android.os.Build;

/**
 * Created by Xfast on 2016/7/21.
 */
public class IOHook {
    private static boolean sLoaded;

    static {
        try {
            System.loadLibrary("iohook");
            sLoaded = true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String getRedirectedPath(String orgPath) {
        try {
            return nativeGetRedirectedPath(orgPath);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String restoreRedirectedPath(String orgPath) {
        try {
            return nativeRestoreRedirectedPath(orgPath);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void redirect(String orgPath, String newPath) {
        try {
            nativeRedirect(orgPath, newPath);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void hook() {
        try {
            nativeHook(Build.VERSION.SDK_INT);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //    private static native void nativeRejectPath(String path);

    private static native String nativeRestoreRedirectedPath(String redirectedPath);

    private static native String nativeGetRedirectedPath(String orgPath);


    public static boolean init() {
        return sLoaded;
    }

    private static native void nativeRedirect(String orgPath, String newPath);

    private static native void nativeHook(int apiLevel);
}
