package com.lody.virtual.helper.proto;

import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.client.core.VirtualCore;

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
	public boolean dependSystem;
	private ApplicationInfo applicationInfo;

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
		dependSystem = in.readByte() != 0;
	}

	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	public void setApplicationInfo(ApplicationInfo applicationInfo) {
		this.applicationInfo = applicationInfo;
	}

	public ClassLoader getClassLoader() {
		return getLoadedApk().getClassLoader();
	}

	public LoadedApk getLoadedApk() {
		LoadedApk loadedApk = null;
		try {
			loadedApk = VirtualCore.mainThread().peekPackageInfo(packageName, true);
		} catch (Throwable e) {
			// Ignore
		}
		if (loadedApk == null) {
			loadedApk = VirtualCore.mainThread().getPackageInfoNoCheck(getApplicationInfo(),
					CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);
		}
		return loadedApk;
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
		dest.writeParcelable(applicationInfo, flags);
		dest.writeByte(dependSystem ? (byte) 1 : (byte) 0);
	}
}
