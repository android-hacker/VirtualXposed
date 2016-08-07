package com.lody.virtual.client.core;

import android.app.Activity;
import android.app.ActivityThread;
import android.content.ComponentName;
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
import android.os.RemoteException;
import android.text.TextUtils;

import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.fixer.ContextFixer;
import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.local.LocalProcessManager;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.compat.ActivityThreadCompat;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.loaders.ClassLoaderHelper;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.service.IAppManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lody
 * @version 2.2
 */
public final class VirtualCore {

	private static VirtualCore gCore = new VirtualCore();
	/**
	 * 纯净无钩子的PackageManager
	 */
	private PackageManager unHookPackageManager;
	/**
	 * Host包名
	 */
	private String pkgName;
	/**
	 * 在API 16以前, ActivityThread通过ThreadLocal管理, 非主线程调用为空, 故在此保存实例.
	 */
	private ActivityThread mainThread;
	private Context context;

	private Object hostBindData;
	/**
	 * 主进程名
	 */
	private String mainProcessName;
	/**
	 * 当前进程名
	 */
	private String processName;
	private ProcessType processType;
	private IAppManager mService;
	private boolean isStartUp;
	private PackageInfo hostPkgInfo;
	private Map<ComponentName, ActivityInfo> activityInfoCache = new HashMap<ComponentName, ActivityInfo>();

	private VirtualCore() {

	}

	public static Object getHostBindData() {
		return getCore().hostBindData;
	}

	public static VirtualCore getCore() {
		return gCore;
	}

	public static PackageManager getPM() {
		return getCore().getPackageManager();
	}

	public static ActivityThread mainThread() {
		return getCore().mainThread;
	}

	public static String getPermissionBroadcast() {
		return "com.lody.virtual.permission.VIRTUAL_BROADCAST";
	}

	public static ComponentName getOriginComponentName(String action) {
		String brc = String.format("%s.BRC_", getCore().getHostPkg());
		if (action != null && action.startsWith(brc)) {
			String comStr = action.replaceFirst(brc, "");
			comStr = comStr.replace("_", "/");
			return ComponentName.unflattenFromString(comStr);
		}
		return null;
	}

	public static String getReceiverAction(String packageName, String className) {
		if (className != null && className.startsWith(".")) {
			className = packageName + className;
		}
		String extAction = packageName + "_" + className;
		return String.format("%s.BRC_%s", getCore().getHostPkg(), extAction);
	}

	public int[] getGids() {
		return hostPkgInfo.gids;
	}

	public PackageInfo getHostPkgInfo() {
		return hostPkgInfo;
	}

	public Context getContext() {
		return context;
	}

	public PackageManager getPackageManager() {
		return context.getPackageManager();
	}

	public String getHostPkg() {
		return pkgName;
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
			mainThread = ActivityThread.currentActivityThread();
			hostBindData = ActivityThreadCompat.getBoundApplication(mainThread);
			unHookPackageManager = context.getPackageManager();
			hostPkgInfo = unHookPackageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
			// Host包名
			pkgName = context.getPackageName();
			// 主进程名
			mainProcessName = context.getApplicationInfo().processName;
			// 当前进程名
			processName = mainThread.getProcessName();
			if (processName.equals(mainProcessName)) {
				processType = ProcessType.Main;
			} else if (processName.endsWith(Constants.SERVER_PROCESS_NAME)) {
				processType = ProcessType.Server;
			} else if (LocalProcessManager.isAppProcess(processName)) {
				processType = ProcessType.VAppClient;
			} else {
				processType = ProcessType.CHILD;
			}
			PatchManager patchManager = PatchManager.getInstance();
			patchManager.injectAll();
			patchManager.checkEnv();
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
		AppInfo info = findApp(pkg);
		if (info != null && !info.dependSystem) {
			ClassLoaderHelper.create(info);
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

	public Intent getLaunchIntent(String pkg) {
		AppInfo info = findApp(pkg);
		if (info != null) {
			Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
			if (intent == null) {
				throw new IllegalStateException("Unable to launch the app named : " + pkg);
			}
			return intent;
		} else {
			throw new IllegalStateException("Unable to find app named : " + pkg);
		}
	}

	public void launchApp(String pkgName) throws Throwable {
		Intent intent = getLaunchIntent(pkgName);
		context.startActivity(intent);
	}

	public void addLoadingPage(Intent intent, Activity activity) {
		if (activity != null) {
			addLoadingPage(intent, activity.getActivityToken());
		}
	}

	public void addLoadingPage(Intent intent, IBinder token) {
		if (token != null) {
			Bundle bundle = new Bundle();
			BundleCompat.putBinder(bundle, ExtraConstants.EXTRA_BINDER, token);
			intent.putExtra(ExtraConstants.EXTRA_SENDER, bundle);
		}
	}

	public AppInfo findApp(final String pkg) {
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
		AppInfo appInfo = findApp(pkg);
		if (appInfo != null) {
			AssetManager assets = new AssetManager();
			assets.addAssetPath(appInfo.apkPath);
			Resources hostRes = context.getResources();
			return new Resources(assets, hostRes.getDisplayMetrics(), hostRes.getConfiguration());
		}
		return null;
	}

	public boolean isHostPackageName(String pkgName) {
		return TextUtils.equals(pkgName, context.getPackageName());
	}

	public synchronized ActivityInfo resolveActivityInfo(Intent intent) {
		ActivityInfo activityInfo = null;
		if (intent.getComponent() == null) {
			ResolveInfo resolveInfo = LocalPackageManager.getInstance().resolveIntent(intent, intent.getType(), 0);
			if (resolveInfo != null && resolveInfo.activityInfo != null) {
				activityInfo = resolveInfo.activityInfo;
				intent.setClassName(activityInfo.packageName, activityInfo.name);
				activityInfoCache.put(intent.getComponent(), activityInfo);
			}
		} else {
			activityInfo = resolveActivityInfo(intent.getComponent());
		}
		return activityInfo;
	}

	public synchronized ActivityInfo resolveActivityInfo(ComponentName componentName) {
		ActivityInfo activityInfo = activityInfoCache.get(componentName);
		if (activityInfo == null) {
			activityInfo = LocalPackageManager.getInstance().getActivityInfo(componentName, 0);
			if (activityInfo != null) {
				activityInfoCache.put(componentName, activityInfo);
			}
		}
		return activityInfo;
	}

	public ServiceInfo resolveServiceInfo(Intent intent) {
		ServiceInfo serviceInfo = null;
		ResolveInfo resolveInfo = LocalPackageManager.getInstance().resolveService(intent, intent.getType(), 0);
		if (resolveInfo != null) {
			serviceInfo = resolveInfo.serviceInfo;
		}
		return serviceInfo;
	}

	public void killApp(String pkg) {
		LocalProcessManager.killAppByPkg(pkg);
	}

	public void killAllApps() {
		LocalProcessManager.killAllApps();
	}

	public List<AppInfo> getAllApps() {
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
