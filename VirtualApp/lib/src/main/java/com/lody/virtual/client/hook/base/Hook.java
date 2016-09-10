package com.lody.virtual.client.hook.base;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.lc.interceptor.client.hook.base.InterceptorHook;
import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
public abstract class Hook {

	private boolean enable = true;

	private InterceptorHook interceptHook;

	public void setInterceptHook(InterceptorHook interceptHook) {
		this.interceptHook = interceptHook;
	}

	private boolean isAvailableHook(Hook hook){
        return hook != null && hook.isEnable();
    }

	private boolean isAvailableInnerOnHook(InterceptorHook hook){
        return isAvailableHook(hook) && hook.isOnHookEnabled();
    }


	public abstract String getName();

	public boolean beforeCall(Object who, Method method, Object... args) {
            if (isAvailableHook(interceptHook)) {
                interceptHook.beforeCall(who, method, args);
            }
		return true;
	}

	public Object call(Object who, Method method, Object... args) throws Throwable {
        Log.e("Hook","Name:"+method.getName());
        if (isAvailableInnerOnHook(interceptHook)) {
            Object o = interceptHook.call(who, method, args);
            if(interceptHook.isOnHookConsumed()) {
                return o;
            }else {
                return method.invoke(who,args);
            }
        }
        return method.invoke(who, args);
	}


	public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
        if (isAvailableHook(interceptHook)) {
            interceptHook.afterCall(who, method, args, result);
        }
		return result;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public final boolean isAppPkg(String pkg) {
		return VirtualCore.get().isAppInstalled(pkg);
	}

	public final String getHostPkg() {
		return VirtualCore.get().getHostPkg();
	}

	protected final PackageManager getPM() {
		return VirtualCore.getPM();
	}

	protected final Context getHostContext() {
		return VirtualCore.get().getContext();
	}

	protected final boolean isAppProcess() {
		return VirtualCore.get().isVAppProcess();
	}

	protected final boolean isServiceProcess() {
		return VirtualCore.get().isServiceProcess();
	}

	protected final boolean isMainProcess() {
		return VirtualCore.get().isMainProcess();
	}


	protected final int getBaseVUid() {
		return VClientImpl.getClient().getBaseVUid();
	}

	protected final int getRealUid() {
		return VirtualCore.get().myUid();
	}


	@Override
	public String toString() {
		return "Hook${ " + getName() + " }";
	}
}
