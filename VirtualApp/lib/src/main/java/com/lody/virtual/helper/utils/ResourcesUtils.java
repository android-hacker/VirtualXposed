package com.lody.virtual.helper.utils;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class ResourcesUtils {

    public static void chmod(File dir, File apk) {
        try {
            //files/virtual_app/pkg/base.apk
            Process process = Runtime.getRuntime().exec("chmod -R 755 " + dir.getAbsolutePath());
            Log.e("chmod", apk + " " + process.waitFor());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                Process process = Runtime.getRuntime().exec("chmod -R 755 " + dir.getAbsolutePath());
//                Log.e("chmod", apk + " " + process.waitFor());
//            } else {
//                Process process = Runtime.getRuntime().exec("chmod 755 " + apk.getAbsolutePath());
//                Log.e("chmod", apk + " " + process.waitFor());
//            }
        } catch (Exception e) {
        }
    }

    public static void makeResources(File apk, File res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //TODO 把zip的asset，dex，lib，除外
            final File target = res;
            if (!TextUtils.equals(apk.getAbsolutePath(), res.getAbsolutePath())) {
                FileUtils.deleteDir(res);
                FileUtils.copyFile(apk, res);
//                try {
//                    Runtime.getRuntime().exec("ln -s " + apk.getAbsolutePath()+" "+res.getAbsolutePath());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
            chmod(target.getParentFile().getParentFile(), target);
        }
    }

}
