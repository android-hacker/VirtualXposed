package com.lody.virtual.client.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.LruCache;

import com.lc.interceptor.client.core.InterceptorManager;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.fixer.ContextFixer;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.service.IAppManager;

import java.util.List;

import dalvik.system.DexFile;
import mirror.android.app.ActivityThread;

/**
 * @author Lody
 * @version 2.2
 */
public final class VirtualCore {

	@SuppressLint("StaticFieldLeak")
	private static VirtualCore gCore = new VirtualCore();
	/**
	 * Client Package Manager
	 */
	private PackageManager unHookPackageManager;
	/**
	 * Host package name
	 */
	private String hostPkgName;
	/**
	 * ActivityThread instance
	 */
	private Object mainThread;
	private Context context;

	/**
	 * Main ProcessName
	 */
	private String mainProcessName;
	/**
	 * Real Process Name
	 */
	private String processName;
	private ProcessType processType;
	private IAppManager mService;
	private boolean isStartUp;
	private PackageInfo hostPkgInfo;
	private final LruCache<ComponentName, ActivityInfo> activityInfoCache = new LruCache<>(6);
	private final int myUid = Process.myUid();
	private int systemPid;
    private boolean isInterceptorEnabled = false;


	private VirtualCore() {}

	public int myUid() {
		return myUid;
	}

	public int myUserId() {
		return VUserHandle.getUserId(myUid);
	}


	public static VirtualCore get() {
		return gCore;
	}

	public static PackageManager getPM() {
		return get().getPackageManager();
	}

	public static Object mainThread() {
		return get().mainThread;
	}


	public int[] getGids() {
		return hostPkgInfo.gids;
	}

	public Context getContext() {
		return context;
	}

	public PackageManager getPackageManager() {
		return context.getPackageManager();
	}

	public String getHostPkg() {
		return hostPkgName;
	}

	public PackageManager getUnHookPackageManager() {
		return unHookPackageManager;
	}


	public void startup(Context context) throws Throwable {
		if (!isStartUp) {
			if (Looper.myLooper() != Looper.getMainLooper()) {
				throw new IllegalStateException("VirtualCore.startup() must called in main thread.");
			}
			this.context = context;
			mainThread = ActivityThread.currentActivityThread.call();
			unHookPackageManager = context.getPackageManager();
			hostPkgInfo = unHookPackageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
			// Host包名
			hostPkgName = context.getApplicationInfo().packageName;
			// 主进程名
			mainProcessName = context.getApplicationInfo().processName;
			// 当前进程名
			processName = ActivityThread.getProcessName.call(mainThread);
			if (processName.equals(mainProcessName)) {
				processType = ProcessType.Main;
			} else if (processName.endsWith(Constants.SERVER_PROCESS_NAME)) {
				processType = ProcessType.Server;
			} else if (VActivityManager.get().isAppProcess(processName)) {
				processType = ProcessType.VAppClient;
			} else {
				processType = ProcessType.CHILD;
			}
			if (isVAppProcess()) {
				systemPid = VActivityManager.get().getSystemPid();
			}
			PatchManager patchManager = PatchManager.getInstance();
			patchManager.init();
			patchManager.injectAll();
            if (isInterceptorEnabled) {
                patchManager.applyInterceptors(InterceptorManager.getInterceptors());
            }
            ContextFixer.fixContext(context);
			isStartUp = true;
		}
	}

	public IAppManager getService() {
		if (mService == null) {
			synchronized (this) {
				if (mService == null) {
					mService = IAppManager.Stub
							.asInterface(ServiceManagerNative.getService(ServiceManagerNative.APP_MANAGER));
				}
			}
		}
		return mService;
	}

	/**
	 * @return 当前进程是否为Virtual App进程
	 */
	public boolean isVAppProcess() {
		return ProcessType.VAppClient == processType;
	}

	/**
	 * @return 当前进程是否为主进程
	 */
	public boolean isMainProcess() {
		return ProcessType.Main == processType;
	}

	/**
	 * @return 当前进程是否为子进程
	 */
	public boolean isChildProcess() {
		return ProcessType.CHILD == processType;
	}

	/**
	 * @return 当前进程是否为服务进程
	 */
	public boolean isServiceProcess() {
		return ProcessType.Server == processType;
	}

	/**
	 * @return 当前进程名
	 */
	public String getProcessName() {
		return processName;
	}

	/**
	 * @return 主进程名
	 */
	public String getMainProcessName() {
		return mainProcessName;
	}

	public void preOpt(String pkg) throws Exception {
		AppSetting info = findApp(pkg);
		if (info != null && !info.dependSystem) {
			DexFile.loadDex(info.apkPath, info.getOdexFile().getPath(), 0).close();
		}
	}

