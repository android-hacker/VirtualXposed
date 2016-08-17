package com.lody.virtual.client;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.app.LoadedApk;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.CompatibilityInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;

import com.lody.virtual.IOHook;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.fixer.ContextFixer;
import com.lody.virtual.client.hook.delegate.AppInstrumentation;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.compat.ActivityThreadCompat;
import com.lody.virtual.helper.compat.AppBindDataCompat;
import com.lody.virtual.helper.proto.ReceiverInfo;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 */

public class VClientImpl extends IVClient.Stub {

	private static final int BIND_APPLICATION = 10;

	private static final String TAG = VClientImpl.class.getSimpleName();

	private static final VClientImpl gClient = new VClientImpl();
	private Instrumentation mInstrumentation = AppInstrumentation.getDefault();

	private IBinder token;
	private final H mH = new H();
	private AppBindData mBoundApplication;
	private Application mInitialApplication;

	public boolean isBound() {
		return mBoundApplication != null;
	}

	public List<String> getSharedPackages() {
		return mBoundApplication.sharedPackages;
	}

	public Application getCurrentApplication() {
		return mInitialApplication;
	}

	public String getCurrentPackage() {
		return mBoundApplication != null ? mBoundApplication.appInfo.packageName : null;
	}

	public int getVUid() {
		return mBoundApplication != null ? mBoundApplication.appInfo.uid : -1;
	}

	public ClassLoader getClassLoader(ApplicationInfo appInfo) {
		LoadedApk apk = VirtualCore.mainThread().getPackageInfoNoCheck(appInfo, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);
		return apk.getClassLoader();
	}


