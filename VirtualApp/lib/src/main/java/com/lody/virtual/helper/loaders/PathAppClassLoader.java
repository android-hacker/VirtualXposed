package com.lody.virtual.helper.loaders;

import java.io.File;

import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.utils.VLog;

import android.content.pm.ApplicationInfo;

import dalvik.system.PathClassLoader;

/**
 * @author Lody
 *
 */
public class PathAppClassLoader extends PathClassLoader {

	private static final String TAG = PathAppClassLoader.class.getSimpleName();
	private AppInfo appInfo;

	public PathAppClassLoader(AppInfo appInfo) {
		super(".", appInfo.libDir, ClassLoader.getSystemClassLoader().getParent());
		this.appInfo = appInfo;
		ClassLoaderInjectHelper.InjectResult result = ClassLoaderInjectHelper.inject(this, new File(appInfo.apkPath),
				new File(appInfo.odexDir), new File(appInfo.libDir));
		if (result.mErrMsg != null) {
			VLog.d(TAG, "Inject PathClassLoader failed : %s.", result.mErrMsg);
		}
	}

	public PathAppClassLoader(AppInfo appInfo, ApplicationInfo outsideAppInfo) {
		super(outsideAppInfo.sourceDir, appInfo.libDir, ClassLoader.getSystemClassLoader().getParent());
		this.appInfo = appInfo;
	}
	public AppInfo getAppInfo() {
		return appInfo;
	}
}
