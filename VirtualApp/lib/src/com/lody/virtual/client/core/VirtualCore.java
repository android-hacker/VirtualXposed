package com.lody.virtual.client.core;

import android.app.ActivityThread;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Looper;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.env.RuntimeEnv;
import com.lody.virtual.client.local.LocalProcessManager;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.compat.ActivityThreadCompat;
import com.lody.virtual.helper.loaders.DexAppClassLoader;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.service.IAppManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private Application application;
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
	private Set<String> hostProviderAuths = new HashSet<String>(5);
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

	public PackageInfo getHostPkgInfo() {
		return hostPkgInfo;
	}

	public Context getContext() {
		return context;
	}

	public Application getApplication() {
		return application;
	}

	public PackageManager getPackageManager() {
		return application.getPackageManager();
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
			ProviderInfo[] hostProviders = hostPkgInfo.providers;
			if (hostProviders != null) {
				for (ProviderInfo info : hostProviders) {
					hostProviderAuths.add(info.authority);
				}
			}
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
			RuntimeEnv.init();
			PatchManager.getInstance().fixContext(context);
			startupInner();
			isStartUp = true;
		}
	}

	public boolean isHostProvider(String auth) {
		return auth != null && hostProviderAuths.contains(auth);
	}

	public void handleApplication(Application application) {
		this.application = application;
		if (isVAppProcess()) {
			String procName = LocalProcessManager.getAppProcessName(Process.myPid());
			String pluginPkg = procName.split(":")[0];
			AppInfo appInfo = VirtualCore.getCore().findApp(pluginPkg);
			if (appInfo == null) {
				throw new RuntimeException("Unable to find AppInfo :" + pluginPkg);
			}
			if (isVAppProcess()) {
				AppSandBox.install(procName, appInfo);
			}
		}
	}

	public IAppManager getService() {
		if (mService == null) {
			synchronized (this) {
				if (mService == null) {
					mService = IAppManager.Stub
							.asInterface(ServiceManagerNative.getService(ServiceManagerNative.PLUGIN_MANAGER));
				}
			}
		}
		return mService;
	}

	private void startupInner() {
		if (isVAppProcess()) {
			// 在插件Application创建前伪装插件进程名
			String plugProcName = LocalProcessManager.getMapAppProcessName(VirtualCore.getCore().getProcessName());
			if (plugProcName == null) {
				RuntimeEnv.exit();
				return;
			}
			String pkg = plugProcName.split(":")[0];
			AppInfo info = findApp(pkg);
			if (info == null) {
				RuntimeEnv.exit();
				return;
			}
			ServiceManagerNative.startup(context);
			// 插件进程要向服务端报告，进程初始化完毕
			LocalProcessManager.onAppProcessCreate(VClientImpl.getClient().asBinder());
			notifyOnEnterApp(pkg);
		}
	}

	public void notifyOnEnterApp(String pluginPkg) {
		LocalProcessManager.onEnterApp(pluginPkg);
	}

	public void notifyOnEnterAppProcessName(String plugProcName) {
		LocalProcessManager.onEnterAppProcessName(plugProcName);
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
		if (info != null && !info.isInstalled()) {
			new DexAppClassLoader(info);
		}
	}

	public InstallResult installApp(String apkPath, int flags) {
		try {
			return getService().installApp(apkPath, flags);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}

	public boolean isAppInstalled(String pkg) {
		try {
			return getService().isAppInstalled(pkg);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}
	public void launchApp(String pkgName) throws Throwable {
		launchApp(pkgName, null);
	}

	public void launchApp(String pkgName, Messenger messenger) throws Throwable {
		AppInfo pluginPackage = findApp(pkgName);
		if (pluginPackage != null) {
			Intent intent = getPM().getLaunchIntentForPackage(pluginPackage.packageName);
			if (intent == null) {
				throw new IllegalStateException("Unable to launch the app named : " + pkgName);
			}
			if (messenger != null) {
				intent.putExtra(ExtraConstants.EXTRA_MESSENGER, messenger);
			}
			context.startActivity(intent);
		} else {
			throw new IllegalStateException("Unable to find plugin named : " + pkgName);
		}

	}

	public AppInfo findApp(final String pkg) {
		try {
			return getService().findAppInfo(pkg);
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
		}
	}


	public int getAppCount() {
		try {
			return getService().getAppCount();
		} catch (RemoteException e) {
			return RuntimeEnv.crash(e);
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
			ResolveInfo resolveInfo = getPM().resolveActivity(intent, 0);
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
			try {
				activityInfo = getPM().getActivityInfo(componentName, 0);
				if (activityInfo != null) {
					activityInfoCache.put(componentName, activityInfo);
				}
			} catch (PackageManager.NameNotFoundException e) {
				// Ignore
			}
		}
		return activityInfo;
	}

	public ServiceInfo resolveServiceInfo(Intent intent) {
		ServiceInfo serviceInfo = null;
		if (intent.getComponent() == null) {
			ResolveInfo resolveInfo = getPM().resolveService(intent, 0);
			if (resolveInfo != null && resolveInfo.serviceInfo != null) {
				serviceInfo = resolveInfo.serviceInfo;
			}
		} else {
			try {
				serviceInfo = getPM().getServiceInfo(intent.getComponent(), 0);
			} catch (PackageManager.NameNotFoundException e) {
				// Ignore
			}
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
			e.printStackTrace();
		}
		// noinspection unchecked
		return Collections.EMPTY_LIST;
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
			return unHookPackageManager.getPackageInfo(packageName, 0) != null;
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
		 * 服务端进程
		 */
		Server,
		/**
		 * 插件客户端进程
		 */
		VAppClient,
		/**
		 * 主进程
		 */
		Main,
		/**
		 * 子进程
		 */
		CHILD
	}
}
