package com.lody.virtual.service.pm;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.DisplayMetrics;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.helper.compat.NativeLibraryHelperCompat;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.service.IAppManager;
import com.lody.virtual.service.am.StaticBroadcastSystem;
import com.lody.virtual.service.am.UidSystem;
import com.lody.virtual.service.am.VActivityManagerService;
import com.lody.virtual.service.interfaces.IAppObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lody
 */
public class VAppManagerService extends IAppManager.Stub {

	private static final String TAG = VAppManagerService.class.getSimpleName();

	private boolean isBooting;

	private final UidSystem mUidSystem = new UidSystem();

	private final StaticBroadcastSystem mBroadcastSystem =
			new StaticBroadcastSystem(
			VirtualCore.getCore().getContext(),
					VActivityManagerService.getService(),
			this
			);

	private static final AtomicReference<VAppManagerService> gService = new AtomicReference<>();

	private RemoteCallbackList<IAppObserver> mRemoteCallbackList = new RemoteCallbackList<IAppObserver>();

	public static VAppManagerService getService() {
		return gService.get();
	}

	public boolean isBooting() {
		return isBooting;
	}

	private static PackageParser.Package parsePackage(File apk, int flags) {
		PackageParser parser = new PackageParser(apk.getPath());
		DisplayMetrics metrics = new DisplayMetrics();
		metrics.setToDefaults();
		return parser.parsePackage(apk, apk.getPath(), metrics, flags);
	}

	public static void systemReady() {
		VAppManagerService instance = new VAppManagerService();
		gService.set(instance);
	}

	public void preloadAllApps() {
		isBooting = true;
		for (File appDir : VEnvironment.getDataAppDirectory().listFiles()) {
			String pkgName = appDir.getName();
			File storeFile = new File(appDir, "base.apk");
			int flags = 0;
			if (!storeFile.exists()) {
				ApplicationInfo appInfo = null;
				try {
					appInfo = VirtualCore.getCore().getUnHookPackageManager()
							.getApplicationInfo(pkgName, 0);
				} catch (PackageManager.NameNotFoundException e) {
					// Ignore
				}
				if (appInfo == null || appInfo.publicSourceDir == null) {
					FileUtils.deleteDir(appDir);
					continue;
				}
				storeFile = new File(appInfo.publicSourceDir);
				flags |= InstallStrategy.DEPEND_SYSTEM_IF_EXIST;
			}
			InstallResult res = install(storeFile.getPath(), flags, true);
			if (!res.isSuccess) {
				VLog.e(TAG, "Unable to install app %s: %s.", pkgName, res.error);
				FileUtils.deleteDir(appDir);
			}
		}
		isBooting = false;
	}

	@Override
	public InstallResult installApp(String apkPath, int flags) {
		return install(apkPath, flags, false);
	}

	private synchronized InstallResult install(String apkPath, int flags, boolean onlyScan) {
		if (apkPath == null) {
			return InstallResult.makeFailure("Not given the apk path.");
		}
		File apk = new File(apkPath);
		if (!apk.exists() || !apk.isFile()) {
			return InstallResult.makeFailure("APK File is not exist.");
		}
		PackageParser.Package pkg = parsePackage(apk, 0);
		if (pkg == null || pkg.packageName == null) {
			return InstallResult.makeFailure("Unable to parse the package.");
		}
		InstallResult res = new InstallResult();
		res.packageName = pkg.packageName;
		// PackageCache holds all packages, try to check if need update.
		PackageParser.Package existOne = PackageCache.get(pkg.packageName);
		if (existOne != null) {
			if ((flags & InstallStrategy.IGNORE_NEW_VERSION) != 0) {
				res.isUpdate = true;
				return res;
			}
			if (!canUpdate(existOne, pkg, flags)) {
				return InstallResult.makeFailure("Unable to update the Apk.");
			}
			res.isUpdate = true;
		}
		File appDir = VEnvironment.getDataAppPackageDirectory(pkg.packageName);


		File libDir = new File(appDir, "lib");
		if (!libDir.exists() && !libDir.mkdirs()) {
			return InstallResult.makeFailure("Unable to create lib dir.");
		}
		boolean dependSystem = (flags & InstallStrategy.DEPEND_SYSTEM_IF_EXIST) != 0
				&& VirtualCore.getCore().isOutsideInstalled(pkg.packageName);

		if (!onlyScan) {
			if (res.isUpdate) {
				FileUtils.deleteDir(libDir);
			}
			NativeLibraryHelperCompat.copyNativeBinaries(new File(apkPath), libDir);
			if (!dependSystem) {
				// /data/app/com.xxx.xxx-1/base.apk
				File storeFile = new File(appDir, "base.apk");
				File parentFolder = storeFile.getParentFile();
				if (!parentFolder.exists() && !parentFolder.mkdirs()) {
					VLog.w(TAG, "Warning: unable to create folder : " + storeFile.getPath());
				} else if (storeFile.exists() && !storeFile.delete()) {
					VLog.w(TAG, "Warning: unable to delete file : " + storeFile.getPath());
				}
				FileUtils.copyFile(apk, storeFile);
				apk = storeFile;
			}
		}
		if (existOne != null) {
			PackageCache.remove(pkg.packageName);
		}
		AppSetting appSetting = new AppSetting();
		appSetting.dependSystem = dependSystem;
		appSetting.apkPath = apk.getPath();
		appSetting.libPath = libDir.getPath();
		File odexFolder = new File(appDir, VirtualRuntime.isArt() ? "oat" : "odex");
		if (!odexFolder.exists() && !odexFolder.mkdirs()) {
			VLog.w(TAG, "Warning: unable to create folder : " + odexFolder.getPath());
		}
		appSetting.odexDir = odexFolder.getPath();
		appSetting.packageName = pkg.packageName;
		appSetting.appId = VUserHandle.getAppId(mUidSystem.getOrCreateUid(pkg.mSharedUserId));

		PackageCache.put(pkg, appSetting);
		mBroadcastSystem.startApp(pkg);


		notifyAppInstalled(pkg.packageName);
		res.isSuccess = true;
		return res;
	}

