package com.lody.virtual.client.ipc;

import android.app.Activity;
import android.app.IServiceConnection;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.hook.secondary.ServiceConnectionDelegate;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.AppTaskInfo;
import com.lody.virtual.remote.BadgerInfo;
import com.lody.virtual.remote.PendingIntentData;
import com.lody.virtual.remote.PendingResultData;
import com.lody.virtual.remote.VParceledListSlice;
import com.lody.virtual.server.IActivityManager;
import com.lody.virtual.server.interfaces.IProcessObserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mirror.android.app.ActivityThread;
import mirror.android.content.ContentProviderNative;

/**
 * @author Lody
 */
public class VActivityManager {

    private static final VActivityManager sAM = new VActivityManager();
    private final Map<IBinder, ActivityClientRecord> mActivities = new HashMap<IBinder, ActivityClientRecord>(6);
    private IActivityManager mRemote;

    public static VActivityManager get() {
        return sAM;
    }

    public IActivityManager getService() {
        if (mRemote == null ||
                (!mRemote.asBinder().pingBinder() && !VirtualCore.get().isVAppProcess())) {
            synchronized (VActivityManager.class) {
                final Object remote = getRemoteInterface();
                mRemote = LocalProxyUtils.genProxy(IActivityManager.class, remote);
            }
        }
        return mRemote;
    }


    private Object getRemoteInterface() {
        return IActivityManager.Stub
                .asInterface(ServiceManagerNative.getService(ServiceManagerNative.ACTIVITY));
    }


