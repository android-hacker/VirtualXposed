package com.lody.virtual.service;

import android.content.pm.PackageInfo;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.helper.bundle.APKBundle;
import com.lody.virtual.helper.compat.NativeLibraryHelperCompat;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.helper.proto.Problem;
import com.lody.virtual.helper.utils.FileIO;
import com.lody.virtual.helper.utils.XLog;
import com.lody.virtual.service.interfaces.IAppObserver;
import com.lody.virtual.service.process.VProcessService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 *
 */
public class VAppService extends IAppManager.Stub {

	private static final String TAG = VAppService.class.getSimpleName();

	private static final VAppService gService = new VAppService();
	private final char[] mLock = new char[0];
	private Map<String, AppInfo> mAppInfoCaches = new ConcurrentHashMap<String, AppInfo>(10);
	private Map<String, APKBundle> mApkBundleCaches = new ConcurrentHashMap<String, APKBundle>(10);
	private RemoteCallbackList<IAppObserver> remoteCallbackList = new RemoteCallbackList<IAppObserver>();

	public static VAppService getService() {
		return gService;
	}

	public void onCreate() {
		 preloadAllApps();
	}

	public void preloadAllApps() {
		XLog.d(TAG, "=============================================");
		XLog.d(TAG, "=======>>>>> Start Scan App >>>===========");
		List<File> pluginList = AppFileSystem.getDefault().getAllApps();
		if (pluginList.isEmpty()) {
			XLog.d(TAG, "=============>>> Empty >>>===============");
		} else {
			for (File plugin : pluginList) {
				XLog.d(TAG, "=============>>> " + plugin.getPath());
				InstallResult result = installApp(plugin.getPath(), InstallStrategy.IGNORE_NEW_VERSION);
				if (!result.isSuccess) {
					FileIO.deleteDir(plugin);
				}
				XLog.d(TAG, "=============>>> Install Result : %s.", result.isSuccess ? "Success" : "Failed");
			}
		}
		XLog.d(TAG, "=============================================");
	}

	public InstallResult installApp(String apkPath, int flags) {
		InstallResult result = new InstallResult();
		try {
			File apkFile = new File(apkPath);
			APKBundle bundle = new APKBundle(apkFile);

			AppInfo appInfo = bundle.getAppInfo();
			String pkgName = bundle.getPackageName();
			result.installedPackageName = pkgName;

			synchronized (mLock) {
				if (mAppInfoCaches.containsKey(pkgName)) {
					switch (flags) {
						case InstallStrategy.UPDATE_IF_EXIST : {
							updateAppLocked(bundle, appInfo);
							result.isUpdate = true;
							break;
						}
						case InstallStrategy.COMPARE_VERSION : {
							PackageInfo nowPkgInfo = mApkBundleCaches.get(pkgName).getPackageInfo(0);
							PackageInfo newPkgInfo = bundle.getPackageInfo(0);
							if (nowPkgInfo.versionCode < newPkgInfo.versionCode) {
								result.isUpdate = true;
								updateAppLocked(bundle, appInfo);
								break;
							} else {
								throw new IllegalStateException("Current APK Version is " + nowPkgInfo.versionCode
										+ ", but New APK Version is " + newPkgInfo.versionCode);
							}
						}
						case InstallStrategy.TERMINATE_IF_EXIST : {
							throw new IllegalStateException("This apk have installed, should not be install again.");
						}
						case InstallStrategy.IGNORE_NEW_VERSION : {
							break;
						}
					}
				} else {
					addAppLocked(bundle, appInfo);
				}
				File libFolder = new File(appInfo.libDir);
				libFolder.mkdirs();
				if (NativeLibraryHelperCompat.copyNativeBinaries(apkFile, libFolder) < 0) {
					throw new RuntimeException("Not support abi.");
				}
				notifyAppInstalled(pkgName);
				result.isSuccess = true;
			}
		} catch (Throwable installError) {
			result.isSuccess = false;
			result.problem = new Problem(installError);
			if (!(installError instanceof IllegalStateException) && result.installedPackageName != null) {
				// Clean up environment
				uninstallApp(result.installedPackageName);
			}
		}
		return result;
	}

	private void addAppLocked(APKBundle parser, AppInfo appInfo) {
		String pkg = appInfo.packageName;
		mAppInfoCaches.put(pkg, appInfo);
		mApkBundleCaches.put(pkg, parser);
	}

	private void updateAppLocked(APKBundle parser, AppInfo appInfo) {
		String pkg = appInfo.packageName;
		removeAppLocked(pkg);
		addAppLocked(parser, appInfo);
	}

	private void removeAppLocked(String pkg) {
		mAppInfoCaches.remove(pkg);
		mApkBundleCaches.remove(pkg);
	}

	public boolean uninstallApp(String pkg) {
		if (isAppInstalled(pkg)) {
			synchronized (mLock) {
				VProcessService.getService().killAppByPkg(pkg);
				removeAppLocked(pkg);
				AppFileSystem.getDefault().deleteApp(pkg);
				return true;
			}
		}
		return false;
	}

	public List<AppInfo> getAllApps() {
		return new ArrayList<AppInfo>(mAppInfoCaches.values());
	}

	public int getAppCount() {
		return mAppInfoCaches.size();
	}

	public boolean isAppInstalled(String pkg) {
		return pkg != null && mAppInfoCaches.containsKey(pkg);
	}

	private void notifyAppInstalled(String pkgName) {
		int N = remoteCallbackList.beginBroadcast();
		while (N-- > 0) {
			try {
				remoteCallbackList.getBroadcastItem(N).onNewApp(pkgName);
			} catch (RemoteException e) {
				// Ignore
			}
		}
		remoteCallbackList.finishBroadcast();
	}

	public void registerObserver(IAppObserver observer) {
		try {
			remoteCallbackList.register(observer);
		} catch (Throwable e) {
			// Ignore
		}
	}

	public void unregisterObserver(IAppObserver observer) {
		try {
			remoteCallbackList.unregister(observer);
		} catch (Throwable e) {
			// Ignore
		}
	}

	public APKBundle getAPKBundle(String pkg) {
		return pkg != null ? mApkBundleCaches.get(pkg) : null;
	}

	public Map<String, APKBundle> getAllAPKBundles() {
		return this.mApkBundleCaches;
	}

	public AppInfo findAppInfo(String pkg) {
		return pkg != null ? mAppInfoCaches.get(pkg) : null;
	}
}
