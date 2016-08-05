package com.lody.virtual.helper.loaders;

import com.lody.virtual.helper.proto.AppInfo;

import java.io.File;
import java.util.Collections;

import dalvik.system.PathClassLoader;

/**
 * @author Lody
 */

public class ClassLoaderHelper {

    public static class AppClassLoader extends PathClassLoader {

        public AppClassLoader(String dexPath, ClassLoader parent) {
            super(dexPath, parent);
        }

        public AppClassLoader(String dexPath, String libraryPath, ClassLoader parent) {
            super(dexPath, libraryPath, parent);
        }
    }

    public static AppClassLoader create(AppInfo appInfo) {
        if (appInfo.dependSystem) {
            return new AppClassLoader(appInfo.apkPath, appInfo.libDir, getRoot());
        } else {
            AppClassLoader classLoader = new AppClassLoader(".", appInfo.libDir, getRoot());
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
