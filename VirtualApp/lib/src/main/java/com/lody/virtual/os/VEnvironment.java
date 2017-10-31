package com.lody.virtual.os;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;

import java.io.File;

/**
 * @author Lody
 */

public class VEnvironment {

    private static final String TAG = VEnvironment.class.getSimpleName();

    private static final File ROOT;
    private static final File DATA_DIRECTORY;
    private static final File USER_DIRECTORY;
    private static final File DALVIK_CACHE_DIRECTORY;

    static {
        File host = new File(getContext().getApplicationInfo().dataDir);
        // Point to: /
        ROOT = ensureCreated(new File(host, "virtual"));
        // Point to: /data/
        DATA_DIRECTORY = ensureCreated(new File(ROOT, "data"));
        // Point to: /data/user/
        USER_DIRECTORY = ensureCreated(new File(DATA_DIRECTORY, "user"));
        // Point to: /opt/
        DALVIK_CACHE_DIRECTORY = ensureCreated(new File(ROOT, "opt"));
    }

    public static void systemReady() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                FileUtils.chmod(ROOT.getAbsolutePath(), FileUtils.FileMode.MODE_755);
                FileUtils.chmod(DATA_DIRECTORY.getAbsolutePath(), FileUtils.FileMode.MODE_755);
                FileUtils.chmod(getDataAppDirectory().getAbsolutePath(), FileUtils.FileMode.MODE_755);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static Context getContext() {
        return VirtualCore.get().getContext();
    }

    private static File ensureCreated(File folder) {
        if (!folder.exists() && !folder.mkdirs()) {
            VLog.w(TAG, "Unable to create the directory: %s.", folder.getPath());
        }
        return folder;
    }

    public static File getDataUserPackageDirectory(int userId,
                                                   String packageName) {
        return ensureCreated(new File(getUserSystemDirectory(userId), packageName));
    }

    public static File getPackageResourcePath(String packgeName) {
        return new File(getDataAppPackageDirectory(packgeName), "base.apk");
    }

    public static File getDataAppDirectory() {
        return ensureCreated(new File(getDataDirectory(), "app"));
    }

    public static File getUidListFile() {
        return new File(getSystemSecureDirectory(), "uid-list.ini");
    }

    public static File getBakUidListFile() {
        return new File(getSystemSecureDirectory(), "uid-list.ini.bak");
    }

    public static File getAccountConfigFile() {
        return new File(getSystemSecureDirectory(), "account-list.ini");
    }

    public static File getVirtualLocationFile() {
        return new File(getSystemSecureDirectory(), "virtual-loc.ini");
    }

    public static File getDeviceInfoFile() {
        return new File(getSystemSecureDirectory(), "device-info.ini");
    }

    public static File getPackageListFile() {
        return new File(getSystemSecureDirectory(), "packages.ini");
    }

    /**
     * @return Virtual storage config file
     */
    public static File getVSConfigFile() {
        return new File(getSystemSecureDirectory(), "vss.ini");
    }

    public static File getBakPackageListFile() {
        return new File(getSystemSecureDirectory(), "packages.ini.bak");
    }


    public static File getJobConfigFile() {
        return new File(getSystemSecureDirectory(), "job-list.ini");
    }

    public static File getDalvikCacheDirectory() {
        return DALVIK_CACHE_DIRECTORY;
    }

    public static File getOdexFile(String packageName) {
        return new File(DALVIK_CACHE_DIRECTORY, "data@app@" + packageName + "-1@base.apk@classes.dex");
    }

    public static File getDataAppPackageDirectory(String packageName) {
        return ensureCreated(new File(getDataAppDirectory(), packageName));
    }

    public static File getAppLibDirectory(String packageName) {
        return ensureCreated(new File(getDataAppPackageDirectory(packageName), "lib"));
    }

    public static File getPackageCacheFile(String packageName) {
        return new File(getDataAppPackageDirectory(packageName), "package.ini");
    }

    public static File getSignatureFile(String packageName) {
        return new File(getDataAppPackageDirectory(packageName), "signature.ini");
    }

    public static File getUserSystemDirectory() {
        return USER_DIRECTORY;
    }

    public static File getUserSystemDirectory(int userId) {
        return new File(USER_DIRECTORY, String.valueOf(userId));
    }

    public static File getWifiMacFile(int userId) {
        return new File(getUserSystemDirectory(userId), "wifiMacAddress");
    }

    public static File getDataDirectory() {
        return DATA_DIRECTORY;
    }

    public static File getSystemSecureDirectory() {
        return ensureCreated(new File(getDataAppDirectory(), "system"));
    }

    public static File getPackageInstallerStageDir() {
        return ensureCreated(new File(DATA_DIRECTORY, ".session_dir"));
    }
}