package com.lody.virtual.client;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
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
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.system.ErrnoException;
import android.system.Os;

import com.lody.virtual.client.core.CrashHandler;
import com.lody.virtual.client.core.InvocationStubManager;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.fixer.ContextFixer;
import com.lody.virtual.client.hook.delegate.AppInstrumentation;
import com.lody.virtual.client.hook.providers.ProviderHook;
import com.lody.virtual.client.hook.proxies.am.HCallbackStub;
import com.lody.virtual.client.hook.secondary.ProxyServiceFactory;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VDeviceManager;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.client.ipc.VirtualStorageManager;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.compat.BuildCompat;
import com.lody.virtual.helper.compat.StorageManagerCompat;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.InstalledAppInfo;
import com.lody.virtual.remote.PendingResultData;
import com.lody.virtual.remote.VDeviceInfo;
import com.lody.virtual.server.interfaces.IUiCallback;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.weishu.exposed.ExposedBridge;
import mirror.android.app.ActivityThread;
import mirror.android.app.ActivityThreadNMR1;
import mirror.android.app.ContextImpl;
import mirror.android.app.IActivityManager;
import mirror.android.app.LoadedApk;
import mirror.android.content.ContentProviderHolderOreo;
import mirror.android.providers.Settings;
import mirror.android.renderscript.RenderScriptCacheDir;
import mirror.android.view.HardwareRenderer;
import mirror.android.view.RenderScript;
import mirror.android.view.ThreadedRenderer;
import mirror.com.android.internal.content.ReferrerIntent;
import mirror.dalvik.system.VMRuntime;
import mirror.java.lang.ThreadGroupN;

import static com.lody.virtual.os.VUserHandle.getUserId;

/**
 * @author Lody
 */

public final class VClientImpl extends IVClient.Stub {

    private static final int NEW_INTENT = 11;
    private static final int RECEIVER = 12;

    private static final String TAG = VClientImpl.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static final VClientImpl gClient = new VClientImpl();
    private final H mH = new H();
    private ConditionVariable mTempLock;
    private Instrumentation mInstrumentation = AppInstrumentation.getDefault();
    private IBinder token;
    private int vuid;
    private VDeviceInfo deviceInfo;
    private AppBindData mBoundApplication;
    private Application mInitialApplication;
    private CrashHandler crashHandler;
    private IUiCallback mUiCallback;

    public static VClientImpl get() {
        return gClient;
    }

    public boolean isBound() {
        return mBoundApplication != null;
    }

    public VDeviceInfo getDeviceInfo() {
        if (deviceInfo == null) {
            synchronized (this) {
                if (deviceInfo == null) {
                    deviceInfo = VDeviceManager.get().getDeviceInfo(getUserId(vuid));
                }
            }
        }
        return deviceInfo;
    }

    public Application getCurrentApplication() {
        return mInitialApplication;
    }

    public String getCurrentPackage() {
        return mBoundApplication != null ?
                mBoundApplication.appInfo.packageName : VPackageManager.get().getNameForUid(getVUid());
    }

    public ApplicationInfo getCurrentApplicationInfo() {
        return mBoundApplication != null ? mBoundApplication.appInfo : null;
    }

    public CrashHandler getCrashHandler() {
        return crashHandler;
    }

