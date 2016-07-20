package com.lody.virtual.helper.proto;

import android.app.Application;
import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.ActivityThreadCompat;

/**
 * @author Lody
 *
 */
public final class AppInfo implements Parcelable {

	public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
		public AppInfo createFromParcel(Parcel source) {
			return new AppInfo(source);
		}

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

	public AppInfo() {
	}

	protected AppInfo(Parcel in) {
		packageName = in.readString();
		apkPath = in.readString();
		dataDir = in.readString();
		libDir = in.readString();
		odexDir = in.readString();
		cacheDir = in.readString();
		applicationInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
	}

	public ClassLoader getClassLoader() {

		return getLoadedApk().getClassLoader();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(packageName);
		dest.writeString(apkPath);
		dest.writeString(dataDir);
		dest.writeString(libDir);
		dest.writeString(odexDir);
		dest.writeString(cacheDir);
		dest.writeParcelable(applicationInfo, 0);
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
}
