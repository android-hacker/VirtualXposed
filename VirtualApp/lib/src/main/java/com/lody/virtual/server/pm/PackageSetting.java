package com.lody.virtual.server.pm;

import android.content.pm.PackageParser;

import com.lody.virtual.remote.InstalledAppInfo;

/**
 * @author Lody
 */

public class PackageSetting {

    public String packageName;
    public String apkPath;
    public String libPath;
    public boolean dependSystem;
    public int appId;
    public PackageParser parser;

    public InstalledAppInfo getAppInfo() {
        return new InstalledAppInfo(packageName, apkPath, libPath, dependSystem, appId);
    }

}
