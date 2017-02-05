package com.lody.virtual.client.hook.base;

import android.content.Context;
import android.content.pm.PackageManager;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
public abstract class Hook {
	
	private boolean enable = true;

	public abstract String getName();

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

	protected final boolean isServerProcess() {
		return VirtualCore.get().isServerProcess();
	}

	protected final boolean isMainProcess() {
		return VirtualCore.get().isMainProcess();
	}

	protected final int getVUid() {
		return VClientImpl.get().getVUid();
	}

	protected final int getBaseVUid() {
		return VClientImpl.get().getBaseVUid();
	}

	protected final int getRealUid() {
		return VirtualCore.get().myUid();
	}


	@Override
	public String toString() {
		return "Hook${ " + getName() + " }";
	}
}
