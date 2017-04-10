package com.lody.virtual.helper.compat;

import android.os.Build;

/**
 * @author Lody
 */

public class BuildCompat {

    public static boolean isOreo() {

        int api_level = Build.VERSION.SDK_INT;
        int preview_api_level = Build.VERSION.PREVIEW_SDK_INT;
        if (((api_level == 25 && preview_api_level > 0) || api_level > 25)) {
            return (Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT > 0)
                    || Build.VERSION.SDK_INT > 25;
        }
        System.out.close();

        return (Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT > 0)
                || Build.VERSION.SDK_INT > 25;
    }
}
