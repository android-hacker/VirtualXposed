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
        return isAndroidLevel(Build.VERSION_CODES.O);
    }

    public static boolean isPie() {
        return isAndroidLevel(Build.VERSION_CODES.P);
    }

    public static boolean isQ() {
        return isAndroidLevel(29);
    }

    public static boolean isR() {
        return isAndroidLevel(30);
    }

    private static boolean isAndroidLevelPreview(int level) {
        return (Build.VERSION.SDK_INT == level && getPreviewSDKInt() > 0)
                || Build.VERSION.SDK_INT > level;
    }

    private static boolean isAndroidLevel(int level) {
        return Build.VERSION.SDK_INT >= level;
    }
}