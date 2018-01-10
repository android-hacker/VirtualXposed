package com.lody.virtual.server.am;

import android.app.ActivityManager;
import android.app.IServiceConnection;
import android.app.IStopUserCallback;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;

import com.lody.virtual.client.IVClient;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.client.ipc.ProviderCall;
import com.lody.virtual.client.ipc.VNotificationManager;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.collection.ArrayMap;
import com.lody.virtual.helper.collection.SparseArray;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.helper.compat.ApplicationThreadCompat;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.compat.IApplicationThreadCompat;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VBinder;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.AppTaskInfo;
import com.lody.virtual.remote.BadgerInfo;
import com.lody.virtual.remote.PendingIntentData;
import com.lody.virtual.remote.PendingResultData;
import com.lody.virtual.remote.VParceledListSlice;
import com.lody.virtual.server.interfaces.IActivityManager;
import com.lody.virtual.server.interfaces.IProcessObserver;
import com.lody.virtual.server.pm.PackageCacheManager;
import com.lody.virtual.server.pm.PackageSetting;
import com.lody.virtual.server.pm.VAppManagerService;
import com.lody.virtual.server.pm.VPackageManagerService;
import com.lody.virtual.server.secondary.BinderDelegateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import mirror.android.app.IServiceConnectionO;

import static android.os.Process.killProcess;
import static com.lody.virtual.os.VBinder.getCallingPid;
import static com.lody.virtual.os.VUserHandle.getUserId;

/**
 * @author Lody
 */
public class VActivityManagerService implements IActivityManager {

    private static final boolean BROADCAST_NOT_STARTED_PKG = false;

