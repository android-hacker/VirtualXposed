package com.lody.virtual.helper.proto;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
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
public final class AppSetting implements Parcelable {

    public String packageName;
    public String apkPath;
    public String libPath;
    public boolean dependSystem;
    public int appId;
    public transient PackageParser parser;

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

    public AppSetting() {
    }


    protected AppSetting(Parcel in) {
        packageName = in.readString();
        apkPath = in.readString();
        libPath = in.readString();
        dependSystem = in.readByte() != 0;
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

    public static final Creator<AppSetting> CREATOR = new Creator<AppSetting>() {
        @Override
        public AppSetting createFromParcel(Parcel in) {
            return new AppSetting(in);
        }

        @Override
        public AppSetting[] newArray(int size) {
            return new AppSetting[size];
        }
    };

    public ApplicationInfo getApplicationInfo(int userId) {
        return VPackageManager.get().getApplicationInfo(packageName, 0, userId);
    }
}