    public int startActivity(Intent intent, ActivityInfo info, IBinder resultTo, Bundle options, String resultWho, int requestCode, int userId) {
        try {
            return getService().startActivity(intent, info, resultTo, options, resultWho, requestCode, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public int startActivities(Intent[] intents, String[] resolvedTypes, IBinder token, Bundle options, int userId) {
        try {
            return getService().startActivities(intents, resolvedTypes, token, options, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public int startActivity(Intent intent, int userId) {
        if (userId < 0) {
            return ActivityManagerCompat.START_NOT_CURRENT_USER_ACTIVITY;
        }
        ActivityInfo info = VirtualCore.get().resolveActivityInfo(intent, userId);
        if (info == null) {
            return ActivityManagerCompat.START_INTENT_NOT_RESOLVED;
        }
        return startActivity(intent, info, null, null, null, 0, userId);
    }

    public ActivityClientRecord onActivityCreate(ComponentName component, ComponentName caller, IBinder token, ActivityInfo info, Intent intent, String affinity, int taskId, int launchMode, int flags) {
        ActivityClientRecord r = new ActivityClientRecord();
        r.info = info;
        mActivities.put(token, r);
        try {
            getService().onActivityCreated(component, caller, token, intent, affinity, taskId, launchMode, flags);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return r;
    }

    public ActivityClientRecord getActivityRecord(IBinder token) {
        synchronized (mActivities) {
            return token == null ? null : mActivities.get(token);
        }
    }

    public void onActivityResumed(Activity activity) {
        IBinder token = mirror.android.app.Activity.mToken.get(activity);
        onActivityResumed(token);
    }

    public void onActivityResumed(IBinder token) {
        try {
            getService().onActivityResumed(VUserHandle.myUserId(), token);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean onActivityDestroy(IBinder token) {
        mActivities.remove(token);
        try {
            return getService().onActivityDestroyed(VUserHandle.myUserId(), token);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public AppTaskInfo getTaskInfo(int taskId) {
        try {
            return getService().getTaskInfo(taskId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ComponentName getCallingActivity(IBinder token) {
        try {
            return getService().getCallingActivity(VUserHandle.myUserId(), token);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public String getCallingPackage(IBinder token) {
        try {
            return getService().getCallingPackage(VUserHandle.myUserId(), token);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public String getPackageForToken(IBinder token) {
        try {
            return getService().getPackageForToken(VUserHandle.myUserId(), token);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ComponentName getActivityForToken(IBinder token) {
        try {
            return getService().getActivityClassForToken(VUserHandle.myUserId(), token);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public ComponentName startService(IInterface caller, Intent service, String resolvedType, int userId) {
        try {
            return getService().startService(caller != null ? caller.asBinder() : null, service, resolvedType, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public int stopService(IInterface caller, Intent service, String resolvedType) {
        try {
            return getService().stopService(caller != null ? caller.asBinder() : null, service, resolvedType, VUserHandle.myUserId());
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public boolean stopServiceToken(ComponentName className, IBinder token, int startId) {
        try {
            return getService().stopServiceToken(className, token, startId, VUserHandle.myUserId());
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, boolean removeNotification) {
        try {
            getService().setServiceForeground(className, token, id, notification,removeNotification,  VUserHandle.myUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int bindService(Context context, Intent service, ServiceConnection connection, int flags) {
        try {
            IServiceConnection conn = ServiceConnectionDelegate.getDelegate(context, connection, flags);
            return getService().bindService(null, null, service, null, conn, flags, 0);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public boolean unbindService(Context context, ServiceConnection connection) {
        try {
            IServiceConnection conn = ServiceConnectionDelegate.removeDelegate(context, connection);
            return getService().unbindService(conn, VUserHandle.myUserId());
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public int bindService(IBinder caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, int userId) {
        try {
            return getService().bindService(caller, token, service, resolvedType, connection, flags, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public boolean unbindService(IServiceConnection connection) {
        try {
            return getService().unbindService(connection, VUserHandle.myUserId());
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void unbindFinished(IBinder token, Intent service, boolean doRebind) {
        try {
            getService().unbindFinished(token, service, doRebind, VUserHandle.myUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void serviceDoneExecuting(IBinder token, int type, int startId, int res) {
        try {
            getService().serviceDoneExecuting(token, type, startId, res, VUserHandle.myUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public IBinder peekService(Intent service, String resolvedType) {
        try {
            return getService().peekService(service, resolvedType, VUserHandle.myUserId());
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void publishService(IBinder token, Intent intent, IBinder service) {
        try {
            getService().publishService(token, intent, service, VUserHandle.myUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public VParceledListSlice getServices(int maxNum, int flags) {
        try {
            return getService().getServices(maxNum, flags, VUserHandle.myUserId());
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void processRestarted(String packageName, String processName, int userId) {
        try {
            getService().processRestarted(packageName, processName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String getAppProcessName(int pid) {
        try {
            return getService().getAppProcessName(pid);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public String getInitialPackage(int pid) {
        try {
            return getService().getInitialPackage(pid);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public boolean isAppProcess(String processName) {
        try {
            return getService().isAppProcess(processName);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void handleApplicationCrash() {
        try {
            getService().handleApplicationCrash();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void killAllApps() {
        try {
            getService().killAllApps();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void killApplicationProcess(String procName, int uid) {
        try {
            getService().killApplicationProcess(procName, uid);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void registerProcessObserver(IProcessObserver observer) {
        try {
            getService().registerProcessObserver(observer);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void killAppByPkg(String pkg, int userId) {
        try {
            getService().killAppByPkg(pkg, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unregisterProcessObserver(IProcessObserver observer) {
        try {
            getService().unregisterProcessObserver(observer);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void appDoneExecuting() {
        try {
            getService().appDoneExecuting();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<String> getProcessPkgList(int pid) {
        try {
            return getService().getProcessPkgList(pid);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public boolean isAppPid(int pid) {
        try {
            return getService().isAppPid(pid);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public int getUidByPid(int pid) {
        try {
            return getService().getUidByPid(pid);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public int getSystemPid() {
        try {
            return getService().getSystemPid();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void sendActivityResult(IBinder resultTo, String resultWho, int requestCode) {
        ActivityClientRecord r = mActivities.get(resultTo);
        if (r != null && r.activity != null) {
            Object mainThread = VirtualCore.mainThread();
            ActivityThread.sendActivityResult.call(mainThread, resultTo, resultWho, requestCode, 0, null);
        }
    }

    public IInterface acquireProviderClient(int userId, ProviderInfo info) throws RemoteException {
        return ContentProviderNative.asInterface.call(getService().acquireProviderClient(userId, info));
    }

    public PendingIntentData getPendingIntent(IBinder binder) throws RemoteException {
        return getService().getPendingIntent(binder);
    }

    public void addPendingIntent(IBinder binder, String creator) throws RemoteException {
        getService().addPendingIntent(binder, creator);
    }

    public void removePendingIntent(IBinder binder) throws RemoteException {
        getService().removePendingIntent(binder);
    }

    public void finishActivity(IBinder token) {
        ActivityClientRecord r = getActivityRecord(token);
        if (r != null) {
            Activity activity = r.activity;
            while (true) {
                // We shouldn't use Activity.getParent(),
                // because It may be overwritten.
                Activity parent = mirror.android.app.Activity.mParent.get(activity);
                if (parent == null) {
                    break;
                }
                activity = parent;
            }
            // We shouldn't use Activity.isFinishing(),
            // because It may be overwritten.
            if (!mirror.android.app.Activity.mFinished.get(activity)) {
                int resultCode = mirror.android.app.Activity.mResultCode.get(activity);
                Intent resultData = mirror.android.app.Activity.mResultData.get(activity);
                ActivityManagerCompat.finishActivity(token, resultCode, resultData);
                mirror.android.app.Activity.mFinished.set(activity, true);
            }
        }
    }

    public boolean isAppRunning(String packageName, int userId) {
        try {
            return getService().isAppRunning(packageName, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public int initProcess(String packageName, String processName, int userId) {
        try {
            return getService().initProcess(packageName, processName, userId);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void sendBroadcast(Intent intent, int userId) {
        Intent newIntent = ComponentUtils.redirectBroadcastIntent(intent, userId);
        if (newIntent != null) {
            VirtualCore.get().getContext().sendBroadcast(newIntent);
        }
    }

    public boolean isVAServiceToken(IBinder token) {
        try {
            return getService().isVAServiceToken(token);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void broadcastFinish(PendingResultData res) {
        try {
            getService().broadcastFinish(res);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public String getPackageForIntentSender(IBinder binder) {
        try {
            return getService().getPackageForIntentSender(binder);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void notifyBadgerChange(BadgerInfo info) {
        try {
            getService().notifyBadgerChange(info);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }
}