    private static final AtomicReference<VActivityManagerService> sService = new AtomicReference<>();
    private static final String TAG = VActivityManagerService.class.getSimpleName();
    private final SparseArray<ProcessRecord> mPidsSelfLocked = new SparseArray<ProcessRecord>();
    private final ActivityStack mMainStack = new ActivityStack(this);
    private final Set<ServiceRecord> mHistory = new HashSet<ServiceRecord>();
    private final ProcessMap<ProcessRecord> mProcessNames = new ProcessMap<ProcessRecord>();
    private final PendingIntents mPendingIntents = new PendingIntents();
    private ActivityManager am = (ActivityManager) VirtualCore.get().getContext()
            .getSystemService(Context.ACTIVITY_SERVICE);
    private NotificationManager nm = (NotificationManager) VirtualCore.get().getContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);

    public static VActivityManagerService get() {
        return sService.get();
    }

    public static void systemReady(Context context) {
        new VActivityManagerService().onCreate(context);
    }

    private static ServiceInfo resolveServiceInfo(Intent service, int userId) {
        if (service != null) {
            ServiceInfo serviceInfo = VirtualCore.get().resolveServiceInfo(service, userId);
            if (serviceInfo != null) {
                return serviceInfo;
            }
        }
        return null;
    }

    public void onCreate(Context context) {
        AttributeCache.init(context);
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES | PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo == null) {
            throw new RuntimeException("Unable to found PackageInfo : " + context.getPackageName());
        }
        sService.set(this);

    }


    @Override
    public int startActivity(Intent intent, ActivityInfo info, IBinder resultTo, Bundle options, String resultWho, int requestCode, int userId) {
        synchronized (this) {
            return mMainStack.startActivityLocked(userId, intent, info, resultTo, options, resultWho, requestCode);
        }
    }

    @Override
    public int startActivities(Intent[] intents, String[] resolvedTypes, IBinder token, Bundle options, int userId) {
        synchronized (this) {
            ActivityInfo[] infos = new ActivityInfo[intents.length];
            for (int i = 0; i < intents.length; i++) {
                ActivityInfo ai = VirtualCore.get().resolveActivityInfo(intents[i], userId);
                if (ai == null) {
                    return ActivityManagerCompat.START_INTENT_NOT_RESOLVED;
                }
                infos[i] = ai;

            }
            return mMainStack.startActivitiesLocked(userId, intents, infos, resolvedTypes, token, options);
        }
    }

    @Override
    public String getPackageForIntentSender(IBinder binder) {
        PendingIntentData data = mPendingIntents.getPendingIntent(binder);
        if (data != null) {
            return data.creator;
        }
        return null;
    }


    @Override
    public PendingIntentData getPendingIntent(IBinder binder) {
        return mPendingIntents.getPendingIntent(binder);
    }

    @Override
    public void addPendingIntent(IBinder binder, String creator) {
        mPendingIntents.addPendingIntent(binder, creator);
    }

    @Override
    public void removePendingIntent(IBinder binder) {
        mPendingIntents.removePendingIntent(binder);
    }

    @Override
    public int getSystemPid() {
        return VirtualCore.get().myUid();
    }

    @Override
    public void onActivityCreated(ComponentName component, ComponentName caller, IBinder token, Intent intent, String affinity, int taskId, int launchMode, int flags) {
        int pid = Binder.getCallingPid();
        ProcessRecord targetApp = findProcessLocked(pid);
        if (targetApp != null) {
            mMainStack.onActivityCreated(targetApp, component, caller, token, intent, affinity, taskId, launchMode, flags);
        }
    }

    @Override
    public void onActivityResumed(int userId, IBinder token) {
        mMainStack.onActivityResumed(userId, token);
    }

    @Override
    public boolean onActivityDestroyed(int userId, IBinder token) {
        ActivityRecord r = mMainStack.onActivityDestroyed(userId, token);
        return r != null;
    }

    @Override
    public AppTaskInfo getTaskInfo(int taskId) {
        return mMainStack.getTaskInfo(taskId);
    }

    @Override
    public String getPackageForToken(int userId, IBinder token) {
        return mMainStack.getPackageForToken(userId, token);
    }

    @Override
    public ComponentName getActivityClassForToken(int userId, IBinder token) {
        return mMainStack.getActivityClassForToken(userId, token);
    }


    private void processDead(ProcessRecord record) {
        synchronized (mHistory) {
            Iterator<ServiceRecord> iterator = mHistory.iterator();
            while (iterator.hasNext()) {
                ServiceRecord r = iterator.next();
                if (r.process != null && r.process.pid == record.pid) {
                    iterator.remove();
                }
            }
            mMainStack.processDied(record);
        }
    }


    @Override
    public IBinder acquireProviderClient(int userId, ProviderInfo info) {
        ProcessRecord callerApp;
        synchronized (mPidsSelfLocked) {
            callerApp = findProcessLocked(getCallingPid());
        }
        if (callerApp == null) {
            throw new SecurityException("Who are you?");
        }
        String processName = info.processName;
        ProcessRecord r;
        synchronized (this) {
            r = startProcessIfNeedLocked(processName, userId, info.packageName);
        }
        if (r != null && r.client.asBinder().isBinderAlive()) {
            try {
                return r.client.acquireProviderClient(info);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public ComponentName getCallingActivity(int userId, IBinder token) {
        return mMainStack.getCallingActivity(userId, token);
    }

    @Override
    public String getCallingPackage(int userId, IBinder token) {
        return mMainStack.getCallingPackage(userId, token);
    }

    private void addRecord(ServiceRecord r) {
        mHistory.add(r);
    }

    private ServiceRecord findRecordLocked(int userId, ServiceInfo serviceInfo) {
        synchronized (mHistory) {
            for (ServiceRecord r : mHistory) {
                // If service is not created, and bindService with the flag that is
                // not BIND_AUTO_CREATE, r.process is null
                if ((r.process == null || r.process.userId == userId)
                        && ComponentUtils.isSameComponent(serviceInfo, r.serviceInfo)) {
                    return r;
                }
            }
            return null;
        }
    }

    private ServiceRecord findRecordLocked(IServiceConnection connection) {
        synchronized (mHistory) {
            for (ServiceRecord r : mHistory) {
                if (r.containConnection(connection)) {
                    return r;
                }
            }
            return null;
        }
    }


    @Override
    public ComponentName startService(IBinder caller, Intent service, String resolvedType, int userId) {
        synchronized (this) {
            return startServiceCommon(service, true, userId);
        }
    }

    private ComponentName startServiceCommon(Intent service,
                                             boolean scheduleServiceArgs, int userId) {
        ServiceInfo serviceInfo = resolveServiceInfo(service, userId);
        if (serviceInfo == null) {
            return null;
        }
        ProcessRecord targetApp = startProcessIfNeedLocked(ComponentUtils.getProcessName(serviceInfo),
                userId,
                serviceInfo.packageName);

        if (targetApp == null) {
            VLog.e(TAG, "Unable to start new Process for : " + ComponentUtils.toComponentName(serviceInfo));
            return null;
        }
        IInterface appThread = targetApp.appThread;
        ServiceRecord r = findRecordLocked(userId, serviceInfo);
        if (r == null) {
            r = new ServiceRecord();
            r.startId = 0;
            r.activeSince = SystemClock.elapsedRealtime();
            r.process = targetApp;
            r.serviceInfo = serviceInfo;
            try {
                IApplicationThreadCompat.scheduleCreateService(appThread, r, r.serviceInfo, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            addRecord(r);
        }
        r.lastActivityTime = SystemClock.uptimeMillis();
        if (scheduleServiceArgs) {
            r.startId++;
            boolean taskRemoved = serviceInfo.applicationInfo != null
                    && serviceInfo.applicationInfo.targetSdkVersion < Build.VERSION_CODES.ECLAIR;
            try {
                IApplicationThreadCompat.scheduleServiceArgs(appThread, r, taskRemoved, r.startId, 0, service);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return ComponentUtils.toComponentName(serviceInfo);
    }

    @Override
    public int stopService(IBinder caller, Intent service, String resolvedType, int userId) {
        synchronized (this) {
            ServiceInfo serviceInfo = resolveServiceInfo(service, userId);
            if (serviceInfo == null) {
                return 0;
            }
            ServiceRecord r = findRecordLocked(userId, serviceInfo);
            if (r == null) {
                return 0;
            }
            stopServiceCommon(r, ComponentUtils.toComponentName(serviceInfo));
            return 1;
        }
    }

    @Override
    public boolean stopServiceToken(ComponentName className, IBinder token, int startId, int userId) {
        synchronized (this) {
            ServiceRecord r = (ServiceRecord) token;
            if (r != null && (r.startId == startId || startId == -1)) {
                stopServiceCommon(r, className);
                return true;
            }

            return false;
        }
    }

    private void stopServiceCommon(ServiceRecord r, ComponentName className) {
        for (ServiceRecord.IntentBindRecord bindRecord : r.bindings) {
            for (IServiceConnection connection : bindRecord.connections) {
                // Report to all of the connections that the service is no longer
                // available.
                try {
                    if (Build.VERSION.SDK_INT >= 26) {
                        IServiceConnectionO.connected.call(connection, className, null, true);
                    } else {
                        connection.connected(className, null);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            try {
                IApplicationThreadCompat.scheduleUnbindService(r.process.appThread, r, bindRecord.intent);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        try {
            IApplicationThreadCompat.scheduleStopService(r.process.appThread, r);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mHistory.remove(r);

    }

    @Override
    public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType,
                           IServiceConnection connection, int flags, int userId) {
        synchronized (this) {
            ServiceInfo serviceInfo = resolveServiceInfo(service, userId);
            if (serviceInfo == null) {
                return 0;
            }
            ServiceRecord r = findRecordLocked(userId, serviceInfo);
            boolean firstLaunch = r == null;
            if (firstLaunch) {
                if ((flags & Context.BIND_AUTO_CREATE) != 0) {
                    startServiceCommon(service, false, userId);
                    r = findRecordLocked(userId, serviceInfo);
                }
            }
            if (r == null) {
                return 0;
            }
            ServiceRecord.IntentBindRecord boundRecord = r.peekBinding(service);

            if (boundRecord != null && boundRecord.binder != null && boundRecord.binder.isBinderAlive()) {
                if (boundRecord.doRebind) {
                    try {
                        IApplicationThreadCompat.scheduleBindService(r.process.appThread, r, service, true, 0);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                ComponentName componentName = new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name);
                connectService(connection, componentName, boundRecord, false);
            } else {
                try {
                    IApplicationThreadCompat.scheduleBindService(r.process.appThread, r, service, false, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            r.lastActivityTime = SystemClock.uptimeMillis();
            r.addToBoundIntent(service, connection);
            return 1;
        }
    }


    @Override
    public boolean unbindService(IServiceConnection connection, int userId) {
        synchronized (this) {
            ServiceRecord r = findRecordLocked(connection);
            if (r == null) {
                return false;
            }

            for (ServiceRecord.IntentBindRecord bindRecord : r.bindings) {
                if (!bindRecord.containConnection(connection)) {
                    continue;
                }
                bindRecord.removeConnection(connection);
                try {
                    IApplicationThreadCompat.scheduleUnbindService(r.process.appThread, r, bindRecord.intent);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            if (r.startId <= 0 && r.getConnectionCount() <= 0) {
                try {
                    IApplicationThreadCompat.scheduleStopService(r.process.appThread, r);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    mHistory.remove(r);
                }
            }
            return true;
        }
    }

    @Override
    public void unbindFinished(IBinder token, Intent service, boolean doRebind, int userId) {
        synchronized (this) {
            ServiceRecord r = (ServiceRecord) token;
            if (r != null) {
                ServiceRecord.IntentBindRecord boundRecord = r.peekBinding(service);
                if (boundRecord != null) {
                    boundRecord.doRebind = doRebind;
                }
            }
        }
    }


    @Override
    public boolean isVAServiceToken(IBinder token) {
        return token instanceof ServiceRecord;
    }


    @Override
    public void serviceDoneExecuting(IBinder token, int type, int startId, int res, int userId) {
        synchronized (this) {
            ServiceRecord r = (ServiceRecord) token;
            if (r == null) {
                return;
            }
            if (ActivityManagerCompat.SERVICE_DONE_EXECUTING_STOP == type) {
                mHistory.remove(r);
            }
        }
    }

    @Override
    public IBinder peekService(Intent service, String resolvedType, int userId) {
        synchronized (this) {
            ServiceInfo serviceInfo = resolveServiceInfo(service, userId);
            if (serviceInfo == null) {
                return null;
            }
            ServiceRecord r = findRecordLocked(userId, serviceInfo);
            if (r != null) {
                ServiceRecord.IntentBindRecord boundRecord = r.peekBinding(service);
                if (boundRecord != null) {
                    return boundRecord.binder;
                }
            }
            return null;
        }
    }

    @Override
    public void publishService(IBinder token, Intent intent, IBinder service, int userId) {
        synchronized (this) {
            ServiceRecord r = (ServiceRecord) token;
            if (r != null) {
                ServiceRecord.IntentBindRecord boundRecord = r.peekBinding(intent);
                if (boundRecord != null) {
                    boundRecord.binder = service;
                    for (IServiceConnection conn : boundRecord.connections) {
                        ComponentName component = ComponentUtils.toComponentName(r.serviceInfo);
                        connectService(conn, component, boundRecord, false);
                    }
                }
            }
        }
    }

    private void connectService(IServiceConnection conn, ComponentName component, ServiceRecord.IntentBindRecord r, boolean dead) {
        try {
            BinderDelegateService delegateService = new BinderDelegateService(component, r.binder);
            if (Build.VERSION.SDK_INT >= 26) {
                IServiceConnectionO.connected.call(conn, component, delegateService, dead);
            } else {
                conn.connected(component, delegateService);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public VParceledListSlice<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags, int userId) {
        synchronized (mHistory) {
            List<ActivityManager.RunningServiceInfo> services = new ArrayList<>(mHistory.size());
            for (ServiceRecord r : mHistory) {
                if (r.process.userId != userId) {
                    continue;
                }
                ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo();
                info.uid = r.process.vuid;
                info.pid = r.process.pid;
                ProcessRecord processRecord = findProcessLocked(r.process.pid);
                if (processRecord != null) {
                    info.process = processRecord.processName;
                    info.clientPackage = processRecord.info.packageName;
                }
                info.activeSince = r.activeSince;
                info.lastActivityTime = r.lastActivityTime;
                info.clientCount = r.getClientCount();
                info.service = ComponentUtils.toComponentName(r.serviceInfo);
                info.started = r.startId > 0;
                services.add(info);
            }
            return new VParceledListSlice<>(services);
        }
    }

    @Override
    public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification,
                                     boolean removeNotification, int userId) {
        ServiceRecord r = (ServiceRecord) token;
        if (r != null) {
            if (id != 0) {
                if (notification == null) {
                    throw new IllegalArgumentException("null notification");
                }
                if (r.foregroundId != id) {
                    if (r.foregroundId != 0) {
                        cancelNotification(userId, r.foregroundId, r.serviceInfo.packageName);
                    }
                    r.foregroundId = id;
                }
                r.foregroundNoti = notification;
                postNotification(userId, id, r.serviceInfo.packageName, notification);
            } else {
                if (removeNotification) {
                    cancelNotification(userId, r.foregroundId, r.serviceInfo.packageName);
                    r.foregroundId = 0;
                    r.foregroundNoti = null;
                }
            }
        }
    }

    private void cancelNotification(int userId, int id, String pkg) {
        id = VNotificationManager.get().dealNotificationId(id, pkg, null, userId);
        String tag = VNotificationManager.get().dealNotificationTag(id, pkg, null, userId);
        nm.cancel(tag, id);
    }

    private void postNotification(int userId, int id, String pkg, Notification notification) {
        id = VNotificationManager.get().dealNotificationId(id, pkg, null, userId);
        String tag = VNotificationManager.get().dealNotificationTag(id, pkg, null, userId);
//        VNotificationManager.get().dealNotification(id, notification, pkg);
        VNotificationManager.get().addNotification(id, tag, pkg, userId);
        try {
            nm.notify(tag, id, notification);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processRestarted(String packageName, String processName, int userId) {
        int callingPid = getCallingPid();
        int appId = VAppManagerService.get().getAppId(packageName);
        int uid = VUserHandle.getUid(userId, appId);
        synchronized (this) {
            ProcessRecord app = findProcessLocked(callingPid);
            if (app == null) {
                ApplicationInfo appInfo = VPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
                appInfo.flags |= ApplicationInfo.FLAG_HAS_CODE;
                String stubProcessName = getProcessName(callingPid);
                int vpid = parseVPid(stubProcessName);
                if (vpid != -1) {
                    performStartProcessLocked(uid, vpid, appInfo, processName);
                }
            }
        }
    }

    private int parseVPid(String stubProcessName) {
        String prefix = VirtualCore.get().getHostPkg() + ":p";
        if (stubProcessName != null && stubProcessName.startsWith(prefix)) {
            try {
                return Integer.parseInt(stubProcessName.substring(prefix.length()));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return -1;
    }


    private String getProcessName(int pid) {
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                return info.processName;
            }
        }
        return null;
    }


    private void attachClient(int pid, final IBinder clientBinder) {
        final IVClient client = IVClient.Stub.asInterface(clientBinder);
        if (client == null) {
            killProcess(pid);
            return;
        }
        IInterface thread = null;
        try {
            thread = ApplicationThreadCompat.asInterface(client.getAppThread());
        } catch (RemoteException e) {
            // process has dead
        }
        if (thread == null) {
            killProcess(pid);
            return;
        }
        ProcessRecord app = null;
        try {
            IBinder token = client.getToken();
            if (token instanceof ProcessRecord) {
                app = (ProcessRecord) token;
            }
        } catch (RemoteException e) {
            // process has dead
        }
        if (app == null) {
            killProcess(pid);
            return;
        }
        try {
            final ProcessRecord record = app;
            clientBinder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    clientBinder.unlinkToDeath(this, 0);
                    onProcessDead(record);
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.client = client;
        app.appThread = thread;
        app.pid = pid;
        synchronized (mProcessNames) {
            mProcessNames.put(app.processName, app.vuid, app);
            mPidsSelfLocked.put(app.pid, app);
        }
    }

    private void onProcessDead(ProcessRecord record) {
        mProcessNames.remove(record.processName, record.vuid);
        mPidsSelfLocked.remove(record.pid);
        processDead(record);
        record.lock.open();
    }

    @Override
    public int getFreeStubCount() {
        return VASettings.STUB_COUNT - mPidsSelfLocked.size();
    }

    @Override
    public int initProcess(String packageName, String processName, int userId) {
        synchronized (this) {
            ProcessRecord r = startProcessIfNeedLocked(processName, userId, packageName);
            return r != null ? r.vpid : -1;
        }
    }

    ProcessRecord startProcessIfNeedLocked(String processName, int userId, String packageName) {
        if (VActivityManagerService.get().getFreeStubCount() < 3) {
            // run GC
            killAllApps();
        }
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        ApplicationInfo info = VPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
        if (ps == null || info == null) {
            return null;
        }
        if (!ps.isLaunched(userId)) {
            sendFirstLaunchBroadcast(ps, userId);
            ps.setLaunched(userId, true);
            VAppManagerService.get().savePersistenceData();
        }
        int uid = VUserHandle.getUid(userId, ps.appId);
        ProcessRecord app = mProcessNames.get(processName, uid);
        if (app != null && app.client.asBinder().isBinderAlive()) {
            return app;
        }
        int vpid = queryFreeStubProcessLocked();
        if (vpid == -1) {
            return null;
        }
        app = performStartProcessLocked(uid, vpid, info, processName);
        if (app != null) {
            app.pkgList.add(info.packageName);
        }
        return app;
    }

    private void sendFirstLaunchBroadcast(PackageSetting ps, int userId) {
        Intent intent = new Intent(Intent.ACTION_PACKAGE_FIRST_LAUNCH, Uri.fromParts("package", ps.packageName, null));
        intent.setPackage(ps.packageName);
        intent.putExtra(Intent.EXTRA_UID, VUserHandle.getUid(ps.appId, userId));
        intent.putExtra("android.intent.extra.user_handle", userId);
        sendBroadcastAsUser(intent, null);
    }


    @Override
    public int getUidByPid(int pid) {
        synchronized (mPidsSelfLocked) {
            ProcessRecord r = findProcessLocked(pid);
            if (r != null) {
                return r.vuid;
            }
        }
        return Process.myUid();
    }

    private ProcessRecord performStartProcessLocked(int vuid, int vpid, ApplicationInfo info, String processName) {
        ProcessRecord app = new ProcessRecord(info, processName, vuid, vpid);
        Bundle extras = new Bundle();
        BundleCompat.putBinder(extras, "_VA_|_binder_", app);
        extras.putInt("_VA_|_vuid_", vuid);
        extras.putString("_VA_|_process_", processName);
        extras.putString("_VA_|_pkg_", info.packageName);
        Bundle res = ProviderCall.call(VASettings.getStubAuthority(vpid), "_VA_|_init_process_", null, extras);
        if (res == null) {
            return null;
        }
        int pid = res.getInt("_VA_|_pid_");
        IBinder clientBinder = BundleCompat.getBinder(res, "_VA_|_client_");
        attachClient(pid, clientBinder);
        return app;
    }

    private int queryFreeStubProcessLocked() {
        for (int vpid = 0; vpid < VASettings.STUB_COUNT; vpid++) {
            int N = mPidsSelfLocked.size();
            boolean using = false;
            while (N-- > 0) {
                ProcessRecord r = mPidsSelfLocked.valueAt(N);
                if (r.vpid == vpid) {
                    using = true;
                    break;
                }
            }
            if (using) {
                continue;
            }
            return vpid;
        }
        return -1;
    }

    @Override
    public boolean isAppProcess(String processName) {
        return parseVPid(processName) != -1;
    }

    @Override
    public boolean isAppPid(int pid) {
        synchronized (mPidsSelfLocked) {
            return findProcessLocked(pid) != null;
        }
    }

    @Override
    public String getAppProcessName(int pid) {
        synchronized (mPidsSelfLocked) {
            ProcessRecord r = mPidsSelfLocked.get(pid);
            if (r != null) {
                return r.processName;
            }
        }
        return null;
    }

    @Override
    public List<String> getProcessPkgList(int pid) {
        synchronized (mPidsSelfLocked) {
            ProcessRecord r = mPidsSelfLocked.get(pid);
            if (r != null) {
                return new ArrayList<>(r.pkgList);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void killAllApps() {
        synchronized (mPidsSelfLocked) {
            for (int i = 0; i < mPidsSelfLocked.size(); i++) {
                ProcessRecord r = mPidsSelfLocked.valueAt(i);
                killProcess(r.pid);
            }
        }
    }

    @Override
    public void killAppByPkg(final String pkg, int userId) {
        synchronized (mProcessNames) {
            ArrayMap<String, SparseArray<ProcessRecord>> map = mProcessNames.getMap();
            int N = map.size();
            while (N-- > 0) {
                SparseArray<ProcessRecord> uids = map.valueAt(N);
                for (int i = 0; i < uids.size(); i++) {
                    ProcessRecord r = uids.valueAt(i);
                    if (userId != VUserHandle.USER_ALL) {
                        if (r.userId != userId) {
                            continue;
                        }
                    }
                    if (r.pkgList.contains(pkg)) {
                        killProcess(r.pid);
                    }
                }
            }
        }
    }

    @Override
    public boolean isAppRunning(String packageName, int userId) {
        boolean running = false;
        synchronized (mPidsSelfLocked) {
            int N = mPidsSelfLocked.size();
            while (N-- > 0) {
                ProcessRecord r = mPidsSelfLocked.valueAt(N);
                if (r.userId == userId && r.info.packageName.equals(packageName)) {
                    running = true;
                    break;
                }
            }
            return running;
        }
    }

    @Override
    public void killApplicationProcess(final String processName, int uid) {
        synchronized (mProcessNames) {
            ProcessRecord r = mProcessNames.get(processName, uid);
            if (r != null) {
                killProcess(r.pid);
            }
        }
    }

    @Override
    public void dump() {

    }

    @Override
    public String getInitialPackage(int pid) {
        synchronized (mPidsSelfLocked) {
            ProcessRecord r = mPidsSelfLocked.get(pid);
            if (r != null) {
                return r.info.packageName;
            }
            return null;
        }
    }

    @Override
    public void handleApplicationCrash() {
        // Nothing
    }

    @Override
    public void appDoneExecuting() {
        synchronized (mPidsSelfLocked) {
            ProcessRecord r = mPidsSelfLocked.get(getCallingPid());
            if (r != null) {
                r.doneExecuting = true;
                r.lock.open();
            }
        }
    }


    /**
     * Should guard by {@link VActivityManagerService#mPidsSelfLocked}
     *
     * @param pid pid
     */
    public ProcessRecord findProcessLocked(int pid) {
        return mPidsSelfLocked.get(pid);
    }

    /**
     * Should guard by {@link VActivityManagerService#mProcessNames}
     *
     * @param uid vuid
     */
    public ProcessRecord findProcessLocked(String processName, int uid) {
        return mProcessNames.get(processName, uid);
    }

    public int stopUser(int userHandle, IStopUserCallback.Stub stub) {
        synchronized (mPidsSelfLocked) {
            int N = mPidsSelfLocked.size();
            while (N-- > 0) {
                ProcessRecord r = mPidsSelfLocked.valueAt(N);
                if (r.userId == userHandle) {
                    killProcess(r.pid);
                }
            }
        }
        try {
            stub.userStopped(userHandle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void sendOrderedBroadcastAsUser(Intent intent, VUserHandle user, String receiverPermission,
                                           BroadcastReceiver resultReceiver, Handler scheduler, int initialCode,
                                           String initialData, Bundle initialExtras) {
        Context context = VirtualCore.get().getContext();
        if (user != null) {
            intent.putExtra("_VA_|_user_id_", user.getIdentifier());
        }
        // TODO: checkPermission
        context.sendOrderedBroadcast(intent, null/* permission */, resultReceiver, scheduler, initialCode, initialData,
                initialExtras);
    }

    public void sendBroadcastAsUser(Intent intent, VUserHandle user) {
        SpecialComponentList.protectIntent(intent);
        Context context = VirtualCore.get().getContext();
        if (user != null) {
            intent.putExtra("_VA_|_user_id_", user.getIdentifier());
        }
        context.sendBroadcast(intent);
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection connection, int flags, VUserHandle user) {
        service = new Intent(service);
        if (user != null) {
            service.putExtra("_VA_|_user_id_", user.getIdentifier());
        }
        return VirtualCore.get().getContext().bindService(service, connection, flags);
    }

    public void sendBroadcastAsUser(Intent intent, VUserHandle user, String permission) {
        SpecialComponentList.protectIntent(intent);
        Context context = VirtualCore.get().getContext();
        if (user != null) {
            intent.putExtra("_VA_|_user_id_", user.getIdentifier());
        }
        // TODO: checkPermission
        context.sendBroadcast(intent);
    }

    boolean handleStaticBroadcast(int appId, ActivityInfo info, Intent intent,
                                  PendingResultData result) {
        Intent realIntent = intent.getParcelableExtra("_VA_|_intent_");
        ComponentName component = intent.getParcelableExtra("_VA_|_component_");
        int userId = intent.getIntExtra("_VA_|_user_id_", VUserHandle.USER_NULL);
        if (realIntent == null) {
            return false;
        }
        if (userId < 0) {
            VLog.w(TAG, "Sent a broadcast without userId " + realIntent);
            return false;
        }
        int vuid = VUserHandle.getUid(userId, appId);
        return handleUserBroadcast(vuid, info, component, realIntent, result);
    }

    private boolean handleUserBroadcast(int vuid, ActivityInfo info, ComponentName component, Intent realIntent, PendingResultData result) {
        if (component != null && !ComponentUtils.toComponentName(info).equals(component)) {
            // Verify the component.
            return false;
        }
        String originAction = SpecialComponentList.unprotectAction(realIntent.getAction());
        if (originAction != null) {
            // restore to origin action.
            realIntent.setAction(originAction);
        }
        handleStaticBroadcastAsUser(vuid, info, realIntent, result);
        return true;
    }

    private void handleStaticBroadcastAsUser(int vuid, ActivityInfo info, Intent intent,
                                             PendingResultData result) {
        synchronized (this) {
            ProcessRecord r = findProcessLocked(info.processName, vuid);
            if (BROADCAST_NOT_STARTED_PKG && r == null) {
                r = startProcessIfNeedLocked(info.processName, getUserId(vuid), info.packageName);
            }
            if (r != null && r.appThread != null) {
                performScheduleReceiver(r.client, vuid, info, intent,
                        result);
            }
        }
    }

    private void performScheduleReceiver(IVClient client, int vuid, ActivityInfo info, Intent intent,
                                         PendingResultData result) {

        ComponentName componentName = ComponentUtils.toComponentName(info);
        BroadcastSystem.get().broadcastSent(vuid, info, result);
        try {
            client.scheduleReceiver(info.processName, componentName, intent, result);
        } catch (Throwable e) {
            if (result != null) {
                result.finish();
            }
        }
    }

    @Override
    public void broadcastFinish(PendingResultData res) {
        BroadcastSystem.get().broadcastFinish(res);
    }

    @Override
    public void notifyBadgerChange(BadgerInfo info) {
        Intent intent = new Intent(VASettings.ACTION_BADGER_CHANGE);
        intent.putExtra("userId", info.userId);
        intent.putExtra("packageName", info.packageName);
        intent.putExtra("badgerCount", info.badgerCount);
        VirtualCore.get().getContext().sendBroadcast(intent);
    }
}