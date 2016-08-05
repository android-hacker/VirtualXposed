package com.lody.virtual.helper.proto;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.ActivityThreadCompat;

import android.app.Application;
import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 *
 */
public final class AppInfo implements Parcelable {

	public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
		@Override
		public AppInfo createFromParcel(Parcel source) {
			return new AppInfo(source);
		}

		@Override
		public AppInfo[] newArray(int size) {
			return new AppInfo[size];
		}
	};
	public String packageName;
	public String apkPath;
	public String dataDir;
	public String libDir;
	public String odexDir;
	public String cacheDir;
	public ApplicationInfo applicationInfo;
	public boolean dependSystem;

	public AppInfo() {
	}

	protected AppInfo(Parcel in) {
		this.packageName = in.readString();
		this.apkPath = in.readString();
		this.dataDir = in.readString();
		this.libDir = in.readString();
		this.odexDir = in.readString();
		this.cacheDir = in.readString();
		this.applicationInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
		this.dependSystem = in.readByte() != 0;
	}

	public ClassLoader getClassLoader() {

		return getLoadedApk().getClassLoader();
	}

	public synchronized LoadedApk getLoadedApk() {
		LoadedApk loadedApk = null;
		try {
			loadedApk = VirtualCore.mainThread().peekPackageInfo(packageName, true);
		} catch (Throwable e) {
			// Ignore
		}
		if (loadedApk != null) {
			return loadedApk;
		}
		loadedApk = ActivityThreadCompat.getPackageInfoNoCheck(applicationInfo);
		int repeat = 3;
		while (repeat-- > 0 && loadedApk == null) {
			loadedApk = ActivityThreadCompat.getPackageInfoNoCheck(applicationInfo);
		}
		return loadedApk;
	}

	public boolean isInstalled() {
		try {
			return VirtualCore.getCore().getUnHookPackageManager().getApplicationInfo(packageName, 0) != null;
		} catch (PackageManager.NameNotFoundException e) {
			// Ignore
		}
		return false;
	}

	public Application getApplication() {
		return AppSandBox.getApplication(packageName);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.packageName);
		dest.writeString(this.apkPath);
		dest.writeString(this.dataDir);
		dest.writeString(this.libDir);
		dest.writeString(this.odexDir);
		dest.writeString(this.cacheDir);
		dest.writeParcelable(this.applicationInfo, flags);
		dest.writeByte(this.dependSystem ? (byte) 1 : (byte) 0);
	}
}
