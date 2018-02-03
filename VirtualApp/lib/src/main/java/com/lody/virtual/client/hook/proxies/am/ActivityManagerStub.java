package com.lody.virtual.client.hook.proxies.am;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationStub;
import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.MethodInvocationProxy;
import com.lody.virtual.client.hook.base.MethodInvocationStub;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.ReplaceLastUidMethodProxy;
import com.lody.virtual.client.hook.base.ResultStaticMethodProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.helper.compat.BuildCompat;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.remote.AppTaskInfo;

import java.lang.reflect.Method;
import java.util.List;

import mirror.android.app.ActivityManagerNative;
import mirror.android.app.ActivityManagerOreo;
import mirror.android.app.IActivityManager;
import mirror.android.content.pm.ParceledListSlice;
import mirror.android.os.ServiceManager;
import mirror.android.util.Singleton;

/**
 * @author Lody
 * @see IActivityManager
 * @see android.app.ActivityManager
 */

@Inject(MethodProxies.class)
public class ActivityManagerStub extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {

    public ActivityManagerStub() {
        super(new MethodInvocationStub<>(ActivityManagerNative.getDefault.call()));
    }

    @Override
    public void inject() throws Throwable {
        if (BuildCompat.isOreo()) {
            //Android Oreo(8.X)
            Object singleton = ActivityManagerOreo.IActivityManagerSingleton.get();
            Singleton.mInstance.set(singleton, getInvocationStub().getProxyInterface());
        } else {
            if (ActivityManagerNative.gDefault.type() == IActivityManager.TYPE) {
                ActivityManagerNative.gDefault.set(getInvocationStub().getProxyInterface());
            } else if (ActivityManagerNative.gDefault.type() == Singleton.TYPE) {
                Object gDefault = ActivityManagerNative.gDefault.get();
                Singleton.mInstance.set(gDefault, getInvocationStub().getProxyInterface());
            }
        }
        BinderInvocationStub hookAMBinder = new BinderInvocationStub(getInvocationStub().getBaseInterface());
        hookAMBinder.copyMethodProxies(getInvocationStub());
        ServiceManager.sCache.get().put(Context.ACTIVITY_SERVICE, hookAMBinder);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        if (VirtualCore.get().isVAppProcess()) {
            addMethodProxy(new StaticMethodProxy("navigateUpTo") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    // throw new RuntimeException("Call navigateUpTo!!!!");
                    VLog.e("VA", "Call navigateUpTo!!!!");
                    return method.invoke(who, args);
                }
            });
            addMethodProxy(new ReplaceLastUidMethodProxy("checkPermissionWithToken"));
            addMethodProxy(new isUserRunning());
            addMethodProxy(new ResultStaticMethodProxy("updateConfiguration", 0));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("setAppLockedVerifying"));
            addMethodProxy(new StaticMethodProxy("checkUriPermission") {
                @Override
                public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
                    return PackageManager.PERMISSION_GRANTED;
                }
            });
            addMethodProxy(new StaticMethodProxy("getRecentTasks") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    Object _infos = method.invoke(who, args);
                    //noinspection unchecked
                    List<ActivityManager.RecentTaskInfo> infos =
                            ParceledListSliceCompat.isReturnParceledListSlice(method)
                                    ? ParceledListSlice.getList.call(_infos)
                                    : (List) _infos;
                    for (ActivityManager.RecentTaskInfo info : infos) {
                        AppTaskInfo taskInfo = VActivityManager.get().getTaskInfo(info.id);
                        if (taskInfo == null) {
                            continue;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                info.topActivity = taskInfo.topActivity;
                                info.baseActivity = taskInfo.baseActivity;
                            } catch (Throwable e) {
                                // ignore
                            }
                        }
                        try {
                            info.origActivity = taskInfo.baseActivity;
                            info.baseIntent = taskInfo.baseIntent;
                        } catch (Throwable e) {
                            // ignore
                        }
                    }
                    return _infos;
                }
            });
            addMethodProxy(new StaticMethodProxy("getRunningTasks") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    Object _infos = method.invoke(who, args);
                    //noinspection unchecked
                    List<ActivityManager.RunningTaskInfo> infos =
                            ParceledListSliceCompat.isReturnParceledListSlice(method)
                                    ? ParceledListSlice.getList.call(_infos)
                                    : (List) _infos;
                    for (ActivityManager.RunningTaskInfo info : infos) {
                        AppTaskInfo taskInfo = VActivityManager.get().getTaskInfo(info.id);
                        if (taskInfo == null) {
                            continue;
                        }
                        info.description = "Virtual";
                        info.topActivity = taskInfo.topActivity;
                        info.baseActivity = taskInfo.baseActivity;
                    }
                    return _infos;
                }
            });
        }
    }

    @Override
    public boolean isEnvBad() {
        return ActivityManagerNative.getDefault.call() != getInvocationStub().getProxyInterface();
    }

    private class isUserRunning extends MethodProxy {
        @Override
        public String getMethodName() {
            return "isUserRunning";
        }

        @Override
        public Object call(Object who, Method method, Object... args) {
            int userId = (int) args[0];
            return userId == 0;
        }
    }
}
