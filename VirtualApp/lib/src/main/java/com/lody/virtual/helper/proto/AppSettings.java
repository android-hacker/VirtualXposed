package com.lody.virtual.helper.proto;

import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.CompatibilityInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.client.core.VirtualCore;

import java.io.File;

/**
 * @author Lody
 *
 */
public final class AppSettings implements Parcelable {

	public String packageName;
	public String apkPath;
	public String dataDir;
	public String libDir;
	public String odexDir;
	public String cacheDir;
	public boolean dependSystem;
	public int uid;
	private ApplicationInfo applicationInfo;

	public AppSettings() {
	}

	public synchronized ApplicationInfo getApplicationInfo() {
		if (applicationInfo == null) {
			try {
				applicationInfo = VirtualCore.getPM().getApplicationInfo(packageName, 0);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return applicationInfo;
	}


	public File getOdexFile() {
		return new File(odexDir, "classes.dex");
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
		dest.writeString(this.packageName);
		dest.writeString(this.apkPath);
		dest.writeString(this.dataDir);
		dest.writeString(this.libDir);
		dest.writeString(this.odexDir);
		dest.writeString(this.cacheDir);
		dest.writeByte(this.dependSystem ? (byte) 1 : (byte) 0);
		dest.writeInt(this.uid);
	}

	protected AppSettings(Parcel in) {
		this.packageName = in.readString();
		this.apkPath = in.readString();
		this.dataDir = in.readString();
		this.libDir = in.readString();
		this.odexDir = in.readString();
		this.cacheDir = in.readString();
		this.dependSystem = in.readByte() != 0;
		this.uid = in.readInt();
	}

	public static final Creator<AppSettings> CREATOR = new Creator<AppSettings>() {
		@Override
		public AppSettings createFromParcel(Parcel source) {
			return new AppSettings(source);
		}

		@Override
		public AppSettings[] newArray(int size) {
			return new AppSettings[size];
		}
	};
}