    public void setCrashHandler(CrashHandler crashHandler) {
        this.crashHandler = crashHandler;
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

    public ClassLoader getClassLoader(String packageName) {
        Context context = createPackageContext(packageName);
        return context.getClassLoader();
    }

    private void sendMessage(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        mH.sendMessage(msg);
    }

    @Override
    public IBinder getAppThread() {
        return ActivityThread.getApplicationThread.call(VirtualCore.mainThread());
    }

    @Override
    public IBinder getToken() {
        return token;
    }

    public void initProcess(IBinder token, int vuid) {
        this.token = token;
        this.vuid = vuid;
    }

    private void handleNewIntent(NewIntentData data) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent = ReferrerIntent.ctor.newInstance(data.intent, data.creator);
        } else {
            intent = data.intent;
        }
        if (ActivityThread.performNewIntents != null) {
            ActivityThread.performNewIntents.call(
                    VirtualCore.mainThread(),
                    data.token,
                    Collections.singletonList(intent)
            );
        } else {
            ActivityThreadNMR1.performNewIntents.call(
                    VirtualCore.mainThread(),
                    data.token,
                    Collections.singletonList(intent),
                    true);
        }
    }

    public void bindApplicationForActivity(final String packageName, final String processName, final Intent intent) {
        mUiCallback = VirtualCore.getUiCallback(intent);
        bindApplication(packageName, processName);
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
        VDeviceInfo deviceInfo = getDeviceInfo();
        if (processName == null) {
            processName = packageName;
        }
        mTempLock = lock;
        try {
            setupUncaughtHandler();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            fixInstalledProviders();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        mirror.android.os.Build.SERIAL.set(deviceInfo.serial);
        mirror.android.os.Build.DEVICE.set(Build.DEVICE.replace(" ", "_"));
        ActivityThread.mInitialApplication.set(
                VirtualCore.mainThread(),
                null
        );
        AppBindData data = new AppBindData();
        InstalledAppInfo info = VirtualCore.get().getInstalledAppInfo(packageName, 0);
        if (info == null) {
            new Exception("App not exist!").printStackTrace();
            Process.killProcess(0);
            System.exit(0);
        }
        data.appInfo = VPackageManager.get().getApplicationInfo(packageName, 0, getUserId(vuid));
        data.processName = processName;
        data.appInfo.processName = processName;
        data.providers = VPackageManager.get().queryContentProviders(processName, getVUid(), PackageManager.GET_META_DATA);
        VLog.i(TAG, String.format("Binding application %s, (%s)", data.appInfo.packageName, data.processName));
        mBoundApplication = data;
        VirtualRuntime.setupRuntime(data.processName, data.appInfo);
        int targetSdkVersion = data.appInfo.targetSdkVersion;
        if (targetSdkVersion < Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.ThreadPolicy newPolicy = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy()).permitNetwork().build();
            StrictMode.setThreadPolicy(newPolicy);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && targetSdkVersion < Build.VERSION_CODES.LOLLIPOP) {
            mirror.android.os.Message.updateCheckRecycle.call(targetSdkVersion);
        }
        if (VASettings.ENABLE_IO_REDIRECT) {
            startIOUniformer();
        }
        NativeEngine.launchEngine();
        Object mainThread = VirtualCore.mainThread();
        NativeEngine.startDexOverride();
        Context context = createPackageContext(data.appInfo.packageName);
        try {
            // anti-virus, fuck ESET-NOD32: a variant of Android/AdDisplay.AdLock.AL potentially unwanted
            // we can make direct call... use reflect to bypass.
            // System.setProperty("java.io.tmpdir", context.getCacheDir().getAbsolutePath());
            System.class.getDeclaredMethod("setProperty", String.class, String.class)
                    .invoke(null, "java.io.tmpdir", context.getCacheDir().getAbsolutePath());
        } catch (Throwable ignored) {
            VLog.e(TAG, "set tmp dir error:", ignored);
        }

        File codeCacheDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            codeCacheDir = context.getCodeCacheDir();
        } else {
            codeCacheDir = context.getCacheDir();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (HardwareRenderer.setupDiskCache != null) {
                HardwareRenderer.setupDiskCache.call(codeCacheDir);
            }
        } else {
            if (ThreadedRenderer.setupDiskCache != null) {
                ThreadedRenderer.setupDiskCache.call(codeCacheDir);
            }
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
        Object boundApp = fixBoundApp(mBoundApplication);
        mBoundApplication.info = ContextImpl.mPackageInfo.get(context);
        mirror.android.app.ActivityThread.AppBindData.info.set(boundApp, data.info);
        VMRuntime.setTargetSdkVersion.call(VMRuntime.getRuntime.call(), data.appInfo.targetSdkVersion);

        boolean conflict = SpecialComponentList.isConflictingInstrumentation(packageName);
        if (!conflict) {
            InvocationStubManager.getInstance().checkEnv(AppInstrumentation.class);
        }

        ApplicationInfo applicationInfo = LoadedApk.mApplicationInfo.get(data.info);
        if (Build.VERSION.SDK_INT >= 26 && applicationInfo.splitNames == null) {
            applicationInfo.splitNames = new String[1];
        }


        boolean enableXposed = VirtualCore.get().isXposedEnabled();
        if (enableXposed) {
            VLog.i(TAG, "Xposed is enabled.");
            ClassLoader originClassLoader = context.getClassLoader();
            ExposedBridge.initOnce(context, data.appInfo, originClassLoader);
            List<InstalledAppInfo> modules = VirtualCore.get().getInstalledApps(0);
            for (InstalledAppInfo module : modules) {
                ExposedBridge.loadModule(module.apkPath, module.getOdexFile().getParent(), module.libPath,
                        data.appInfo, originClassLoader);
            }
        } else {
            VLog.w(TAG, "Xposed is disable..");
        }

        mInitialApplication = LoadedApk.makeApplication.call(data.info, false, null);

        // ExposedBridge.patchAppClassLoader(context);

        mirror.android.app.ActivityThread.mInitialApplication.set(mainThread, mInitialApplication);
        ContextFixer.fixContext(mInitialApplication);
        if (Build.VERSION.SDK_INT >= 24 && "com.tencent.mm:recovery".equals(processName)) {
            fixWeChatRecovery(mInitialApplication);
        }
        if (data.providers != null) {
            installContentProviders(mInitialApplication, data.providers);
        }
        if (lock != null) {
            lock.open();
            mTempLock = null;
        }
        VirtualCore.get().getComponentDelegate().beforeApplicationCreate(mInitialApplication);
        try {
            mInstrumentation.callApplicationOnCreate(mInitialApplication);
            InvocationStubManager.getInstance().checkEnv(HCallbackStub.class);
            if (conflict) {
                InvocationStubManager.getInstance().checkEnv(AppInstrumentation.class);
            }
            Application createdApp = ActivityThread.mInitialApplication.get(mainThread);
            if (createdApp != null) {
                mInitialApplication = createdApp;
            }
        } catch (Exception e) {
            if (!mInstrumentation.onException(mInitialApplication, e)) {
                // 1. tell ui that do not need wait use now.
                if (mUiCallback != null) {
                    try {
                        mUiCallback.onOpenFailed(packageName, VUserHandle.myUserId());
                    } catch (RemoteException ignored) {
                    }
                }
                // 2. tell vams that launch finish.
                VActivityManager.get().appDoneExecuting();

                // 3. rethrow
                throw new RuntimeException(
                        "Unable to create application " + (mInitialApplication == null ? " [null application] " : mInitialApplication.getClass().getName())
                                + ": " + e.toString(), e);
            }
        }
        VActivityManager.get().appDoneExecuting();
        VirtualCore.get().getComponentDelegate().afterApplicationCreate(mInitialApplication);
    }

    private void fixWeChatRecovery(Application app) {
        try {
            Field field = app.getClassLoader().loadClass("com.tencent.recovery.Recovery").getField("context");
            field.setAccessible(true);
            if (field.get(null) != null) {
                return;
            }
            field.set(null, app.getBaseContext());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void setupUncaughtHandler() {
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }
        ThreadGroup newRoot = new RootThreadGroup(root);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            final List<ThreadGroup> groups = mirror.java.lang.ThreadGroup.groups.get(root);
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (groups) {
                List<ThreadGroup> newGroups = new ArrayList<>(groups);
                newGroups.remove(newRoot);
                mirror.java.lang.ThreadGroup.groups.set(newRoot, newGroups);
                groups.clear();
                groups.add(newRoot);
                mirror.java.lang.ThreadGroup.groups.set(root, groups);
                for (ThreadGroup group : newGroups) {
                    if (group == newRoot) {
                        continue;
                    }
                    mirror.java.lang.ThreadGroup.parent.set(group, newRoot);
                }
            }
        } else {
            final ThreadGroup[] groups = ThreadGroupN.groups.get(root);
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (groups) {
                ThreadGroup[] newGroups = groups.clone();
                ThreadGroupN.groups.set(newRoot, newGroups);
                ThreadGroupN.groups.set(root, new ThreadGroup[]{newRoot});
                for (Object group : newGroups) {
                    if (group == newRoot) {
                        continue;
                    }
                    ThreadGroupN.parent.set(group, newRoot);
                }
                ThreadGroupN.ngroups.set(root, 1);
            }
        }
    }

    @SuppressLint("SdCardPath")
    private void startIOUniformer() {
        ApplicationInfo info = mBoundApplication.appInfo;
        int userId = VUserHandle.myUserId();
        String wifiMacAddressFile = deviceInfo.getWifiFile(userId).getPath();
        NativeEngine.redirectDirectory("/sys/class/net/wlan0/address", wifiMacAddressFile);
        NativeEngine.redirectDirectory("/sys/class/net/eth0/address", wifiMacAddressFile);
        NativeEngine.redirectDirectory("/sys/class/net/wifi/address", wifiMacAddressFile);

        NativeEngine.redirectDirectory("/data/data/" + info.packageName, info.dataDir);
        NativeEngine.redirectDirectory("/data/user/0/" + info.packageName, info.dataDir);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NativeEngine.redirectDirectory("/data/user_de/0/" + info.packageName, info.dataDir);
        }
        String libPath = VEnvironment.getAppLibDirectory(info.packageName).getAbsolutePath();
        String userLibPath = new File(VEnvironment.getUserSystemDirectory(userId), info.packageName + "/lib").getAbsolutePath();
        NativeEngine.redirectDirectory(userLibPath, libPath);
        NativeEngine.redirectDirectory("/data/data/" + info.packageName + "/lib/", libPath);
        NativeEngine.redirectDirectory("/data/user/0/" + info.packageName + "/lib/", libPath);

        File dataUserLib = new File(VEnvironment.getDataUserPackageDirectory(userId, info.packageName), "lib");
        if (!dataUserLib.exists()) {
            try {
                Os.symlink(libPath, dataUserLib.getPath());
            } catch (ErrnoException e) {
                VLog.w(TAG, "symlink error", e);
            }
        }

        setupVirtualStorage(info, userId);

        NativeEngine.enableIORedirect();
    }

    private void setupVirtualStorage(ApplicationInfo info, int userId) {
        VirtualStorageManager vsManager = VirtualStorageManager.get();
        boolean enable = vsManager.isVirtualStorageEnable(info.packageName, userId);
        if (!enable) {
            // There are lots of situation to deal, I am tired, disable it now.
            // such as: FileProvider.
            return;
        }

        File vsDir = VEnvironment.getVirtualStorageDir(info.packageName, userId);
        if (vsDir == null || !vsDir.exists() || !vsDir.isDirectory()) {
            return;
        }

        HashSet<String> storageRoots = getMountPoints();
        storageRoots.add(Environment.getExternalStorageDirectory().getAbsolutePath());

        Set<String> whiteList = new HashSet<>();
        whiteList.add(Environment.DIRECTORY_PODCASTS);
        whiteList.add(Environment.DIRECTORY_RINGTONES);
        whiteList.add(Environment.DIRECTORY_ALARMS);
        whiteList.add(Environment.DIRECTORY_NOTIFICATIONS);
        whiteList.add(Environment.DIRECTORY_PICTURES);
        whiteList.add(Environment.DIRECTORY_MOVIES);
        whiteList.add(Environment.DIRECTORY_DOWNLOADS);
        whiteList.add(Environment.DIRECTORY_DCIM);
        whiteList.add("Android/obb");
        if (Build.VERSION.SDK_INT >= 19) {
            whiteList.add(Environment.DIRECTORY_DOCUMENTS);
        }

        // ensure virtual storage white directory exists.
        for (String whiteDir : whiteList) {
            File originalDir = new File(Environment.getExternalStorageDirectory(), whiteDir);
            File virtualDir = new File(vsDir, whiteDir);
            if (!originalDir.exists()) {
                continue;
            }
            //noinspection ResultOfMethodCallIgnored
            virtualDir.mkdirs();
        }

        String vsPath = vsDir.getAbsolutePath();
        NativeEngine.whitelist(vsPath, true);
        String privatePath = VEnvironment.getVirtualPrivateStorageDir(userId).getAbsolutePath();
        NativeEngine.whitelist(privatePath, true);

        for (String storageRoot : storageRoots) {
            for (String whiteDir : whiteList) {
                // white list, do not redirect
                String whitePath = new File(storageRoot, whiteDir).getAbsolutePath();
                NativeEngine.whitelist(whitePath, true);
            }

            // redirect xxx/Android/data/ -> /xxx/Android/data/<host>/virtual/<user>
            NativeEngine.redirectDirectory(new File(storageRoot, "Android/data/").getAbsolutePath(), privatePath);
            // redirect /sdcard/ -> vsdcard
            NativeEngine.redirectDirectory(storageRoot, vsPath);
        }
    }

    @SuppressLint("SdCardPath")
    private HashSet<String> getMountPoints() {
        HashSet<String> mountPoints = new HashSet<>(3);
        mountPoints.add("/mnt/sdcard/");
        mountPoints.add("/sdcard/");
        String[] points = StorageManagerCompat.getAllPoints(VirtualCore.get().getContext());
        if (points != null) {
            Collections.addAll(mountPoints, points);
        }
        return mountPoints;

    }

    private Context createPackageContext(String packageName) {
        try {
            Context hostContext = VirtualCore.get().getContext();
            return hostContext.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            VirtualRuntime.crash(new RemoteException());
        }
        throw new RuntimeException();
    }

    private Object fixBoundApp(AppBindData data) {
        Object thread = VirtualCore.mainThread();
        Object boundApp = mirror.android.app.ActivityThread.mBoundApplication.get(thread);
        mirror.android.app.ActivityThread.AppBindData.appInfo.set(boundApp, data.appInfo);
        mirror.android.app.ActivityThread.AppBindData.processName.set(boundApp, data.processName);
        mirror.android.app.ActivityThread.AppBindData.instrumentationName.set(
                boundApp,
                new ComponentName(data.appInfo.packageName, Instrumentation.class.getName())
        );
        ActivityThread.AppBindData.providers.set(boundApp, data.providers);
        return boundApp;
    }

    private void installContentProviders(Context app, List<ProviderInfo> providers) {
        long origId = Binder.clearCallingIdentity();
        Object mainThread = VirtualCore.mainThread();
        try {
            for (ProviderInfo cpi : providers) {
                try {
                    ActivityThread.installProvider(mainThread, app, cpi, null);
                } catch (Throwable e) {
                    e.printStackTrace();
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
        if (!isBound()) {
            VClientImpl.get().bindApplication(info.packageName, info.processName);
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
            VLog.e(TAG, "", e);
        }
        if (client != null) {
            provider = mirror.android.content.ContentProviderClient.mContentProvider.get(client);
            client.release();
        }
        return provider != null ? provider.asBinder() : null;
    }

    private void fixInstalledProviders() {
        clearSettingProvider();
        Map clientMap = ActivityThread.mProviderMap.get(VirtualCore.mainThread());
        for (Object clientRecord : clientMap.values()) {
            if (BuildCompat.isOreo()) {
                IInterface provider = ActivityThread.ProviderClientRecordJB.mProvider.get(clientRecord);
                Object holder = ActivityThread.ProviderClientRecordJB.mHolder.get(clientRecord);
                if (holder == null) {
                    continue;
                }
                ProviderInfo info = ContentProviderHolderOreo.info.get(holder);
                if (!info.authority.startsWith(VASettings.STUB_CP_AUTHORITY)) {
                    provider = ProviderHook.createProxy(true, info.authority, provider);
                    ActivityThread.ProviderClientRecordJB.mProvider.set(clientRecord, provider);
                    ContentProviderHolderOreo.provider.set(holder, provider);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                IInterface provider = ActivityThread.ProviderClientRecordJB.mProvider.get(clientRecord);
                Object holder = ActivityThread.ProviderClientRecordJB.mHolder.get(clientRecord);
                if (holder == null) {
                    continue;
                }
                ProviderInfo info = IActivityManager.ContentProviderHolder.info.get(holder);
                if (!info.authority.startsWith(VASettings.STUB_CP_AUTHORITY)) {
                    provider = ProviderHook.createProxy(true, info.authority, provider);
                    ActivityThread.ProviderClientRecordJB.mProvider.set(clientRecord, provider);
                    IActivityManager.ContentProviderHolder.provider.set(holder, provider);
                }
            } else {
                String authority = ActivityThread.ProviderClientRecord.mName.get(clientRecord);
                IInterface provider = ActivityThread.ProviderClientRecord.mProvider.get(clientRecord);
                if (provider != null && !authority.startsWith(VASettings.STUB_CP_AUTHORITY)) {
                    provider = ProviderHook.createProxy(true, authority, provider);
                    ActivityThread.ProviderClientRecord.mProvider.set(clientRecord, provider);
                }
            }
        }

    }

    private void clearSettingProvider() {
        Object cache;
        cache = Settings.System.sNameValueCache.get();
        if (cache != null) {
            clearContentProvider(cache);
        }
        cache = Settings.Secure.sNameValueCache.get();
        if (cache != null) {
            clearContentProvider(cache);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Settings.Global.TYPE != null) {
            cache = Settings.Global.sNameValueCache.get();
            if (cache != null) {
                clearContentProvider(cache);
            }
        }
    }

    private static void clearContentProvider(Object cache) {
        if (BuildCompat.isOreo()) {
            Object holder = Settings.NameValueCacheOreo.mProviderHolder.get(cache);
            if (holder != null) {
                Settings.ContentProviderHolder.mContentProvider.set(holder, null);
            }
        } else {
            Settings.NameValueCache.mContentProvider.set(cache, null);
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
    public void scheduleReceiver(String processName, ComponentName component, Intent intent, PendingResultData resultData) {
        ReceiverData receiverData = new ReceiverData();
        receiverData.resultData = resultData;
        receiverData.intent = intent;
        receiverData.component = component;
        receiverData.processName = processName;
        sendMessage(RECEIVER, receiverData);
    }

    private void handleReceiver(ReceiverData data) {
        BroadcastReceiver.PendingResult result = data.resultData.build();
        try {
            if (!isBound()) {
                bindApplication(data.component.getPackageName(), data.processName);
            }
            Context context = mInitialApplication.getBaseContext();
            Context receiverContext = ContextImpl.getReceiverRestrictedContext.call(context);
            String className = data.component.getClassName();
            BroadcastReceiver receiver = (BroadcastReceiver) context.getClassLoader().loadClass(className).newInstance();
            mirror.android.content.BroadcastReceiver.setPendingResult.call(receiver, result);
            data.intent.setExtrasClassLoader(context.getClassLoader());
            if (data.intent.getComponent() == null) {
                data.intent.setComponent(data.component);
            }
            receiver.onReceive(receiverContext, data.intent);
            if (mirror.android.content.BroadcastReceiver.getPendingResult.call(receiver) != null) {
                result.finish();
            }
        } catch (Exception e) {
            // must be this for misjudge of anti-virus!!
            throw new RuntimeException(String.format("Unable to start receiver: %s ", data.component), e);
        }
        VActivityManager.get().broadcastFinish(data.resultData);
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

    private static class RootThreadGroup extends ThreadGroup {

        RootThreadGroup(ThreadGroup parent) {
            super(parent, "VA-Root");
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            CrashHandler handler = VClientImpl.gClient.crashHandler;
            if (handler != null) {
                handler.handleUncaughtException(t, e);
            } else {
                VLog.e("uncaught", e);
                System.exit(0);
            }
        }
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

    private final class ReceiverData {
        PendingResultData resultData;
        Intent intent;
        ComponentName component;
        String processName;
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
                }
                break;
                case RECEIVER: {
                    handleReceiver((ReceiverData) msg.obj);
                }
            }
        }
    }
}
