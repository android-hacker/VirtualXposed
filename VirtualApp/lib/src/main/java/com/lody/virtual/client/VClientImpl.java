package com.lody.virtual.client;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;

import com.lody.virtual.IOHook;
import com.lody.virtual.client.core.PatchManager;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.fixer.ContextFixer;
import com.lody.virtual.client.hook.delegate.AppInstrumentation;
import com.lody.virtual.client.hook.patchs.am.HCallbackHook;
import com.lody.virtual.client.hook.providers.ProviderHook;
import com.lody.virtual.client.hook.secondary.ProxyServiceFactory;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.secondary.FakeIdentityBinder;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mirror.android.app.ActivityThread;
import mirror.android.app.ContextImpl;
import mirror.android.app.ContextImplICS;
import mirror.android.app.ContextImplKitkat;
import mirror.android.app.IActivityManager;
import mirror.android.app.LoadedApk;
import mirror.android.renderscript.RenderScriptCacheDir;
import mirror.android.view.HardwareRenderer;
import mirror.android.view.RenderScript;
import mirror.com.android.internal.content.ReferrerIntent;
import mirror.dalvik.system.VMRuntime;

import static com.lody.virtual.os.VUserHandle.getUserId;

/**
 * @author Lody
 */

public final class VClientImpl extends IVClient.Stub {

	private static final int NEW_INTENT = 11;

	private static final String TAG = VClientImpl.class.getSimpleName();

	private ConditionVariable mTempLock;

	@SuppressLint("StaticFieldLeak")
	private static final VClientImpl gClient = new VClientImpl();
	private Instrumentation mInstrumentation = AppInstrumentation.getDefault();

	private IBinder token;
	private int vuid;
	private final H mH = new H();
	private AppBindData mBoundApplication;
	private Application mInitialApplication;

	public boolean isBound() {
		return mBoundApplication != null;
	}


	public Application getCurrentApplication() {
		return mInitialApplication;
	}

	public String getCurrentPackage() {
		return mBoundApplication != null ? mBoundApplication.appInfo.packageName : null;
	}

	public int getVUid() {
		return vuid;
	}

	public int getBaseVUid() {
		return VUserHandle.getAppId(vuid);
	}

	public ClassLoader getClassLoader(ApplicationInfo appInfo) {
		Context context = createPackageContext(appInfo.packageName);
		return context.getClassLoader();
	}


	private final class NewIntentData {
		String creator;
		IBinder token;
		Intent intent;
	}
	private final class AppBindData {
		String processName;
		ApplicationInfo appInfo;
		List<ProviderInfo> providers;
		Object info;
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
	public IBinder getAppThread() {
		Binder appThread = ActivityThread.getApplicationThread.call(VirtualCore.mainThread());
		return new FakeIdentityBinder(appThread) {
			@Override
			protected int getFakeUid() {
				return Process.SYSTEM_UID;
			}
		};
	}

	@Override
	public IBinder getToken() {
		return token;
	}

	public void initProcess(IBinder token, int vuid) {
		if (this.token != null) {
			throw new IllegalStateException("Token is exist!");
		}
		this.token = token;
		this.vuid = vuid;
	}

	private class H extends Handler {

		private H() {
			super(Looper.getMainLooper());
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case NEW_INTENT: {
					handleNewIntent((NewIntentData) msg.obj);
				} break;
			}
		}
	}

