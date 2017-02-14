package com.lody.virtual.helper.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ResourcesUtils {

    public static void makeResources(File apk, File res) {
        //TODO 把zip的asset，dex，lib，除外
        if (!TextUtils.equals(apk.getAbsolutePath(), res.getAbsolutePath())) {
            FileUtils.deleteDir(res);
            FileUtils.copyFile(apk, res);
        }
//        try {
//            Process process =Runtime.getRuntime().exec("chmod 744 " + res.getAbsolutePath());
////            Process process = Runtime.getRuntime().exec("chown :sdcard_rw " + res.getAbsolutePath());
//            Log.e("chmod", apk + " " + process.waitFor());
//        } catch (Exception e) {
//        }
    }

}
