package com.lody.virtual.helper.loaders;

import com.lody.virtual.helper.proto.AppInfo;

import dalvik.system.PathClassLoader;

/**
 * @author Lody
 */

public class ClassLoaderHelper {

    public static PathClassLoader create(AppInfo appInfo) {
        if (appInfo.dependSystem) {
            return new PathClassLoader(appInfo.apkPath, appInfo.libDir, getRoot());
        } else {
            PathClassLoader classLoader = new PathClassLoader(".", appInfo.libDir, getRoot());
            ClassLoaderInjectHelper.inject(classLoader, new DexAppClassLoader(appInfo));
            return classLoader;
        }
    }

    private static ClassLoader getRoot() {
        return ClassLoader.getSystemClassLoader().getParent();
    }
}