	private boolean canUpdate(PackageParser.Package existOne, PackageParser.Package newOne, int flags) {
		if ((flags & InstallStrategy.COMPARE_VERSION) != 0) {
			if (existOne.mVersionCode < newOne.mVersionCode) {
				return true;
			}
		}
		if ((flags & InstallStrategy.TERMINATE_IF_EXIST) != 0) {
			return false;
		}
		if ((flags & InstallStrategy.UPDATE_IF_EXIST) != 0) {
			return true;
		}
		return false;
	}

	public boolean uninstallApp(String pkg) {
		synchronized (PackageCache.sPackageCaches) {
			if (isAppInstalled(pkg)) {
				try {
					VActivityManagerService.getService().killAppByPkg(pkg, VUserHandle.USER_ALL);
					FileUtils.deleteDir(VEnvironment.getDataAppPackageDirectory(pkg));
					PackageCache.remove(pkg);
					mBroadcastSystem.stopApp(pkg);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					notifyAppUninstalled(pkg);
				}
				return true;
			}
		}
		return false;
	}

	public List<AppSetting> getAllApps() {
		List<AppSetting> settings = new ArrayList<>(getAppCount());
		for (PackageParser.Package p : PackageCache.sPackageCaches.values()) {
			settings.add((AppSetting) p.mExtras);
		}
		return settings;
	}

	public int getAppCount() {
		return PackageCache.sPackageCaches.size();
	}

	public boolean isAppInstalled(String pkg) {
		return pkg != null && PackageCache.sPackageCaches.get(pkg) != null;
	}

	private void notifyAppInstalled(String pkgName) {
		int N = mRemoteCallbackList.beginBroadcast();
		while (N-- > 0) {
			try {
				mRemoteCallbackList.getBroadcastItem(N).onNewApp(pkgName);
			} catch (RemoteException e) {
				// Ignore
			}
		}
		mRemoteCallbackList.finishBroadcast();
		Intent virtualIntent = new Intent(Constants.ACTION_PACKAGE_ADDED);
		Uri uri = Uri.fromParts("package", pkgName, null);
		virtualIntent.setData(uri);
		VirtualCore.getCore().getContext().sendBroadcast(virtualIntent);
	}

	private void notifyAppUninstalled(String pkgName) {
		int N = mRemoteCallbackList.beginBroadcast();
		while (N-- > 0) {
			try {
				mRemoteCallbackList.getBroadcastItem(N).onRemoveApp(pkgName);
			} catch (RemoteException e) {
				// Ignore
			}
		}
		mRemoteCallbackList.finishBroadcast();
		Intent virtualIntent = new Intent(Constants.ACTION_PACKAGE_REMOVED);
		Uri uri = Uri.fromParts("package", pkgName, null);
		virtualIntent.setData(uri);
		VirtualCore.getCore().getContext().sendBroadcast(virtualIntent);
	}

	@Override
	public void registerObserver(IAppObserver observer) {
		try {
			mRemoteCallbackList.register(observer);
		} catch (Throwable e) {
			// Ignore
		}
	}

	@Override
	public void unregisterObserver(IAppObserver observer) {
		try {
			mRemoteCallbackList.unregister(observer);
		} catch (Throwable e) {
			// Ignore
		}
	}

	public AppSetting findAppInfo(String pkg) {
		synchronized (PackageCache.class) {
			if (pkg != null) {
				return (AppSetting) PackageCache.sPackageCaches.get(pkg).mExtras;
			}
			return null;
		}
	}

	public int getAppId(String pkg) {
		AppSetting setting = findAppInfo(pkg);
		return setting != null ? setting.appId : -1;
	}
}
