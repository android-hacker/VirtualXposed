package com.lody.virtual.client.hook.base;

import android.content.Context;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppSetting;

import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * @author Lody
 *
 */
public abstract class Hook {

	private boolean enable = true;
	private LinkedList<SubModule> subModules;

	public void addSubModule(SubModule module) {
		if (subModules == null) {
			subModules = new LinkedList<>();
		}
		subModules.offer(module);
	}

	public abstract class SubModule {
		public abstract void apply(Object who, Method method, Object... args);
	}

	/**
	 * @return Hook的方法名
	 */
	public abstract String getName();

	public boolean beforeHook(Object who, Method method, Object... args) {
		if (subModules != null) {
			for (SubModule subModule : subModules) {
				subModule.apply(who, method, args);
			}
		}
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

	protected final AppSetting findAppInfo(String pkg) {
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

	protected final boolean isSystemProcess() {
		return isMainProcess() || isServiceProcess();
	}

	protected final PackageManager getUnhookPM() {
		return VirtualCore.getCore().getUnHookPackageManager();
	}

	@Override
	public String toString() {
		return "Hook${ " + getName() + " }";
	}
}
