package com.lody.virtual.server;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Shared singleton I/O thread for the system.  This is a thread for non-background
 * service operations that can potential block briefly on network IO operations
 * (not waiting for data itself, but communicating with network daemons).
 */
public final class IoThread extends HandlerThread {
    private static IoThread sInstance;
    private static Handler sHandler;

    private IoThread() {
        super("virtual.android.io", android.os.Process.THREAD_PRIORITY_DEFAULT);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new IoThread();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static IoThread get() {
        synchronized (IoThread.class) {
            ensureThreadLocked();
            return sInstance;
        }
    }

    public static Handler getHandler() {
        synchronized (IoThread.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }
}
