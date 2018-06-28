package com.lody.virtual.os;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.EncodeUtils;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;

import java.io.File;
import java.util.Locale;

import mirror.dalvik.system.VMRuntime;

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
        return new File(getDataAppPackageDirectory(packgeName),
                EncodeUtils.decode("YmFzZS5hcGs=")); // base.apk
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
        if (isAndroidO()) {
            // in Android O, the oatfile is relate with classloader, we must ensure the correct location to avoid repeated load dex.
            String instructionSet = VMRuntime.getCurrentInstructionSet.call();
            File oatDir = ensureCreated(new File(getDataAppPackageDirectory(packageName), "oat" + File.separator + instructionSet));
            return new File(oatDir, EncodeUtils.decode("YmFzZS5vZGV4")); // base.odex
        } else {
            // return new File(DALVIK_CACHE_DIRECTORY, "data@app@" + packageName + "-1@base.apk@classes.dex");
            return new File(DALVIK_CACHE_DIRECTORY, EncodeUtils.decode("ZGF0YUBhcHBA") +
                    packageName +
                    EncodeUtils.decode("LTFAYmFzZS5hcGtAY2xhc3Nlcy5kZXg="));
        }
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

    public static File getVirtualStorageBaseDir() {
        File externalFilesRoot = Environment.getExternalStorageDirectory();
        if (externalFilesRoot != null) {
            File vBaseDir = new File(externalFilesRoot, "VirtualXposed");
            File vSdcard = new File(vBaseDir, "vsdcard");
            return ensureCreated(vSdcard);
        }
        return null;
    }

    public static File getVirtualStorageDir(String packageName, int userId) {
        File virtualStorageBaseDir = getVirtualStorageBaseDir();
        // Apps may share sdcard files, we can not separate them by package.
        if (virtualStorageBaseDir == null) {
            return null;
        }
        File userBase = new File(virtualStorageBaseDir, String.valueOf(userId));
        return ensureCreated(userBase);
    }

    // /sdcard/Android/data/<host_package>/virtual/<user>
    public static File getVirtualPrivateStorageDir(int userId) {
        String base = String.format(Locale.ENGLISH, "%s/Android/data/%s/%s/%d", Environment.getExternalStorageDirectory(),
                VirtualCore.get().getHostPkg(), "virtual", userId);
        File file = new File(base);
        return ensureCreated(file);
    }

    public static File getVirtualPrivateStorageDir(int userId, String packageName) {
        File file = new File(getVirtualPrivateStorageDir(userId), packageName);
        return ensureCreated(file);
    }

    public static File getWifiMacFile(int userId) {
        // return new File(getUserSystemDirectory(userId), "wifiMacAddress");
        return new File(getUserSystemDirectory(userId), EncodeUtils.decode("d2lmaU1hY0FkZHJlc3M="));
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

    public static boolean isAndroidO() {
        return Build.VERSION.SDK_INT > 25;
    }
}