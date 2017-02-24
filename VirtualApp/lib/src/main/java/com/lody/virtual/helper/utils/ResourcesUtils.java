package com.lody.virtual.helper.utils;

import android.text.TextUtils;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;

import java.io.File;
import java.io.IOException;


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

    /***
     * 假如资源文件不存在，则false
     * @param packageName
     * @param apk
     * @return
     */
    public static boolean check(String packageName,File apk){
            File res = getPackageResourcePath(packageName);
        if(!res.exists()){
            VLog.d("ResCheck", "no find res");
            return false;
        }
        if(apk.exists()){
            VLog.d("ResCheck", "apk exists");
        }
        String cmd = "ln -s " + res.getAbsolutePath()+" "+apk.getAbsolutePath();
        try {
            VLog.d("ResCheck", "link "+cmd);
            Runtime.getRuntime().exec(cmd);
        }catch (Exception e){
            FileUtils.copyFile(res, apk);
        }
        return true;
    }

    public static void make(String packgeName,File apk) {
        //需要检查apk
        try {
            if(!TextUtils.equals(apk.getAbsolutePath(), apk.getCanonicalPath())){
                //是link文件
                VLog.d("ResCheck","is link "+apk.getAbsolutePath());
                return;
            }
        } catch (IOException e) {
        }
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
            VLog.d("ResCheck", apk + " " + process.waitFor());
        } catch (Exception e) {
        }
    }

}
