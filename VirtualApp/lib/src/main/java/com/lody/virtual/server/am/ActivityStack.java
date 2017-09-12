package com.lody.virtual.server.am;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.SparseArray;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.ClassUtils;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.remote.AppTaskInfo;
import com.lody.virtual.remote.StubActivityRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import mirror.android.app.ActivityManagerNative;
import mirror.android.app.ActivityThread;
import mirror.android.app.IActivityManager;
import mirror.android.app.IApplicationThread;
import mirror.com.android.internal.R_Hide;

import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_TASK;
import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_TOP;

/**
 * @author Lody
 */

/* package */ class ActivityStack {

    private final ActivityManager mAM;
    private final VActivityManagerService mService;

    /**
     * [Key] = TaskId [Value] = TaskRecord
     */
    private final SparseArray<TaskRecord> mHistory = new SparseArray<>();


    ActivityStack(VActivityManagerService mService) {
        this.mService = mService;
        mAM = (ActivityManager) VirtualCore.get().getContext().getSystemService(Context.ACTIVITY_SERVICE);
    }

    private static void removeFlags(Intent intent, int flags) {
        intent.setFlags(intent.getFlags() & ~flags);
    }

    private static boolean containFlags(Intent intent, int flags) {
        return (intent.getFlags() & flags) != 0;
    }

    private static ActivityRecord topActivityInTask(TaskRecord task) {
        synchronized (task.activities) {
            for (int size = task.activities.size() - 1; size >= 0; size--) {
                ActivityRecord r = task.activities.get(size);
                if (!r.marked) {
                    return r;
                }
            }
            return null;
        }
    }


    private void deliverNewIntentLocked(ActivityRecord sourceRecord, ActivityRecord targetRecord, Intent intent) {
        if (targetRecord == null) {
            return;
        }
        String creator = sourceRecord != null ? sourceRecord.component.getPackageName() : "android";
        try {
            targetRecord.process.client.scheduleNewIntent(creator, targetRecord.token, intent);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    private TaskRecord findTaskByAffinityLocked(int userId, String affinity) {
        for (int i = 0; i < this.mHistory.size(); i++) {
            TaskRecord r = this.mHistory.valueAt(i);
            if (userId == r.userId && affinity.equals(r.affinity)) {
                return r;
            }
        }
        return null;
    }

    private TaskRecord findTaskByIntentLocked(int userId, Intent intent) {
        for (int i = 0; i < this.mHistory.size(); i++) {
            TaskRecord r = this.mHistory.valueAt(i);
            if (userId == r.userId && r.taskRoot != null && intent.getComponent().equals(r.taskRoot.getComponent())) {
                return r;
            }
        }
        return null;
    }

    private ActivityRecord findActivityByToken(int userId, IBinder token) {
        ActivityRecord target = null;
        if (token != null) {
            for (int i = 0; i < this.mHistory.size(); i++) {
                TaskRecord task = this.mHistory.valueAt(i);
                if (task.userId != userId) {
                    continue;
                }
                synchronized (task.activities) {
                    for (ActivityRecord r : task.activities) {
                        if (r.token == token) {
                            target = r;
                        }
                    }
                }
            }
        }
        return target;
    }

    private boolean markTaskByClearTarget(TaskRecord task, ClearTarget clearTarget, ComponentName component) {
        boolean marked = false;
        synchronized (task.activities) {
            switch (clearTarget) {
                case TASK: {
                    for (ActivityRecord r : task.activities) {
                        r.marked = true;
                        marked = true;
                    }
                }
                break;
                case SPEC_ACTIVITY: {
                    for (ActivityRecord r : task.activities) {
                        if (r.component.equals(component)) {
                            r.marked = true;
                            marked = true;
                        }
                    }
                }
                break;
                case TOP: {
                    int N = task.activities.size();
                    while (N-- > 0) {
                        ActivityRecord r = task.activities.get(N);
                        if (r.component.equals(component)) {
                            marked = true;
                            break;
                        }
                    }
                    if (marked) {
                        while (N++ < task.activities.size() - 1) {
                            task.activities.get(N).marked = true;
                        }
                    }
                }
                break;
            }
        }
        return marked;
    }

    /**
     * App started in VA may be removed in OverView screen, then AMS.removeTask
     * will be invoked, all data struct about the task in AMS are released,
     * while the client's process is still alive. So remove related data in VA
     * as well. A new TaskRecord will be recreated in `onActivityCreated`
     */
    private void optimizeTasksLocked() {
        // noinspection deprecation
        ArrayList<ActivityManager.RecentTaskInfo> recentTask = new ArrayList<>(mAM.getRecentTasks(Integer.MAX_VALUE,
                ActivityManager.RECENT_WITH_EXCLUDED | ActivityManager.RECENT_IGNORE_UNAVAILABLE));
        int N = mHistory.size();
        while (N-- > 0) {
            TaskRecord task = mHistory.valueAt(N);
            ListIterator<ActivityManager.RecentTaskInfo> iterator = recentTask.listIterator();
            boolean taskAlive = false;
            while (iterator.hasNext()) {
                ActivityManager.RecentTaskInfo info = iterator.next();
                if (info.id == task.taskId) {
                    taskAlive = true;
                    iterator.remove();
                    break;
                }
            }
            if (!taskAlive) {
                mHistory.removeAt(N);
            }
        }
    }


    int startActivitiesLocked(int userId, Intent[] intents, ActivityInfo[] infos, String[] resolvedTypes, IBinder token, Bundle options) {
        optimizeTasksLocked();
        ReuseTarget reuseTarget = ReuseTarget.CURRENT;
        Intent intent = intents[0];
        ActivityInfo info = infos[0];
        ActivityRecord resultTo = findActivityByToken(userId, token);
        if (resultTo != null && resultTo.launchMode == LAUNCH_SINGLE_INSTANCE) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (containFlags(intent, Intent.FLAG_ACTIVITY_CLEAR_TOP)) {
            removeFlags(intent, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        if (containFlags(intent, Intent.FLAG_ACTIVITY_CLEAR_TASK) && !containFlags(intent, Intent.FLAG_ACTIVITY_NEW_TASK)) {
            removeFlags(intent, Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switch (info.documentLaunchMode) {
                case ActivityInfo.DOCUMENT_LAUNCH_INTO_EXISTING:
                    reuseTarget = ReuseTarget.DOCUMENT;
                    break;
                case ActivityInfo.DOCUMENT_LAUNCH_ALWAYS:
                    reuseTarget = ReuseTarget.MULTIPLE;
                    break;
            }
        }
        if (containFlags(intent, Intent.FLAG_ACTIVITY_NEW_TASK)) {
            reuseTarget = containFlags(intent, Intent.FLAG_ACTIVITY_MULTIPLE_TASK) ? ReuseTarget.MULTIPLE : ReuseTarget.AFFINITY;
        } else if (info.launchMode == LAUNCH_SINGLE_TASK) {
            reuseTarget = containFlags(intent, Intent.FLAG_ACTIVITY_MULTIPLE_TASK) ? ReuseTarget.MULTIPLE : ReuseTarget.AFFINITY;
        }
        if (resultTo == null && reuseTarget == ReuseTarget.CURRENT) {
            reuseTarget = ReuseTarget.AFFINITY;
        }
        String affinity = ComponentUtils.getTaskAffinity(info);
        TaskRecord reuseTask = null;
        if (reuseTarget == ReuseTarget.AFFINITY) {
            reuseTask = findTaskByAffinityLocked(userId, affinity);
        } else if (reuseTarget == ReuseTarget.CURRENT) {
            reuseTask = resultTo.task;
        } else if (reuseTarget == ReuseTarget.DOCUMENT) {
            reuseTask = findTaskByIntentLocked(userId, intent);
        }
        Intent[] destIntents = startActivitiesProcess(userId, intents, infos, resultTo);
        if (reuseTask == null) {
            realStartActivitiesLocked(null, destIntents, resolvedTypes, options);
        } else {
            ActivityRecord top = topActivityInTask(reuseTask);
            if (top != null) {
                realStartActivitiesLocked(top.token, destIntents, resolvedTypes, options);
            }
        }
        return 0;
    }

    private Intent[] startActivitiesProcess(int userId, Intent[] intents, ActivityInfo[] infos, ActivityRecord resultTo) {
        Intent[] destIntents = new Intent[intents.length];
        for (int i = 0; i < intents.length; i++) {
            destIntents[i] = startActivityProcess(userId, resultTo, intents[i], infos[i]);
        }
        return destIntents;
    }


    int startActivityLocked(int userId, Intent intent, ActivityInfo info, IBinder resultTo, Bundle options,
                            String resultWho, int requestCode) {
        optimizeTasksLocked();

        Intent destIntent;
        ActivityRecord sourceRecord = findActivityByToken(userId, resultTo);
        TaskRecord sourceTask = sourceRecord != null ? sourceRecord.task : null;

        ReuseTarget reuseTarget = ReuseTarget.CURRENT;
        ClearTarget clearTarget = ClearTarget.NOTHING;
        boolean clearTop = containFlags(intent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
        boolean clearTask = containFlags(intent, Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (intent.getComponent() == null) {
            intent.setComponent(new ComponentName(info.packageName, info.name));
        }
        if (sourceRecord != null && sourceRecord.launchMode == LAUNCH_SINGLE_INSTANCE) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (clearTop) {
            removeFlags(intent, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            clearTarget = ClearTarget.TOP;
        }
        if (clearTask) {
            if (containFlags(intent, Intent.FLAG_ACTIVITY_NEW_TASK)) {
                clearTarget = ClearTarget.TASK;
            } else {
                removeFlags(intent, Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switch (info.documentLaunchMode) {
                case ActivityInfo.DOCUMENT_LAUNCH_INTO_EXISTING:
                    clearTarget = ClearTarget.TASK;
                    reuseTarget = ReuseTarget.DOCUMENT;
                    break;
                case ActivityInfo.DOCUMENT_LAUNCH_ALWAYS:
                    reuseTarget = ReuseTarget.MULTIPLE;
                    break;
            }
        }
        boolean singleTop = false;

        switch (info.launchMode) {
            case LAUNCH_SINGLE_TOP: {
                singleTop = true;
                if (containFlags(intent, Intent.FLAG_ACTIVITY_NEW_TASK)) {
                    reuseTarget = containFlags(intent, Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                            ? ReuseTarget.MULTIPLE
                            : ReuseTarget.AFFINITY;
                }
            }
            break;
            case LAUNCH_SINGLE_TASK: {
                clearTop = false;
                clearTarget = ClearTarget.TOP;
                reuseTarget = containFlags(intent, Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        ? ReuseTarget.MULTIPLE
                        : ReuseTarget.AFFINITY;
            }
            break;
            case LAUNCH_SINGLE_INSTANCE: {
                clearTop = false;
                clearTarget = ClearTarget.TOP;
                reuseTarget = ReuseTarget.AFFINITY;
            }
            break;
            default: {
                if (containFlags(intent, Intent.FLAG_ACTIVITY_SINGLE_TOP)) {
                    singleTop = true;
                }
            }
            break;
        }
        if (clearTarget == ClearTarget.NOTHING) {
            if (containFlags(intent, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)) {
                clearTarget = ClearTarget.SPEC_ACTIVITY;
            }
        }
        if (sourceTask == null && reuseTarget == ReuseTarget.CURRENT) {
            reuseTarget = ReuseTarget.AFFINITY;
        }

        String affinity = ComponentUtils.getTaskAffinity(info);
        TaskRecord reuseTask = null;
        switch (reuseTarget) {
            case AFFINITY:
                reuseTask = findTaskByAffinityLocked(userId, affinity);
                break;
            case DOCUMENT:
                reuseTask = findTaskByIntentLocked(userId, intent);
                break;
            case CURRENT:
                reuseTask = sourceTask;
                break;
            default:
                break;
        }

        boolean taskMarked = false;
        if (reuseTask == null) {
            startActivityInNewTaskLocked(userId, intent, info, options);
        } else {
            boolean delivered = false;
            mAM.moveTaskToFront(reuseTask.taskId, 0);
            boolean startTaskToFront = !clearTask && !clearTop && ComponentUtils.isSameIntent(intent, reuseTask.taskRoot);

            if (clearTarget.deliverIntent || singleTop) {
                taskMarked = markTaskByClearTarget(reuseTask, clearTarget, intent.getComponent());
                ActivityRecord topRecord = topActivityInTask(reuseTask);
                if (clearTop && !singleTop && topRecord != null && taskMarked) {
                    topRecord.marked = true;
                }
                // Target activity is on top
                if (topRecord != null && !topRecord.marked && topRecord.component.equals(intent.getComponent())) {
                    deliverNewIntentLocked(sourceRecord, topRecord, intent);
                    delivered = true;
                }
            }
            if (taskMarked) {
                synchronized (mHistory) {
                    scheduleFinishMarkedActivityLocked();
                }
            }
            if (!startTaskToFront) {
                if (!delivered) {
                    destIntent = startActivityProcess(userId, sourceRecord, intent, info);
                    if (destIntent != null) {
                        startActivityFromSourceTask(reuseTask, destIntent, info, resultWho, requestCode, options);
                    }
                }
            }
        }
        return 0;
    }

    private void startActivityInNewTaskLocked(int userId, Intent intent, ActivityInfo info, Bundle options) {
        Intent destIntent = startActivityProcess(userId, null, intent, info);
        if (destIntent != null) {
            destIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            destIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            destIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // noinspection deprecation
                destIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            } else {
                destIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                VirtualCore.get().getContext().startActivity(destIntent, options);
            } else {
                VirtualCore.get().getContext().startActivity(destIntent);
            }
        }
    }

    private void scheduleFinishMarkedActivityLocked() {
        int N = mHistory.size();
        while (N-- > 0) {
            final TaskRecord task = mHistory.valueAt(N);
            for (final ActivityRecord r : task.activities) {
                if (!r.marked) {
                    continue;
                }
                VirtualRuntime.getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            r.process.client.finishActivity(r.token);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private void startActivityFromSourceTask(TaskRecord task, Intent intent, ActivityInfo info, String resultWho,
                                             int requestCode, Bundle options) {
        ActivityRecord top = task.activities.isEmpty() ? null : task.activities.get(task.activities.size() - 1);
        if (top != null) {
            if (startActivityProcess(task.userId, top, intent, info) != null) {
                realStartActivityLocked(top.token, intent, resultWho, requestCode, options);
            }
        }
    }


    private void realStartActivitiesLocked(IBinder resultTo, Intent[] intents, String[] resolvedTypes, Bundle options) {
        Class<?>[] types = IActivityManager.startActivities.paramList();
        Object[] args = new Object[types.length];
        if (types[0] == IApplicationThread.TYPE) {
            args[0] = ActivityThread.getApplicationThread.call(VirtualCore.mainThread());
        }
        int pkgIndex = ArrayUtils.protoIndexOf(types, String.class);
        int intentsIndex = ArrayUtils.protoIndexOf(types, Intent[].class);
        int resultToIndex = ArrayUtils.protoIndexOf(types, IBinder.class, 2);
        int optionsIndex = ArrayUtils.protoIndexOf(types, Bundle.class);
        int resolvedTypesIndex = intentsIndex + 1;
        if (pkgIndex != -1) {
            args[pkgIndex] = VirtualCore.get().getHostPkg();
        }
        args[intentsIndex] = intents;
        args[resultToIndex] = resultTo;
        args[resolvedTypesIndex] = resolvedTypes;
        args[optionsIndex] = options;
        ClassUtils.fixArgs(types, args);
        IActivityManager.startActivities.call(ActivityManagerNative.getDefault.call(),
                (Object[]) args);
    }

    private void realStartActivityLocked(IBinder resultTo, Intent intent, String resultWho, int requestCode,
                                         Bundle options) {
        Class<?>[] types = mirror.android.app.IActivityManager.startActivity.paramList();
        Object[] args = new Object[types.length];
        if (types[0] == IApplicationThread.TYPE) {
            args[0] = ActivityThread.getApplicationThread.call(VirtualCore.mainThread());
        }
        int intentIndex = ArrayUtils.protoIndexOf(types, Intent.class);
        int resultToIndex = ArrayUtils.protoIndexOf(types, IBinder.class, 2);
        int optionsIndex = ArrayUtils.protoIndexOf(types, Bundle.class);
        int resolvedTypeIndex = intentIndex + 1;
        int resultWhoIndex = resultToIndex + 1;
        int requestCodeIndex = resultToIndex + 2;

        args[intentIndex] = intent;
        args[resultToIndex] = resultTo;
        args[resultWhoIndex] = resultWho;
        args[requestCodeIndex] = requestCode;
        if (optionsIndex != -1) {
            args[optionsIndex] = options;
        }
        args[resolvedTypeIndex] = intent.getType();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            args[intentIndex - 1] = VirtualCore.get().getHostPkg();
        }
        ClassUtils.fixArgs(types, args);

        mirror.android.app.IActivityManager.startActivity.call(ActivityManagerNative.getDefault.call(),
                (Object[]) args);
    }

    private String fetchStubActivity(int vpid, ActivityInfo targetInfo) {

        boolean isFloating = false;
        boolean isTranslucent = false;
        boolean showWallpaper = false;
        try {
            int[] R_Styleable_Window = R_Hide.styleable.Window.get();
            int R_Styleable_Window_windowIsTranslucent = R_Hide.styleable.Window_windowIsTranslucent.get();
            int R_Styleable_Window_windowIsFloating = R_Hide.styleable.Window_windowIsFloating.get();
            int R_Styleable_Window_windowShowWallpaper = R_Hide.styleable.Window_windowShowWallpaper.get();

            AttributeCache.Entry ent = AttributeCache.instance().get(targetInfo.packageName, targetInfo.theme,
                    R_Styleable_Window);
            if (ent != null && ent.array != null) {
                showWallpaper = ent.array.getBoolean(R_Styleable_Window_windowShowWallpaper, false);
                isTranslucent = ent.array.getBoolean(R_Styleable_Window_windowIsTranslucent, false);
                isFloating = ent.array.getBoolean(R_Styleable_Window_windowIsFloating, false);
            }else{
                Resources resources=VirtualCore.get().getResources(targetInfo.packageName);
                if(resources!=null) {
                    TypedArray typedArray = resources.newTheme().obtainStyledAttributes(targetInfo.theme, R_Styleable_Window);
                    if(typedArray!=null){
                        showWallpaper = typedArray.getBoolean(R_Styleable_Window_windowShowWallpaper, false);
                        isTranslucent = typedArray.getBoolean(R_Styleable_Window_windowIsTranslucent, false);
                        isFloating = typedArray.getBoolean(R_Styleable_Window_windowIsFloating, false);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        boolean isDialogStyle = isFloating || isTranslucent || showWallpaper;
        if (isDialogStyle) {
            return VASettings.getStubDialogName(vpid);
        } else {
            return VASettings.getStubActivityName(vpid);
        }
    }

    private Intent startActivityProcess(int userId, ActivityRecord sourceRecord, Intent intent, ActivityInfo info) {
        intent = new Intent(intent);
        ProcessRecord targetApp = mService.startProcessIfNeedLocked(info.processName, userId, info.packageName);
        if (targetApp == null) {
            return null;
        }
        Intent targetIntent = new Intent();
        targetIntent.setClassName(VirtualCore.get().getHostPkg(), fetchStubActivity(targetApp.vpid, info));
        ComponentName component = intent.getComponent();
        if (component == null) {
            component = ComponentUtils.toComponentName(info);
        }
        targetIntent.setType(component.flattenToString());
        StubActivityRecord saveInstance = new StubActivityRecord(intent, info,
                sourceRecord != null ? sourceRecord.component : null, userId);
        saveInstance.saveToIntent(targetIntent);
        return targetIntent;
    }

    void onActivityCreated(ProcessRecord targetApp, ComponentName component, ComponentName caller, IBinder token,
                           Intent taskRoot, String affinity, int taskId, int launchMode, int flags) {
        synchronized (mHistory) {
            optimizeTasksLocked();
            TaskRecord task = mHistory.get(taskId);
            if (task == null) {
                task = new TaskRecord(taskId, targetApp.userId, affinity, taskRoot);
                mHistory.put(taskId, task);
            }
            ActivityRecord record = new ActivityRecord(task, component, caller, token, targetApp.userId, targetApp,
                    launchMode, flags, affinity);
            synchronized (task.activities) {
                task.activities.add(record);
            }
        }
    }

    void onActivityResumed(int userId, IBinder token) {
        synchronized (mHistory) {
            optimizeTasksLocked();
            ActivityRecord r = findActivityByToken(userId, token);
            if (r != null) {
                synchronized (r.task.activities) {
                    r.task.activities.remove(r);
                    r.task.activities.add(r);
                }
            }
        }
    }

    ActivityRecord onActivityDestroyed(int userId, IBinder token) {
        synchronized (mHistory) {
            optimizeTasksLocked();
            ActivityRecord r = findActivityByToken(userId, token);
            if (r != null) {
                synchronized (r.task.activities) {
                    r.task.activities.remove(r);
                    // We shouldn't remove task at this point,
                    // it will be removed by optimizeTasksLocked().
                }
            }
            return r;
        }
    }

    void processDied(ProcessRecord record) {
        synchronized (mHistory) {
            optimizeTasksLocked();
            int N = mHistory.size();
            while (N-- > 0) {
                TaskRecord task = mHistory.valueAt(N);
                synchronized (task.activities) {
                    Iterator<ActivityRecord> iterator = task.activities.iterator();
                    while (iterator.hasNext()) {
                        ActivityRecord r = iterator.next();
                        if (r.process.pid == record.pid) {
                            iterator.remove();
                            if (task.activities.isEmpty()) {
                                mHistory.remove(task.taskId);
                            }
                        }
                    }
                }
            }

        }
    }

    String getPackageForToken(int userId, IBinder token) {
        synchronized (mHistory) {
            ActivityRecord r = findActivityByToken(userId, token);
            if (r != null) {
                return r.component.getPackageName();
            }
            return null;
        }
    }

    ComponentName getCallingActivity(int userId, IBinder token) {
        synchronized (mHistory) {
            ActivityRecord r = findActivityByToken(userId, token);
            if (r != null) {
                return r.caller != null ? r.caller : r.component;
            }
            return null;
        }
    }

    public String getCallingPackage(int userId, IBinder token) {
        synchronized (mHistory) {
            ActivityRecord r = findActivityByToken(userId, token);
            if (r != null) {
                return r.caller != null ? r.caller.getPackageName() : "android";
            }
            return "android";
        }
    }

    AppTaskInfo getTaskInfo(int taskId) {
        synchronized (mHistory) {
            TaskRecord task = mHistory.get(taskId);
            if (task != null) {
                return task.getAppTaskInfo();
            }
            return null;
        }
    }

    ComponentName getActivityClassForToken(int userId, IBinder token) {
        synchronized (mHistory) {
            ActivityRecord r = findActivityByToken(userId, token);
            if (r != null) {
                return r.component;
            }
            return null;
        }
    }

    private enum ClearTarget {
        NOTHING,
        SPEC_ACTIVITY,
        TASK(true),
        TOP(true);

        boolean deliverIntent;

        ClearTarget() {
            this(false);
        }

        ClearTarget(boolean deliverIntent) {
            this.deliverIntent = deliverIntent;
        }
    }

    private enum ReuseTarget {
        CURRENT, AFFINITY, DOCUMENT, MULTIPLE
    }
}