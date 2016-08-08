package com.lody.virtual.client.hook.base;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppInfo;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * @author Lody
 *
 */
public abstract class Hook {

	private boolean enable = true;

	/**
	 * @return Hook的方法名
	 */
	public abstract String getName();

	public boolean beforeHook(Object who, Method method, Object... args) {
		return true;
	}

	/**
	 * Hook回调
	 */
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return method.invoke(who, args);
	}

	public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
		return result;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public final boolean isAppPkg(String pkg) {
		return VirtualCore.getCore().isAppInstalled(pkg);
	}

	public final String getHostPkg() {
		return VirtualCore.getCore().getHostPkg();
	}

	protected final PackageManager getPM() {
		return VirtualCore.getPM();
	}

	protected final Context getHostContext() {
		return VirtualCore.getCore().getContext();
	}

	protected final AppInfo findAppInfo(String pkg) {
		return VirtualCore.getCore().findApp(pkg);
	}

	protected final boolean isAppProcess() {
		return VirtualCore.getCore().isVAppProcess();
	}

	protected final boolean isServiceProcess() {
		return VirtualCore.getCore().isServiceProcess();
	}

	protected final boolean isMainProcess() {
		return VirtualCore.getCore().isMainProcess();
	}

	protected final PackageManager getUnhookPM() {
		return VirtualCore.getCore().getUnHookPackageManager();
	}
}
