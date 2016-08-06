package com.lody.virtual.service.pm;

import android.content.Intent;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.DisplayMetrics;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.compat.NativeLibraryHelperCompat;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.helper.utils.FileIO;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.AppFileSystem;
import com.lody.virtual.service.IAppManager;
import com.lody.virtual.service.interfaces.IAppObserver;
import com.lody.virtual.service.process.VProcessService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lody
 */
public class VAppService extends IAppManager.Stub {

	private static final String TAG = VAppService.class.getSimpleName();

	private static final AtomicReference<VAppService> gService = new AtomicReference<>();

	private RemoteCallbackList<IAppObserver> remoteCallbackList = new RemoteCallbackList<IAppObserver>();

	public static VAppService getService() {
		return gService.get();
	}

	private static void ensureFoldersCreated(File... folders) {
		for (File folder : folders) {
			if (!folder.exists() && !folder.mkdirs()) {
				VLog.w(TAG, "Warning: unable to create folder : " + folder.getPath());
			}
		}
	}

	private static PackageParser.Package parsePackage(File apk, int flags) {
		PackageParser parser = new PackageParser(apk.getPath());
		DisplayMetrics metrics = new DisplayMetrics();
		metrics.setToDefaults();
		return parser.parsePackage(apk, apk.getPath(), metrics, flags);
	}

	public static void systemReady() {
		VAppService instance = new VAppService();
		gService.set(instance);
		instance.preloadAllApps();
	}

	public void preloadAllApps() {
		List<File> appList = AppFileSystem.getDefault().getAllApps();
		for (File app : appList) {
			InstallResult res = install(app.getPath(),
					InstallStrategy.TERMINATE_IF_EXIST | InstallStrategy.DEPEND_SYSTEM_IF_EXIST, true);
			if (!res.isSuccess) {
				FileIO.deleteDir(app);
			}
		}
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
		if (pkg == null) {
			return InstallResult.makeFailure("Unable to parse the package.");
		}
		InstallResult res = new InstallResult();
		res.packageName = pkg.packageName;
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

		File libDir = AppFileSystem.getDefault().getAppLibFolder(pkg.packageName);
		boolean dependSystem = (flags & InstallStrategy.DEPEND_SYSTEM_IF_EXIST) != 0
				&& VirtualCore.getCore().isOutsideInstalled(pkg.packageName);

		if (!onlyScan) {
			if (res.isUpdate) {
				FileIO.deleteDir(libDir);
			}
			if (!libDir.exists() && !libDir.mkdirs()) {
				return InstallResult.makeFailure("Unable to create lib dir.");
			}
			int libRes = NativeLibraryHelperCompat.copyNativeBinaries(new File(apkPath), libDir);
			if (libRes < 0) {
				return InstallResult.makeFailure("This APK's native lib is not support your device.");
			}

			if (!dependSystem) {
				File storeFile = AppFileSystem.getDefault().getAppApkFile(pkg.packageName);
				File parentFolder = storeFile.getParentFile();
				if (!parentFolder.exists() && !parentFolder.mkdirs()) {
					VLog.w(TAG, "Warning: unable to create folder : " + storeFile.getPath());
				} else if (storeFile.exists() && !storeFile.delete()) {
					VLog.w(TAG, "Warning: unable to delete file : " + storeFile.getPath());
				}
				FileIO.copyFile(apk, storeFile);
				apk = storeFile;
			}
		}
		if (existOne != null) {
			PackageCache.remove(pkg.packageName);
		}
		AppFileSystem fileSystem = AppFileSystem.getDefault();
		AppInfo appInfo = new AppInfo();
		appInfo.dependSystem = dependSystem;
		appInfo.apkPath = apk.getPath();
		appInfo.packageName = pkg.packageName;
		appInfo.setApplicationInfo(pkg.applicationInfo);

		File dataFolder = fileSystem.getAppPackageFolder(pkg.packageName);
		File libFolder = fileSystem.getAppLibFolder(pkg.packageName);
		File dvmCacheFolder = fileSystem.getAppDVMCacheFolder(pkg.packageName);
		File cacheFolder = fileSystem.getAppCacheFolder(pkg.packageName);

		ensureFoldersCreated(dataFolder, libFolder, dvmCacheFolder, cacheFolder);

		appInfo.dataDir = dataFolder.getPath();
		appInfo.libDir = libFolder.getPath();
		appInfo.cacheDir = cacheFolder.getPath();
		appInfo.odexDir = dvmCacheFolder.getPath();
		PackageCache.put(pkg, appInfo);
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
				VProcessService.getService().killAppByPkg(pkg);
				FileIO.deleteDir(AppFileSystem.getDefault().getAppPackageFolder(pkg));
				PackageCache.remove(pkg);
				notifyAppUninstalled(pkg);
				return true;
			}
		}
		return false;
	}

	public List<AppInfo> getAllApps() {
		return new ArrayList<>(PackageCache.sAppInfos.values());
	}

	public int getAppCount() {
		return PackageCache.sAppInfos.size();
	}

	public boolean isAppInstalled(String pkg) {
		return pkg != null && PackageCache.sPackageCaches.get(pkg) != null;
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
		Intent virtualIntent = new Intent(Constants.VIRTUAL_ACTION_PACKAGE_ADDED);
		Uri uri = Uri.fromParts("package", pkgName, null);
		virtualIntent.setData(uri);
		VirtualCore.getCore().getContext().sendBroadcast(virtualIntent);
	}


	private void notifyAppUninstalled(String pkgName) {
		int N = remoteCallbackList.beginBroadcast();
		while (N-- > 0) {
			try {
				remoteCallbackList.getBroadcastItem(N).onRemoveApp(pkgName);
			} catch (RemoteException e) {
				// Ignore
			}
		}
		remoteCallbackList.finishBroadcast();
		Intent virtualIntent = new Intent(Constants.VIRTUAL_ACTION_PACKAGE_REMOVED);
		Uri uri = Uri.fromParts("package", pkgName, null);
		virtualIntent.setData(uri);
		VirtualCore.getCore().getContext().sendBroadcast(virtualIntent);
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

	public AppInfo findAppInfo(String pkg) {
		synchronized (PackageCache.class) {
			if (pkg != null) {
				return PackageCache.sAppInfos.get(pkg);
			}
			return null;
		}
	}
}
