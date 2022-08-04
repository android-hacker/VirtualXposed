package com.lody.virtual.client.hook.base;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VirtualLocationManager;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.VDeviceInfo;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
public abstract class MethodProxy {

    private boolean enable = true;
    private LogInvocation.Condition mInvocationLoggingCondition = LogInvocation.Condition.NEVER; // Inherit

    public MethodProxy() {
        LogInvocation loggingAnnotation = getClass().getAnnotation(LogInvocation.class);
        if (loggingAnnotation != null) {
            this.mInvocationLoggingCondition = loggingAnnotation.value();
        }
    }

    public static String getHostPkg() {
        return VirtualCore.get().getHostPkg();
    }

    public static String getAppPkg() {
        return VClientImpl.get().getCurrentPackage();
    }

    protected static Context getHostContext() {
        return VirtualCore.get().getContext();
    }

    protected static boolean isAppProcess() {
        return VirtualCore.get().isVAppProcess();
    }

    protected static boolean isServerProcess() {
        return VirtualCore.get().isServerProcess();
    }

    protected static boolean isMainProcess() {
        return VirtualCore.get().isMainProcess();
    }

    protected static int getVUid() {
        return VClientImpl.get().getVUid();
    }

    public static int getAppUserId() {
        return VUserHandle.getUserId(getVUid());
    }

    protected static int getBaseVUid() {
        return VClientImpl.get().getBaseVUid();
    }

    protected static int getRealUid() {
        return VirtualCore.get().myUid();
    }

    protected static VDeviceInfo getDeviceInfo() {
        return VClientImpl.get().getDeviceInfo();
    }

    protected static boolean isFakeLocationEnable() {
        return VirtualLocationManager.get().getMode(VUserHandle.myUserId(), VClientImpl.get().getCurrentPackage()) != 0;
    }

    public static boolean isVisiblePackage(ApplicationInfo info) {
        return getHostPkg().equals(info.packageName)
                || ComponentUtils.isSystemApp(info)
                || VirtualCore.get().isOutsidePackageVisible(info.packageName);
    }

    public abstract String getMethodName();

    public boolean beforeCall(Object who, Method method, Object... args) {
        return true;
    }

    public Object call(Object who, Method method, Object... args) throws Throwable {
        return method.invoke(who, args);
    }

    public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
        return result;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public LogInvocation.Condition getInvocationLoggingCondition() {
        return mInvocationLoggingCondition;
    }

    public void setInvocationloggingCondition(LogInvocation.Condition invocationLoggingCondition) {
        mInvocationLoggingCondition = invocationLoggingCondition;
    }

    public boolean isAppPkg(String pkg) {
        return VirtualCore.get().isAppInstalled(pkg);
    }

    protected PackageManager getPM() {
        return VirtualCore.getPM();
    }

    @Override
    public String toString() {
        return "Method : " + getMethodName();
    }
}
