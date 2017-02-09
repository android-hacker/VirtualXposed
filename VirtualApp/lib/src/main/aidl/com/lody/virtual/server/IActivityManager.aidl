// IActivityManager.aidl
package com.lody.virtual.server;

import com.lody.virtual.helper.proto.VParceledListSlice;
import com.lody.virtual.helper.proto.AppTaskInfo;
import com.lody.virtual.helper.proto.PendingIntentData;
import com.lody.virtual.helper.proto.PendingResultData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.app.Notification;
import android.app.IServiceConnection;
import android.app.IActivityManager.ContentProviderHolder;
import com.lody.virtual.server.interfaces.IProcessObserver;
import com.lody.virtual.server.interfaces.IUiObserver;


interface IActivityManager {

    int initProcess(in String packageName, in String processName, int userId);

    int getFreeStubCount();

    int getSystemPid();

    int getUidByPid(int pid);

    void registerUIObserver(IUiObserver observer);

    void unregisterUIObserver(IUiObserver observer);

    boolean isAppProcess(String processName);

    boolean isAppRunning(String packageName, int userId);

    boolean isAppPid(int pid);

    String getAppProcessName(int pid);

    List<String> getProcessPkgList(int pid);

    void killAllApps();

    void killAppByPkg(String pkg, int userId);

    void killApplicationProcess(String procName, int vuid);

    void dump();

    void registerProcessObserver(in IProcessObserver observer);

    void unregisterProcessObserver(in IProcessObserver observer);

    String getInitialPackage(int pid);

    void handleApplicationCrash();

    void appDoneExecuting();

    int startActivity(in Intent intent, in ActivityInfo info, in IBinder resultTo, in Bundle options, String resultWho, int requestCode, int userId);

    void onActivityCreated(in ComponentName component, in ComponentName caller, in IBinder token, in Intent intent, in String affinity, int taskId, int launchMode, int flags);

    void onActivityResumed(int userId, in IBinder token);

    boolean onActivityDestroyed(int userId, in IBinder token);

    ComponentName getActivityClassForToken(int userId, in IBinder token);

    String getCallingPackage(int userId, in IBinder token);

    ComponentName getCallingActivity(int userId, in IBinder token);

    AppTaskInfo getTaskInfo(int taskId);

    String getPackageForToken(int userId, in IBinder token);

    boolean isVAServiceToken(in IBinder token);

    ComponentName startService(in IBinder caller,in Intent service, String resolvedType, int userId);

    int stopService(in IBinder caller, in Intent service, String resolvedType, int userId);

    boolean stopServiceToken(in ComponentName className, in IBinder token, int startId, int userId);

    void setServiceForeground(in ComponentName className, in IBinder token, int id,
                            in Notification notification, boolean keepNotification, int userId);

    int bindService(in IBinder caller, in IBinder token, in Intent service,
                    String resolvedType, in IServiceConnection connection, int flags, int userId);

    boolean unbindService(in IServiceConnection connection, int userId);

    void unbindFinished(in IBinder token, in Intent service, in boolean doRebind, int userId);

    void serviceDoneExecuting(in IBinder token, in int type, in int startId, in int res, int userId);

    IBinder peekService(in Intent service, String resolvedType, int userId);

    void publishService(in IBinder token, in Intent intent, in IBinder service, int userId);

    VParceledListSlice getServices(int maxNum, int flags, int userId);

    IBinder acquireProviderClient(int userId, in ProviderInfo info);

    PendingIntentData getPendingIntent(IBinder binder);

    void addPendingIntent(IBinder binder, String packageName);

    void removePendingIntent(IBinder binder);

    void processRestarted(in String packageName, in String processName, int userId);

    void broadcastFinish(in PendingResultData res);

    Intent dispatchStickyBroadcast(in IntentFilter filter);
}
