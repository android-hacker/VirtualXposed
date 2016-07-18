package com.lody.virtual.helper.utils;

import android.os.Build;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by 247321453 on 2016/7/17.
 */
public class OSUtils {
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    static BuildProperties prop;

    public static BuildProperties getBuildProperties() {
        if (prop == null) {
            synchronized (OSUtils.class) {
                if (prop == null) {
                    try {
                        prop = BuildProperties.newInstance();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return prop;
    }

    private static boolean isPropertiesExist(String... keys) {
        try {
            BuildProperties prop = getBuildProperties();
            for (String key : keys) {
                String str = prop.getProperty(key);
                if (str != null)
                    return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isEMUI() {
        return isPropertiesExist(KEY_EMUI_VERSION_CODE);
    }

    public static boolean isMIUI() {
        return isPropertiesExist(KEY_MIUI_VERSION_CODE, KEY_MIUI_VERSION_NAME, KEY_MIUI_INTERNAL_STORAGE);
    }

    public static String getMIUIVersion(){
        return getBuildProperties().getProperty(KEY_MIUI_VERSION_CODE);
    }
    public static boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }
}
