package com.lody.virtual.helper.utils;

import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;

import java.io.File;


public class ResourcesUtils {
    private static final String TAG = ResourcesUtils.class.getSimpleName();

    private static final File APK_DIRECTORY;

    static {
        APK_DIRECTORY = ensureCreated(new File(VirtualCore.get().getContext().getFilesDir(), "virtual_res"));
    }

    private static File ensureCreated(File folder) {
        if (!folder.exists() && !folder.mkdirs()) {
            VLog.w(TAG, "Unable to create the directory: %s.", folder.getPath());
        }
        return folder;

    }

    public static File getPackageResourcePath(String packgeName) {
        File dir = ensureCreated(APK_DIRECTORY);
        return new File(dir, packgeName + "-res.apk");
    }

    public static void make(String packgeName,File apk) {
        File res = getPackageResourcePath(packgeName);
        String apkPath = apk.getAbsolutePath();
        String cmd = "ln -s " + res.getAbsolutePath()+" "+apkPath;
        apk.renameTo(res);
        try {
            Runtime.getRuntime().exec(cmd);
        }catch (Exception e){
            res = getPackageResourcePath(packgeName);
            apk = new File(apkPath);
            FileUtils.copyFile(res, apk);
        }

        try {
            Process process = Runtime.getRuntime().exec("chmod -R 755 " + res.getAbsolutePath());
            Log.e("chmod", apk + " " + process.waitFor());
        } catch (Exception e) {
        }
    }

}
