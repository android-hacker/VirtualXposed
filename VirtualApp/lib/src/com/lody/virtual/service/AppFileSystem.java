package com.lody.virtual.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 *
 *
 *         插件的文件系统
 */
public class AppFileSystem {

	private static final String TAG = AppFileSystem.class.getSimpleName();

	// 目录结构
	// app_VApps
	// ----com.XXX.XXX
	// --------apk
	// ------------base.apk
	// --------lib
	// --------dalvik-cache
	// --------cache

	private static final AppFileSystem gDefault = new AppFileSystem();

	private static final String STORE_APK_NAME = "apk/base.apk";
	private static final String LIB_DIR_NAME = "lib";
	private static final String DVM_CACHE_DIR_NAME = "dalvik-cache";
	private static final String CACHE_DIR_NAME = "cache";
	private static final String FILES_DIR_NAME = "files";
	private static final String PLUGIN_ROOT_DIR_NAME = "VApps";

	private Context context;

	private AppFileSystem() {
		context = VirtualCore.getCore().getContext();
		File rootDir = getAppRootDir();
		rootDir.mkdirs();
	}

	public static AppFileSystem getDefault() {
		return gDefault;
	}

	public List<File> getAllApps() {
		List<File> apkFiles = new ArrayList<File>(5);
		File baseDir = getAppRootDir();
		File[] packageDirs = baseDir.listFiles();
		if (packageDirs != null) {
			for (File packageDir : packageDirs) {
				File apk = new File(packageDir, STORE_APK_NAME);
				if (apk.exists()) {
					apkFiles.add(apk);
				} else {
					String pkgName = packageDir.getName();
					try {
						ApplicationInfo applicationInfo = VirtualCore.getCore().getUnHookPackageManager()
								.getApplicationInfo(pkgName, PackageManager.GET_SHARED_LIBRARY_FILES);
						apkFiles.add(new File(applicationInfo.sourceDir));
					} catch (PackageManager.NameNotFoundException e) {
						deleteApp(pkgName);
					}
				}
			}
		}
		return apkFiles;
	}

	public File getAppApkFile(String packageName) {
		return new File(getAppPackageFolder(packageName), STORE_APK_NAME);
	}

	public File getAppPackageFolder(String packageName) {
		File file = new File(getAppRootDir(), packageName);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	public File getAppLibFolder(String pkgName) {
		return new File(getAppPackageFolder(pkgName), LIB_DIR_NAME);
	}

	public File getAppDVMCacheFolder(String pkgName) {
		return new File(getAppPackageFolder(pkgName), DVM_CACHE_DIR_NAME);
	}

	public File getAppCacheFolder(String pkgName) {
		return new File(getAppPackageFolder(pkgName), CACHE_DIR_NAME);
	}

	/**
	 * 删除一个插件
	 * 
	 * @param packageName
	 *            插件包名
	 * @return 删除是否成功
	 */
	public boolean deleteApp(String packageName) {
		File baseDir = getAppRootDir();
		File packageDir = new File(baseDir, packageName);
		return packageDir.exists() && FileIO.deleteDir(packageDir);
	}

	public File getAppRootDir() {
		return context.getDir(PLUGIN_ROOT_DIR_NAME, Context.MODE_PRIVATE);
	}

	public File getAppAssetFolder(String pkgName) {
		return new File(getAppPackageFolder(pkgName), FILES_DIR_NAME);
	}
}
