package com.lody.virtual.client.hook.patchs.miui.security;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.HookMIUISecurityBinder;

import android.os.IBinder;
import android.os.ServiceManager;

import miui.security.ISecurityManager;

/**
 * @author Lody
 */
@Patch({Hook_ActivityResume.class,})
public class MIUISecurityManagerPatch extends PatchObject<ISecurityManager, HookMIUISecurityBinder> {

	private static final String TAG = MIUISecurityManagerPatch.class.getSimpleName();

	public static boolean needInject() {
		try {
			IBinder binder = ServiceManager.getService(HookMIUISecurityBinder.SECURITY_SERVICE);
			return binder != null && ISecurityManager.Stub.DESCRIPTOR.equals(binder.getInterfaceDescriptor());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected HookMIUISecurityBinder initHookObject() {
		return new HookMIUISecurityBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(HookMIUISecurityBinder.SECURITY_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ReplaceCallingPkgHook("startInterceptSmsBySender"));
		addHook(new ReplaceCallingPkgHook("addAccessControlPass"));
		addHook(new ReplaceCallingPkgHook("checkAccessControlPass"));
		addHook(new ReplaceCallingPkgHook("finishAccessControl"));
		addHook(new ReplaceCallingPkgHook("grantRuntimePermission"));
		addHook(new ReplaceCallingPkgHook("removeAccessControlPass"));
		addHook(new ReplaceCallingPkgHook("removeAccessControlPassAsUser"));
		addHook(new ReplaceLastPkgHook("killNativePackageProcesses"));
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(HookMIUISecurityBinder.SECURITY_SERVICE);
	}
}