	public InstallResult installApp(String apkPath, int flags) {
		try {
			return getService().installApp(apkPath, flags);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public boolean isAppInstalled(String pkg) {
		try {
			return getService().isAppInstalled(pkg);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public Intent getLaunchIntent(String packageName, int userId) {
		VPackageManager pm = VPackageManager.get();
		Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
		intentToResolve.addCategory(Intent.CATEGORY_INFO);
		intentToResolve.setPackage(packageName);
		List<ResolveInfo> ris = pm.queryIntentActivities(intentToResolve, intentToResolve.resolveType(context), 0, userId);

		// Otherwise, try to find a main launcher activity.
		if (ris == null || ris.size() <= 0) {
			// reuse the intent instance
			intentToResolve.removeCategory(Intent.CATEGORY_INFO);
			intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
			intentToResolve.setPackage(packageName);
			ris = pm.queryIntentActivities(intentToResolve, intentToResolve.resolveType(context), 0, userId);
		}
		if (ris == null || ris.size() <= 0) {
			return null;
		}
		Intent intent = new Intent(intentToResolve);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(ris.get(0).activityInfo.packageName,
				ris.get(0).activityInfo.name);
		return intent;
	}


	public void setLoadingPage(Intent intent, Activity activity) {
		if (activity != null) {
			setLoadingPage(intent, mirror.android.app.Activity.mToken.get(activity));
		}
	}

	public void setLoadingPage(Intent intent, IBinder token) {
		if (token != null) {
			Bundle bundle = new Bundle();
			BundleCompat.putBinder(bundle, "_VA_|_loading_token_", token);
			intent.putExtra("_VA_|_sender_", bundle);
		}
	}

    public void setInterceptorEnabled(boolean interceptorEnabled) {
        isInterceptorEnabled = interceptorEnabled;
    }

    public AppSetting findApp(String pkg) {
		try {
			return getService().findAppInfo(pkg);
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public int getAppCount() {
		try {
			return getService().getAppCount();
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public boolean isStartup() {
		return isStartUp;
	}

	public boolean uninstallApp(String pkgName) {
		try {
			return getService().uninstallApp(pkgName);
		} catch (RemoteException e) {
			// Ignore
		}
		return false;
	}

	public Resources getResources(String pkg) {
		AppSetting appSetting = findApp(pkg);
		if (appSetting != null) {
			AssetManager assets = mirror.android.content.res.AssetManager.ctor.newInstance();
			mirror.android.content.res.AssetManager.addAssetPath.call(assets, appSetting.apkPath);
			Resources hostRes = context.getResources();
			return new Resources(assets, hostRes.getDisplayMetrics(), hostRes.getConfiguration());
		}
		return null;
	}

	public boolean isHostPackageName(String pkgName) {
		return TextUtils.equals(pkgName, context.getPackageName());
	}


	public synchronized ActivityInfo resolveActivityInfo(Intent intent, int userId) {
		ActivityInfo activityInfo = null;
		if (intent.getComponent() == null) {
			ResolveInfo resolveInfo = VPackageManager.get().resolveIntent(intent, intent.getType(), 0, 0);
			if (resolveInfo != null && resolveInfo.activityInfo != null) {
				if (resolveInfo.activityInfo.targetActivity != null) {
					ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.targetActivity);
					resolveInfo.activityInfo = VPackageManager.get().getActivityInfo(componentName, 0, userId);
					if (intent.getComponent() != null) {
						intent.setComponent(componentName);
					}
				}
				activityInfo = resolveInfo.activityInfo;
				if (intent.getComponent() == null) {
					intent.setClassName(activityInfo.packageName, activityInfo.name);
				}
				activityInfoCache.put(intent.getComponent(), activityInfo);
			}
		} else {
			activityInfo = resolveActivityInfo(intent.getComponent(), userId);
		}
		return activityInfo;
	}

	public synchronized ActivityInfo resolveActivityInfo(ComponentName componentName, int userId) {
		ActivityInfo activityInfo = activityInfoCache.get(componentName);
		if (activityInfo == null) {
			activityInfo = VPackageManager.get().getActivityInfo(componentName, 0, userId);
			if (activityInfo != null) {
				activityInfoCache.put(componentName, activityInfo);
			}
		}
		return activityInfo;
	}

	public ServiceInfo resolveServiceInfo(Intent intent, int userId) {
		ServiceInfo serviceInfo = null;
		ResolveInfo resolveInfo = VPackageManager.get().resolveService(intent, intent.getType(), 0, userId);
		if (resolveInfo != null) {
			serviceInfo = resolveInfo.serviceInfo;
		}
		return serviceInfo;
	}

	public void killApp(String pkg) {
		VActivityManager.get().killAppByPkg(pkg);
	}

	public void killAllApps() {
		VActivityManager.get().killAllApps();
	}

	public List<AppSetting> getAllApps() {
		try {
			return getService().getAllApps();
		} catch (RemoteException e) {
			return VirtualRuntime.crash(e);
		}
	}

	public void preloadAllApps() {
		try {
			getService().preloadAllApps();
		} catch (RemoteException e) {
			// Ignore
		}
	}

	public boolean isOutsideInstalled(String packageName) {
		try {
			return unHookPackageManager.getApplicationInfo(packageName, 0) != null;
		} catch (PackageManager.NameNotFoundException e) {
			// Ignore
		}
		return false;
	}

	public int getSystemPid() {
		return systemPid;
	}

	public ContentResolver getContentResolver() {
		return context.getContentResolver();
	}

	/**
	 * 进程类型
	 */
	enum ProcessType {
		/**
		 * Server process
		 */
		Server,
		/**
		 * Virtual app process
		 */
		VAppClient,
		/**
		 * Main process
		 */
		Main,
		/**
		 * Child process
		 */
		CHILD
	}
}
