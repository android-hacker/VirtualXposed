package com.lody.virtual.helper.utils;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Created by 247321453 on 2016/7/17.
 */
public class OSUtils {
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    static OSUtils sOSUtils;

    public static OSUtils getInstance() {
        if (sOSUtils == null) {
            synchronized (OSUtils.class) {
                if (sOSUtils == null) {
                    sOSUtils = new OSUtils();
                }
            }
        }
        return sOSUtils;
    }

    private OSUtils() {
        Properties properties = null;
        try {
            properties = new Properties();
            properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        } catch (IOException e) {
            properties = null;
        }
        if (properties != null) {
            emui = !TextUtils.isEmpty(properties.getProperty(KEY_EMUI_VERSION_CODE));
            miuiVersion = properties.getProperty(KEY_MIUI_VERSION_CODE);
            miui = !TextUtils.isEmpty(miuiVersion)
                    || !TextUtils.isEmpty(properties.getProperty(KEY_MIUI_VERSION_NAME))
                    || !TextUtils.isEmpty(properties.getProperty(KEY_MIUI_INTERNAL_STORAGE));
        }
        flyme = hasFlyme();
    }

    private boolean emui;
    private boolean miui;
    private boolean flyme;
    private String miuiVersion;

    public String getMiuiVersion() {
        return miuiVersion;
    }

    public boolean isEmui() {
        return emui;
    }

    public boolean isMiui() {
        return miui;
    }

    public boolean isFlyme() {
        return flyme;
    }

    private boolean hasFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }
}
