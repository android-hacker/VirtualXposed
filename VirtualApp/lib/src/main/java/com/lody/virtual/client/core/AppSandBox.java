package com.lody.virtual.client.core;

import android.app.Application;
import android.app.IActivityManager;
import android.app.LoadedApk;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.renderscript.RenderScript;
import android.renderscript.RenderScriptCacheDir;
import android.view.HardwareRenderer;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.env.RuntimeEnv;
import com.lody.virtual.client.fixer.ContextFixer;
import com.lody.virtual.client.local.LocalContentManager;
import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.local.LocalProcessManager;
import com.lody.virtual.helper.compat.ActivityThreadCompat;
import com.lody.virtual.helper.compat.VMRuntimeCompat;
import com.lody.virtual.helper.loaders.ClassLoaderHelper;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.proto.ReceiverInfo;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Lody
 *
 */
public class AppSandBox {

	private static final String TAG = AppSandBox.class.getSimpleName();
	private static HashSet<String> installedApps = new HashSet<String>();
	private static Map<String, Application> applicationMap = new HashMap<>();

	private static String LAST_PKG;

	public static Application getApplication(String pkg) {
		return applicationMap.get(pkg);
	}

	public static void install(final String procName, final String pkg) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			installLocked(procName, pkg);
		} else {
			final CountDownLatch lock = new CountDownLatch(1);
			RuntimeEnv.getUIHandler().post(new Runnable() {
				@Override
				public void run() {
					installLocked(procName, pkg);
					lock.countDown();
				}
			});
			try {
				lock.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void installLocked(String processName, String pkg) {
		if (installedApps.contains(pkg)) {
			return;
		}
		LAST_PKG = pkg;
		PatchManager.fixAllSettings();
		VLog.d(TAG, "Installing %s.", pkg);
		LocalProcessManager.onAppProcessCreate(VClientImpl.getClient().asBinder());
		AppInfo appInfo = VirtualCore.getCore().findApp(pkg);
		if (appInfo == null) {
			return;
		}
		LocalPackageManager pm = LocalPackageManager.getInstance();
		ApplicationInfo applicationInfo = appInfo.applicationInfo;
		RuntimeEnv.setCurrentProcessName(processName, appInfo);

		LoadedApk loadedApk = createLoadedApk(appInfo);
		setupRuntime(applicationInfo);

		ClassLoader classLoader = loadedApk.getClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		Application app = loadedApk.makeApplication(false, null);
		Reflect.on(VirtualCore.mainThread()).set("mInitialApplication", app);
		ContextFixer.fixContext(app.getBaseContext());
		// Install ContentProviders
		List<ProviderInfo> providers = pm.queryContentProviders(processName, 0);
		List<IActivityManager.ContentProviderHolder> holders = new ArrayList<>(providers.size());
		for (ProviderInfo info : providers) {
			IActivityManager.ContentProviderHolder holder = ActivityThreadCompat.installProvider(app, info);
			if (holder != null) {
				holders.add(holder);
			}
		}
		LocalContentManager.getDefault().publishContentProviders(holders);
		// Application => onCreate()
		VirtualCore.mainThread().getInstrumentation().callApplicationOnCreate(app);

		List<ReceiverInfo> receiverInfos = pm.queryReceivers(processName, 0);
		installReceivers(app, receiverInfos);
		LocalProcessManager.onEnterApp(pkg);
		applicationMap.put(appInfo.packageName, app);
		installedApps.add(appInfo.packageName);
	}

	private static void installReceivers(Context app, List<ReceiverInfo> receivers) {
		ClassLoader classLoader = app.getClassLoader();
		for (ReceiverInfo one : receivers) {
			ComponentName component = one.component;
			IntentFilter[] filters = one.filters;
			if (filters == null || filters.length == 0) {
				filters = new IntentFilter[1];
				IntentFilter filter = new IntentFilter();
				filter.addAction(VirtualCore.getReceiverAction(component.getPackageName(), component.getClassName()));
				filters[0] = filter;
			}
			for (IntentFilter filter : filters) {
				try {
					BroadcastReceiver receiver = (BroadcastReceiver) classLoader.loadClass(component.getClassName())
							.newInstance();
					if (one.permission != null) {
						app.registerReceiver(receiver, filter, one.permission, null);
					} else {
						app.registerReceiver(receiver, filter);
					}
				} catch (Throwable e) {
					// Ignore
				}
			}
		}
	}

	private static void setupRuntime(ApplicationInfo applicationInfo) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
				&& applicationInfo.targetSdkVersion < Build.VERSION_CODES.LOLLIPOP) {
			try {
				Message.updateCheckRecycle(applicationInfo.targetSdkVersion);
			} catch (Throwable e) {
				// Ignore
			}
		}
		VMRuntimeCompat.setTargetSdkVersion(applicationInfo.targetSdkVersion);

		Context appContext = createAppContext(applicationInfo);
		File codeCacheDir;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
	}


	private static Context createAppContext(ApplicationInfo appInfo) {
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
		ClassLoader classLoader = ClassLoaderHelper.create(appInfo);
		Reflect.on(loadedApk).set("mClassLoader", classLoader);
		try {
			Reflect.on(loadedApk).set("mSecurityViolation", false);
		} catch (Throwable e) {
			// Ignore
		}
		return loadedApk;
	}


}
