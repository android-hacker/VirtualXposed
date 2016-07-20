package com.lody.virtual.client.hook.base;

import android.content.Context;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppInfo;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
public abstract class Hook<T extends PatchObject> {

	private T patchObject;

	private boolean enable = true;

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook(T patchObject) {
		this.patchObject = patchObject;
	}

	/**
	 * @return Hook的方法名
	 */
	public abstract String getName();

	/**
	 * Hook回调
	 */
	public abstract Object onHook(Object who, Method method, Object... args) throws Throwable;

	public PatchObject getPatchObject() {
		return patchObject;
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

	public final PackageManager getPM() {
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
