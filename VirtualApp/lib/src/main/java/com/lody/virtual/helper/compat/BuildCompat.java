package com.lody.virtual.helper.compat;

import android.os.Build;

/**
 * @author Lody
 */

public class BuildCompat {

    public static int getPreviewSDKInt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return Build.VERSION.PREVIEW_SDK_INT;
            } catch (Throwable e) {
                // ignore
            }
        }
        return 0;
    }

    public static boolean isOreo() {

        return (Build.VERSION.SDK_INT == 25 && getPreviewSDKInt() > 0)
                || Build.VERSION.SDK_INT > 25;
    }

    public static boolean isQ() {
        final int SDK = Build.VERSION.SDK_INT;
        final int Q = 29;
        return SDK == Q - 1 && getPreviewSDKInt() > 0 || Build.VERSION.SDK_INT >= Q;
    }

}