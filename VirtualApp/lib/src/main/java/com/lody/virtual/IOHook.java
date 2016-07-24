package com.lody.virtual;

/**
 * Created by Xfast on 2016/7/21.
 */
public class IOHook {
    private static boolean sLoaded;
    static {
        try {
            System.loadLibrary("iohook");
            sLoaded = true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static native String getRedirectedPath(String orgPath);

    public static native String restoreRedirectedPath(String redirectedPath);

//    public static native void rejectPath(String path);

    public static boolean init() {
        return sLoaded;
    }

    public static native void redirect(String orgPath, String newPath);

    public static native void hook();
}
