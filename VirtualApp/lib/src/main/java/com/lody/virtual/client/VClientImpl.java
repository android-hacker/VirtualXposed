package com.lody.virtual.client;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.app.LoadedApk;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.res.CompatibilityInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.PathClassLoader;

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

	public String geCurrentPackage() {
		return mBoundApplication.appInfo.packageName;
	}

	private final class AppBindData {
		String processName;
		ApplicationInfo appInfo;
		List<String> sharedPackages;
		List<ProviderInfo> providers;
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
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public synchronized void start() {
				new Exception().printStackTrace();
				super.start();
			}
		});
		IOHook.startDexOverride();
		IOHook.hookNative();
		ContextFixer.fixCamera();
		mBoundApplication = data;
		ActivityThread mainThread = VirtualCore.mainThread();
		mBoundApplication.info = mainThread.getPackageInfoNoCheck(data.appInfo,
				CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO);
		// T_T   T_T   T_T   T_T   T_T   T_T   T_T   T_T
		// Gms use the {com.android.location.provider.jar}
		// T_T   T_T   T_T   T_T   T_T   T_T   T_T   T_T
		if (data.processName.equals("com.google.android.gms.persistent")) {
			File file = new File("/system/framework/com.android.location.provider.jar");
			if (file.exists()) {
				PathClassLoader parent = new PathClassLoader(file.getPath(), ClassLoader.getSystemClassLoader().getParent());
				Reflect.on(mBoundApplication.info).set("mBaseClassLoader", parent);
			}
		}
		fixBoundApp(mBoundApplication, VirtualCore.getHostBindData());
		Application app = data.info.makeApplication(false, null);
		mInitialApplication = app;
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

		List<ReceiverInfo> receivers = VPackageManager.getInstance().queryReceivers(data.processName, 0);
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
			List<ProviderInfo> providers) {
		VirtualRuntime.setupRuntime(processName, appInfo);
		final AppBindData appBindData = new AppBindData();
		appBindData.processName = processName;
		appBindData.appInfo = appInfo;
		appBindData.sharedPackages = sharedPackages;
		appBindData.providers = providers;
		sendMessage(BIND_APPLICATION, appBindData);
	}
}
