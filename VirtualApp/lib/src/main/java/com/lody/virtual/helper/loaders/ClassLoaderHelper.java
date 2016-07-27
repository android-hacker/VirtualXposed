package com.lody.virtual.helper.loaders;

import com.lody.virtual.helper.proto.AppInfo;

import java.io.File;
import java.util.Collections;

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
            try {
                ClassLoaderInjectHelper.installDexes(classLoader, new File(appInfo.odexDir), Collections.singletonList(new File(appInfo.apkPath)));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return classLoader;
        }
    }

    private static ClassLoader getRoot() {
        return ClassLoader.getSystemClassLoader().getParent();
    }
}