	private void handleNewIntent(NewIntentData data) {
		Intent intent;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			intent = ReferrerIntent.ctor.newInstance(data.intent, data.creator);
		} else {
			intent = data.intent;
		}
		ActivityThread.performNewIntents.call(
				VirtualCore.mainThread(),
				data.token,
				Collections.singletonList(intent)
		);
	}

	public void bindApplication(final String packageName, final String processName) {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			bindApplicationNoCheck(packageName, processName, new ConditionVariable());
		} else {
			final ConditionVariable lock = new ConditionVariable();
			VirtualRuntime.getUIHandler().post(new Runnable() {
				@Override
				public void run() {
					bindApplicationNoCheck(packageName, processName, lock);
					lock.open();
				}
			});
			lock.block();
		}
	}

	private void bindApplicationNoCheck(String packageName, String processName, ConditionVariable lock) {
		mTempLock = lock;
		try {
			fixInstalledProviders();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		ActivityThread.mInitialApplication.set(
				VirtualCore.mainThread(),
				null
		);
		AppBindData data = new AppBindData();
		data.appInfo = VPackageManager.get().getApplicationInfo(packageName, 0, getUserId(vuid));
		data.processName = processName;
		data.providers = VPackageManager.get().queryContentProviders(processName, getVUid(), PackageManager.GET_META_DATA);
		mBoundApplication = data;
		VirtualRuntime.setupRuntime(data.processName, data.appInfo);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public synchronized void start() {
				new Exception().printStackTrace();
				super.start();
			}
		});
		if (data.appInfo.targetSdkVersion < Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.ThreadPolicy newPolicy = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy()).permitNetwork().build();
			StrictMode.setThreadPolicy(newPolicy);
		}
		IOHook.hookNative();
		Object mainThread = VirtualCore.mainThread();
		IOHook.startDexOverride();
		Context context = createPackageContext(data.appInfo.packageName);
		System.setProperty("java.io.tmpdir", context.getCacheDir().getAbsolutePath());
		File codeCacheDir;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			codeCacheDir = context.getCodeCacheDir();
		} else {
			codeCacheDir = context.getCacheDir();
		}
		if (HardwareRenderer.setupDiskCache != null) {
			HardwareRenderer.setupDiskCache.call(codeCacheDir);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (RenderScriptCacheDir.setupDiskCache != null) {
				RenderScriptCacheDir.setupDiskCache.call(codeCacheDir);
			}
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			if (RenderScript.setupDiskCache != null) {
				RenderScript.setupDiskCache.call(codeCacheDir);
			}
		}
		File filesDir = new File(data.appInfo.dataDir, "files");
		File cacheDir = new File(data.appInfo.dataDir, "cache");
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
			if (ContextImplICS.mExternalFilesDir != null) {
				ContextImplICS.mExternalFilesDir.set(context, filesDir);
			}
			if (ContextImplICS.mExternalCacheDir != null) {
				ContextImplICS.mExternalCacheDir.set(context, cacheDir);
			}
		} else {
			if (ContextImplKitkat.mExternalCacheDirs != null) {
				ContextImplKitkat.mExternalCacheDirs.set(context, new File[] {cacheDir});
			}
			if (ContextImplKitkat.mExternalFilesDirs != null) {
				ContextImplKitkat.mExternalFilesDirs.set(context, new File[] {filesDir});
			}

		}
		Object boundApp = fixBoundApp(mBoundApplication);
		mBoundApplication.info = ContextImpl.mPackageInfo.get(context);
		mirror.android.app.ActivityThread.AppBindData.info.set(boundApp, data.info);
		VMRuntime.setTargetSdkVersion.call(VMRuntime.getRuntime.call(), data.appInfo.targetSdkVersion);

		Application app = LoadedApk.makeApplication.call(data.info, false, null);
		mInitialApplication = app;
		mirror.android.app.ActivityThread.mInitialApplication.set(mainThread, app);
		ContextFixer.fixContext(app);
		List<ProviderInfo> providers = VPackageManager.get().queryContentProviders(data.processName, vuid, PackageManager.GET_META_DATA);
		if (providers != null) {
			installContentProviders(app, providers);
		}
		if (lock != null) {
			lock.open();
			mTempLock = null;
		}
		try {
			mInstrumentation.callApplicationOnCreate(app);
			PatchManager.getInstance().checkEnv(HCallbackHook.class);
			PatchManager.getInstance().checkEnv(AppInstrumentation.class);
			mInitialApplication = ActivityThread.mInitialApplication.get(mainThread);
		} catch (Exception e) {
			if (!mInstrumentation.onException(app, e)) {
				throw new RuntimeException(
						"Unable to create application " + app.getClass().getName()
								+ ": " + e.toString(), e);
			}
		}
		VActivityManager.get().appDoneExecuting();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				ex.printStackTrace();
				Process.killProcess(Process.myPid());
			}
		});
	}

	private Context createPackageContext(String packageName) {
		try {
			Context hostContext = VirtualCore.get().getContext();
			return hostContext.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Object fixBoundApp(AppBindData data) {
		// TODO: Using Native VM Hook to fix the `Camera` and `AudioRecord`.
		Object thread = VirtualCore.mainThread();
		Object boundApp = mirror.android.app.ActivityThread.mBoundApplication.get(thread);
		mirror.android.app.ActivityThread.AppBindData.appInfo.set(boundApp, data.appInfo);
		mirror.android.app.ActivityThread.AppBindData.processName.set(boundApp, data.processName);
		mirror.android.app.ActivityThread.AppBindData.instrumentationName.set(boundApp, new ComponentName(data.appInfo.packageName, Instrumentation.class.getName()));
		return boundApp;
	}

	private void installContentProviders(Context app, List<ProviderInfo> providers) {
		long origId = Binder.clearCallingIdentity();
		Object mainThread = VirtualCore.mainThread();
		try {
			for (ProviderInfo cpi : providers) {
				if (cpi.enabled) {
					ActivityThread.installProvider(mainThread, app, cpi, null);
				}
			}
		} finally {
			Binder.restoreCallingIdentity(origId);
		}
	}


	@Override
	public IBinder acquireProviderClient(ProviderInfo info) {
		if (mTempLock != null) {
			mTempLock.block();
		}
		if (!VClientImpl.getClient().isBound()) {
			VClientImpl.getClient().bindApplication(info.packageName, info.processName);
		}
		IInterface provider = null;
		String[] authorities = info.authority.split(";");
		String authority = authorities.length == 0 ? info.authority : authorities[0];
		ContentResolver resolver = VirtualCore.get().getContext().getContentResolver();
		ContentProviderClient client = null;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				client = resolver.acquireUnstableContentProviderClient(authority);
			} else {
				client = resolver.acquireContentProviderClient(authority);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (client != null) {
			provider = mirror.android.content.ContentProviderClient.mContentProvider.get(client);
			client.release();
		}
		return provider != null ? provider.asBinder() : null;
	}

	private void fixInstalledProviders() {
		Map clientMap = ActivityThread.mProviderMap.get(VirtualCore.mainThread());
		for (Object clientRecord : clientMap.values()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				IInterface provider = ActivityThread.ProviderClientRecordJB.mProvider.get(clientRecord);
				Object holder = ActivityThread.ProviderClientRecordJB.mHolder.get(clientRecord);
				ProviderInfo info = IActivityManager.ContentProviderHolder.info.get(holder);
				if (holder != null && !info.authority.startsWith(StubManifest.STUB_CP_AUTHORITY)) {
					provider = ProviderHook.createProxy(true, info.authority, provider);
					ActivityThread.ProviderClientRecordJB.mProvider.set(clientRecord, provider);
					IActivityManager.ContentProviderHolder.provider.set(holder, provider);
				}
			} else {
				String authority = ActivityThread.ProviderClientRecord.mName.get(clientRecord);
				IInterface provider = ActivityThread.ProviderClientRecord.mProvider.get(clientRecord);
				if (provider != null && !authority.startsWith(StubManifest.STUB_CP_AUTHORITY)) {
					provider = ProviderHook.createProxy(true, authority, provider);
					ActivityThread.ProviderClientRecord.mProvider.set(clientRecord, provider);
				}
			}
		}

	}


	@Override
	public void finishActivity(IBinder token) {
		VActivityManager.get().finishActivity(token);
	}

	@Override
	public void scheduleNewIntent(String creator, IBinder token, Intent intent) {
		NewIntentData data = new NewIntentData();
		data.creator = creator;
		data.token = token;
		data.intent = intent;
		sendMessage(NEW_INTENT, data);
	}

	@Override
	public IBinder createProxyService(ComponentName component, IBinder binder) {
		return ProxyServiceFactory.getProxyService(getCurrentApplication(), component, binder);
	}

	@Override
	public String getDebugInfo() {
		return "process : " + VirtualRuntime.getProcessName() + "\n" +
				"initialPkg : " + VirtualRuntime.getInitialPackageName() + "\n" +
				"vuid : " + vuid;
	}
}
