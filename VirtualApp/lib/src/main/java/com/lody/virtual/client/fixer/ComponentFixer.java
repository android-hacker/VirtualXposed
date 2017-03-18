package com.lody.virtual.client.fixer;

import android.content.pm.ComponentInfo;
import android.text.TextUtils;

import com.lody.virtual.server.pm.PackageSetting;

/**
 * @author Lody
 */

public class ComponentFixer {

    public static String fixComponentClassName(String pkgName, String className) {
        if (className != null) {
            if (className.charAt(0) == '.') {
                return pkgName + className;
            }
            return className;
        }
        return null;
    }

    public static void fixComponentInfo(PackageSetting setting, ComponentInfo info, int userId) {
        if (info != null) {
            if (TextUtils.isEmpty(info.processName)) {
                info.processName = info.packageName;
            }
            info.name = fixComponentClassName(info.packageName, info.name);
            if (info.processName == null) {
                info.processName = info.applicationInfo.processName;
            }
        }
    }

}
