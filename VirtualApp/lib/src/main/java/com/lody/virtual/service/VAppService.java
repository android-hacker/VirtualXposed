package com.lody.virtual.service;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.bundle.APKBundle;
import com.lody.virtual.helper.compat.NativeLibraryHelperCompat;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.helper.proto.Problem;
import com.lody.virtual.helper.utils.FileIO;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.interfaces.IAppObserver;
import com.lody.virtual.service.process.VProcessService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

	private static final String[] PRE_INSTALL_PKG = {"com.google.android.gsf", "com.google.android.gsf.login",
			"com.google.android.gms", "com.android.vending"};

	public static VAppService getService() {
		return gService;
	}

	public void systemReady() {
		preloadAllApps();
	}

	public void preloadAllApps() {
		VLog.d(TAG, "=============================================");
		VLog.d(TAG, "==========$$$ Start Scan App $$$===========");
		List<File> appList = AppFileSystem.getDefault().getAllApps();
		if (appList.isEmpty()) {
			VLog.d(TAG, "===============$$$ Empty $$$===================");
		} else {
			for (File app : appList) {
				VLog.d(TAG, "=============>>> " + app.getPath());
				if (!scan(app.getPath())) {
					FileIO.deleteDir(app);
				}
			}
		}
		for (String pkg : PRE_INSTALL_PKG) {
			if (isAppInstalled(pkg)) {
				continue;
			}
			try {
				ApplicationInfo applicationInfo = VirtualCore.getCore().getUnHookPackageManager()
						.getApplicationInfo(pkg, 0);
				String apkPath = applicationInfo.publicSourceDir;
				installApp(apkPath, InstallStrategy.COMPARE_VERSION);
			} catch (Throwable e) {
				// Ignore
			}
		}
		VLog.d(TAG, "=============================================");
		VLog.d(TAG, "=============================================");
	}

	private boolean scan(String apkPath) {
		try {
			File apkFile = new File(apkPath);
			APKBundle bundle = new APKBundle(apkFile);
			AppInfo appInfo = bundle.getAppInfo();
			addAppLocked(bundle, appInfo, true, true);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public InstallResult installApp(String apkPath, int flags) {
		return install(apkPath, flags, false);
	}

	private InstallResult install(String apkPath, int flags, boolean onlyScan) {
		InstallResult result = new InstallResult();
		try {
			File apkFile = new File(apkPath);
			APKBundle bundle = new APKBundle(apkFile);
			AppInfo appInfo = bundle.getAppInfo();
			String pkgName = bundle.getPackageName();
			result.packageName = pkgName;
			boolean dependSystem = false;

			synchronized (mLock) {
				if ((flags & InstallStrategy.DEPEND_SYSTEM_IF_EXIST) != 0) {
					dependSystem = true;
				}
				appInfo.dependSystem = dependSystem;
				if (mAppInfoCaches.containsKey(pkgName)) {
					if ((flags & InstallStrategy.UPDATE_IF_EXIST) != 0) {
						updateAppLocked(bundle, appInfo, dependSystem);
						result.isUpdate = true;
					}
					if ((flags & InstallStrategy.COMPARE_VERSION) != 0) {
						PackageInfo nowPkgInfo = mApkBundleCaches.get(pkgName).getPackageInfo(0);
						PackageInfo newPkgInfo = bundle.getPackageInfo(0);
						if (nowPkgInfo.versionCode < newPkgInfo.versionCode) {
							result.isUpdate = true;
							updateAppLocked(bundle, appInfo, dependSystem);
						} else {
							throw new IllegalStateException("Current APK Version is " + nowPkgInfo.versionCode
									+ ", but New APK Version is " + newPkgInfo.versionCode);
						}
					}
					if ((flags & InstallStrategy.TERMINATE_IF_EXIST) != 0) {
						throw new IllegalStateException("This apk have installed, should not be scan again.");
					}

					if ((flags & InstallStrategy.IGNORE_NEW_VERSION) != 0) {
						// Nothing to do
					}
				} else {
					addAppLocked(bundle, appInfo, false, dependSystem);
				}
				File libFolder = new File(appInfo.libDir);
				libFolder.mkdirs();
				if (!onlyScan && NativeLibraryHelperCompat.copyNativeBinaries(apkFile, libFolder) < 0) {
					throw new RuntimeException("Not support abi.");
				}
				notifyAppInstalled(pkgName);
				result.isSuccess = true;
			}
		} catch (Throwable installError) {
			result.isSuccess = false;
			result.problem = new Problem(installError);
			if (!(installError instanceof IllegalStateException) && result.packageName != null) {
				// Clean up environment
				uninstallApp(result.packageName);
			}
		}
		return result;
	}

	private void addAppLocked(APKBundle bundle, AppInfo appInfo, boolean onlyScan, boolean dependSystem) throws IOException {
		String pkg = appInfo.packageName;
		mAppInfoCaches.put(pkg, appInfo);
		mApkBundleCaches.put(pkg, bundle);
		if (!onlyScan && !dependSystem) {
			// If the app has installed,
			// we needn't -copy-apk- and -dex-opt-
			bundle.copyToPrivate();
		}
	}

	private void updateAppLocked(APKBundle bundle, AppInfo appInfo, boolean dependSystem) throws IOException {
		String pkg = appInfo.packageName;
		removeAppLocked(pkg);
		addAppLocked(bundle, appInfo, false, dependSystem);
		VProcessService.getService().killAppByPkg(appInfo.packageName);
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

	@Override
	public void registerObserver(IAppObserver observer) {
		try {
			remoteCallbackList.register(observer);
		} catch (Throwable e) {
			// Ignore
		}
	}

	@Override
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
		return Collections.unmodifiableMap(mApkBundleCaches);
	}

	public AppInfo findAppInfo(String pkg) {
		return pkg != null ? mAppInfoCaches.get(pkg) : null;
	}
}
