package com.lody.virtual.helper.proto;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.client.ipc.VPackageManager;

import java.io.File;

/**
 * @author Lody
 *
 */
public final class AppSetting implements Parcelable {

	public String packageName;
	public String apkPath;
	public String libPath;
	public String odexDir;
	public boolean dependSystem;
	public int appId;
	public transient PackageParser parser;

	public AppSetting() {

	}


	protected AppSetting(Parcel in) {
		packageName = in.readString();
		apkPath = in.readString();
		libPath = in.readString();
		odexDir = in.readString();
		dependSystem = in.readByte() != 0;
	}

	public File getOdexFile() {
		return new File(odexDir, "base.dex");
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(packageName);
		dest.writeString(apkPath);
		dest.writeString(libPath);
		dest.writeString(odexDir);
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
