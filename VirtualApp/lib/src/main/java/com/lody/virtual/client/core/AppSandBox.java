package com.lody.virtual.client.core;

import android.app.Application;
import android.app.LoadedApk;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.Message;
import android.os.StrictMode;
import android.renderscript.RenderScript;
import android.renderscript.RenderScriptCacheDir;
import android.text.TextUtils;
import android.view.HardwareRenderer;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.env.RuntimeEnv;
import com.lody.virtual.client.hook.modifiers.ContextModifier;
import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.local.LocalProcessManager;
import com.lody.virtual.helper.compat.ActivityThreadCompat;
import com.lody.virtual.helper.compat.VMRuntimeCompat;
import com.lody.virtual.helper.loaders.PathAppClassLoader;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.XLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Lody
 *
 */
public class AppSandBox {

	private static final String TAG = AppSandBox.class.getSimpleName();
	private static HashSet<String> installedApps = new HashSet<String>();
	private static Map<String, Application> applicationMap = new HashMap<>();

	private static boolean sInstalling = false;

	public static Application getApplication(String pkg) {
		return applicationMap.get(pkg);
	}


	public static void install(String procName, String pkg) {
		sInstalling = true;
		if (installedApps.contains(pkg)) {
			return;
		}
		XLog.d(TAG, "Installing %s.", pkg);
		LocalProcessManager.onAppProcessCreate(VClientImpl.getClient().asBinder());
		AppInfo appInfo = VirtualCore.getCore().findApp(pkg);
		if (appInfo == null) {
			return;
		}
		ApplicationInfo applicationInfo = appInfo.applicationInfo;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L && applicationInfo.targetSdkVersion < Build.VERSION_CODES.L) {
			try {
				Message.updateCheckRecycle(applicationInfo.targetSdkVersion);
			} catch (Throwable e) {
				// Ignore
			}
		}
		VMRuntimeCompat.setTargetSdkVersion(applicationInfo.targetSdkVersion);

		LoadedApk loadedApk = createLoadedApk(appInfo);

		Context appContext = createAppContext(applicationInfo);
		RuntimeEnv.setCurrentProcessName(procName, appInfo);

		File codeCacheDir;

		if (Build.VERSION.SDK_INT >= 23) {
			codeCacheDir = appContext.getCodeCacheDir();
		} else {
			codeCacheDir = appContext.getCacheDir();
		}
		if (codeCacheDir != null) {
			System.setProperty("java.io.tmpdir", codeCacheDir.getPath());
			try {
				HardwareRenderer.setupDiskCache(codeCacheDir);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			if (Build.VERSION.SDK_INT >= 23) {
				try {
					RenderScriptCacheDir.setupDiskCache(codeCacheDir);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else if (Build.VERSION.SDK_INT >= 16) {
				try {
					Reflect.on(RenderScript.class).call("setupDiskCache", codeCacheDir);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
		if (applicationInfo.targetSdkVersion <= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy());
			builder.permitNetwork();
			StrictMode.setThreadPolicy(builder.build());
		}

		List<ProviderInfo> providers = null;

		try {
			PackageInfo pkgInfo = VirtualCore.getPM().getPackageInfo(pkg, PackageManager.GET_PROVIDERS);
			if (pkgInfo.providers != null) {
				providers = new ArrayList<ProviderInfo>(pkgInfo.providers.length);
				for (ProviderInfo providerInfo : pkgInfo.providers) {
					if (TextUtils.equals(procName, providerInfo.processName)) {
						providers.add(providerInfo);
					}
				}
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		ClassLoader classLoader = loadedApk.getClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);
		Application app = loadedApk.makeApplication(false, null);
		Reflect.on(VirtualCore.mainThread()).set("mInitialApplication", app);
		ContextModifier.modifyContext(app.getBaseContext());
		if (providers != null) {
			ActivityThreadCompat.installContentProviders(app, providers);
		}
		VirtualCore.mainThread().getInstrumentation().callApplicationOnCreate(app);
		LocalPackageManager pm = LocalPackageManager.getInstance();

		List<ActivityInfo> receivers = pm.getReceivers(pkg, 0);
		for (ActivityInfo receiverInfo : receivers) {
			if (TextUtils.equals(receiverInfo.processName, procName)) {
				List<IntentFilter> filters = pm.getReceiverIntentFilter(receiverInfo);
				if (filters != null && filters.size() > 0) {
					for (IntentFilter filter : filters) {
						try {
							BroadcastReceiver receiver = (BroadcastReceiver) classLoader.loadClass(receiverInfo.name)
									.newInstance();
							if (receiverInfo.permission != null) {
								app.registerReceiver(receiver, filter, receiverInfo.permission, null);
							} else {
								app.registerReceiver(receiver, filter);
							}
						} catch (Throwable e) {
							// Ignore
						}
					}
				} else {
					try {
						BroadcastReceiver receiver = (BroadcastReceiver) classLoader.loadClass(receiverInfo.name)
								.newInstance();
						IntentFilter filter = new IntentFilter();
						filter.addAction(VirtualCore.getReceiverAction(receiverInfo.packageName, receiverInfo.name));
						if (receiverInfo.permission != null) {
							app.registerReceiver(receiver, filter, receiverInfo.permission, null);
						} else {
							app.registerReceiver(receiver, filter);
						}
					} catch (Throwable e) {
						// Ignore
					}
				}
			}
		}
		LocalProcessManager.onEnterApp(pkg);
		applicationMap.put(appInfo.packageName, app);
		installedApps.add(appInfo.packageName);
		sInstalling = false;
		XLog.d(TAG, "Application of Process(%s) have launched. ", RuntimeEnv.getCurrentProcessName());
	}

	public static boolean isInstalling() {
		return sInstalling;
	}

	public static Context createAppContext(ApplicationInfo appInfo) {
		Context context = VirtualCore.getCore().getContext();
		try {
			return context.createPackageContext(appInfo.packageName,
					Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static LoadedApk createLoadedApk(AppInfo appInfo) {
		ApplicationInfo applicationInfo = appInfo.applicationInfo;
		LoadedApk loadedApk = ActivityThreadCompat.getPackageInfoNoCheck(applicationInfo);
		PathAppClassLoader classLoader;
		ApplicationInfo outsideAppInfo = null;
		try {
			outsideAppInfo = VirtualCore.getCore().getUnHookPackageManager().getApplicationInfo(appInfo.packageName,
					PackageManager.GET_SHARED_LIBRARY_FILES);
		} catch (PackageManager.NameNotFoundException e) {
			// Ignore
		}
		if (outsideAppInfo != null) {
			classLoader = new PathAppClassLoader(appInfo, outsideAppInfo);
		} else {
			classLoader = new PathAppClassLoader(appInfo);
		}
		Reflect.on(loadedApk).set("mClassLoader", classLoader);
		return loadedApk;
	}

	public static Set<String> getInstalledPackages() {
		return new HashSet<>(installedApps);
	}

}
