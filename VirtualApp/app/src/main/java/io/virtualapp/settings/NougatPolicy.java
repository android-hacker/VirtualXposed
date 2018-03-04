package io.virtualapp.settings;

import android.content.Context;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * Android 7.0 全量编译策略
 * Created by weishu on 17/6/12.
 */

public class NougatPolicy {

    static boolean fullCompile(Context context) {
        if (Build.VERSION.SDK_INT < 24) {
            return true;
        }
        try {
            Object pm = getPackageManagerBinderProxy();
            if (pm == null) {
                return false;
            }
            /*
            @Override
            public boolean performDexOptMode(String packageName,
            boolean checkProfiles, String targetCompilerFilter, boolean force) {
                int dexOptStatus = performDexOptTraced(packageName, checkProfiles,
                        targetCompilerFilter, force);
                return dexOptStatus != PackageDexOptimizer.DEX_OPT_FAILED;
            */

            final Method performDexOptMode = pm.getClass().getDeclaredMethod("performDexOptMode",
                    String.class, boolean.class, String.class, boolean.class);
            return (boolean) performDexOptMode.invoke(pm, context.getPackageName(), false, "speed", true);
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean clearCompileData(Context context) {
        boolean ret;
        try {
            Object pm = getPackageManagerBinderProxy();
            final Method performDexOpt = pm.getClass().getDeclaredMethod("performDexOpt", String.class,
                    boolean.class, int.class, boolean.class);
            ret = (Boolean) performDexOpt.invoke(pm, context.getPackageName(), false, 2 /*install*/, true);
        } catch (Throwable e) {
            ret = false;
        }
        return ret;
    }

    private static Object getPackageManagerBinderProxy() throws Exception {
        Class<?> activityThread = Class.forName("android.app.ActivityThread");
        final Method getPackageManager = activityThread.getDeclaredMethod("getPackageManager");
        return getPackageManager.invoke(null);
    }
}