	private final class AppBindData {
		String processName;
		ApplicationInfo appInfo;
		List<String> sharedPackages;
		List<ProviderInfo> providers;
		List<String> usesLibraries;
		LoadedApk info;
	}

	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mH.sendMessage(msg);
	}


	public static VClientImpl getClient() {
		return gClient;
	}

	@Override
	public IBinder getAppThread() throws RemoteException {
		return VirtualCore.mainThread().getApplicationThread();
	}

	@Override
	public IBinder getToken() throws RemoteException {
		return token;
	}

	public void setToken(IBinder token) {
		if (this.token != null) {
			throw new IllegalStateException("Token is exist!");
		}
		this.token = token;
	}

	private class H extends Handler {

		private H() {
			super(Looper.getMainLooper());
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BIND_APPLICATION: {
					handleBindApplication((AppBindData)msg.obj);
				} break;
			}
		}
	}

	private void handleBindApplication(AppBindData data) {
		VLog.d(TAG, "VClient bound, uid : %d, dataPath : %s, processName : %s.", data.appInfo.uid, data.appInfo.dataDir, data.processName);
		mBoundApplication = data;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public synchronized void start() {
				new Exception().printStackTrace();
				super.start();
			}
		});
		ThreadGroup systemGroup = new ThreadGroup("va-system") {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				VLog.e(TAG, e);
				Process.killProcess(Process.myPid());
			}
		};
		ThreadGroup root = Thread.currentThread().getThreadGroup();
		while (true) {
			ThreadGroup parent = root.getParent();
			if (parent == null) {
				break;
			}
			root = parent;
		}
		try {
			Reflect.on(root).set("parent", systemGroup);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		IOHook.hookNative();
		ActivityThread mainThread = VirtualCore.mainThread();
		Reflect.on(mainThread).set("mInitialApplication", null);
		IOHook.startDexOverride();
		ContextFixer.fixCamera();
		List<String> libraries = new ArrayList<>();
		if (data.usesLibraries != null) {
			boolean fail = false;
			for (String library : data.usesLibraries) {
				if (library.equals("android.test.runner")) {
					continue;
				}
				try {
					ApplicationInfo info = VirtualCore.getPM().getApplicationInfo(library, 0);
					if (info.sourceDir != null) {
						libraries.add(info.sourceDir);
					}
				} catch (Throwable e) {
					fail = true;
				}
				if (fail) {
					File file = new File("/system/framework/" + library + ".jar");
					if (file.exists()) {
						libraries.add(file.getPath());
						fail = false;
					} else {
						file = new File("/system/framework/" + library + ".boot.jar");
						if (file.exists()) {
							libraries.add(file.getPath());
							fail = false;
						}
					}
				}
				if (fail) {
					VLog.w(TAG, "Unable to detect the library : %s.", library);
				}
			}
		}
		data.appInfo.sharedLibraryFiles = libraries.toArray(new String[libraries.size()]);
		Context context;
		try {
			Context hostContext = VirtualCore.getCore().getContext();
			while (hostContext instanceof ContextWrapper) {
				hostContext = ((ContextWrapper) hostContext).getBaseContext();
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				Reflect.on(hostContext).set("mUser", new UserHandle(VUserHandle.myUserId()));
			}
			context = hostContext.createPackageContext(data.appInfo.packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		VLog.d(TAG, "Pre path : %s.", context.getSharedPrefsFile("a").getPath());
		mBoundApplication.info = Reflect.on(context).get("mPackageInfo");

		fixBoundApp(mBoundApplication, VirtualCore.getHostBindData());
		Application app = data.info.makeApplication(false, null);
		mInitialApplication = app;
		Reflect.on(mainThread).set("mInitialApplication", mInitialApplication);
		ContextFixer.fixContext(app);
		List<ProviderInfo> providers = data.providers;
		if (providers != null) {
			installContentProviders(app, providers);
		}
		try {
			mInstrumentation.callApplicationOnCreate(app);
		} catch (Exception e) {
			if (!mInstrumentation.onException(app, e)) {
				throw new RuntimeException(
						"Unable to create application " + app.getClass().getName()
								+ ": " + e.toString(), e);
			}
		}

		List<ReceiverInfo> receivers = VPackageManager.getInstance().queryReceivers(data.processName,0 , 0);
		installReceivers(app, receivers);
		VActivityManager.getInstance().appDoneExecuting();
	}

	private void fixBoundApp(AppBindData data, Object hostBindData) {
		AppBindDataCompat compat = new AppBindDataCompat(hostBindData);
		compat.setAppInfo(data.appInfo);
		compat.setProcessName(data.processName);
		compat.setInfo(data.info);
	}

	private void installContentProviders(Context context, List<ProviderInfo> providers) {
		final ArrayList<IActivityManager.ContentProviderHolder> results =
				new ArrayList<IActivityManager.ContentProviderHolder>();

		for (ProviderInfo cpi : providers) {
			IActivityManager.ContentProviderHolder cph = ActivityThreadCompat.installProvider(context, cpi);
			if (cph != null) {
				cph.noReleaseNeeded = true;
				results.add(cph);
			}
		}
		VActivityManager.getInstance().publishContentProviders(results);
	}

	private static void installReceivers(Context context, List<ReceiverInfo> receivers) {
		ClassLoader classLoader = context.getClassLoader();
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
						context.registerReceiver(receiver, filter, one.permission, null);
					} else {
						context.registerReceiver(receiver, filter);
					}
				} catch (Throwable e) {
					// Ignore
				}
			}
		}
	}


	@Override
	public void bindApplication(String processName, ApplicationInfo appInfo, List<String> sharedPackages,
			List<ProviderInfo> providers, List<String> usesLibraries) {
		VirtualRuntime.setupRuntime(processName, appInfo);
		final AppBindData appBindData = new AppBindData();
		appBindData.processName = processName;
		appBindData.appInfo = appInfo;
		appBindData.sharedPackages = sharedPackages;
		appBindData.providers = providers;
		appBindData.usesLibraries = usesLibraries;
		sendMessage(BIND_APPLICATION, appBindData);
	}
}
