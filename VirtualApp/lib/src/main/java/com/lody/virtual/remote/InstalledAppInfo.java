package com.lody.virtual.remote;

import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lody
 */
public final class InstalledAppInfo implements Parcelable {

    public static final Creator<InstalledAppInfo> CREATOR = new Creator<InstalledAppInfo>() {
        @Override
        public InstalledAppInfo createFromParcel(Parcel in) {
            return new InstalledAppInfo(in);
        }

        @Override
        public InstalledAppInfo[] newArray(int size) {
            return new InstalledAppInfo[size];
        }
    };
    public String packageName;
    public String apkPath;
    public String libPath;
    public boolean dependSystem;
    public int appId;


    public InstalledAppInfo(String packageName, String apkPath, String libPath, boolean dependSystem, int appId) {
        this.packageName = packageName;
        this.apkPath = apkPath;
        this.libPath = libPath;
        this.dependSystem = dependSystem;
        this.appId = appId;
    }

    protected InstalledAppInfo(Parcel in) {
        packageName = in.readString();
        apkPath = in.readString();
        libPath = in.readString();
        dependSystem = in.readByte() != 0;
    }

    public List<Integer> getInstalledUsers() {
        List<Integer> installedUsers = new LinkedList<>();
        List<VUserInfo> users = VUserManager.get().getUsers();
        for (VUserInfo info : users) {
            if (VEnvironment.getDataUserPackageDirectory(info.id, packageName).exists()) {
                installedUsers.add(info.id);
            }
        }
        return installedUsers;
    }

    public boolean isInstalled(int userId) {
        return VEnvironment.getDataUserPackageDirectory(userId, packageName).exists();
    }

    public void installAsUser(int userId) {
        VEnvironment.getDataUserPackageDirectory(userId, packageName).mkdirs();
    }

    public File getOdexFile() {
        return VEnvironment.getOdexFile(packageName);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeString(apkPath);
        dest.writeString(libPath);
        dest.writeByte((byte) (dependSystem ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ApplicationInfo getApplicationInfo(int userId) {
        return VPackageManager.get().getApplicationInfo(packageName, 0, userId);
    }
}
