package com.lody.virtual.client.hook.patchs.restriction;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.IRestrictionsManager;
import android.os.Build;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.binders.HookRestrictionBinder;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RestrictionPatch extends PatchObject<IRestrictionsManager, HookRestrictionBinder> {

	@Override
	protected HookRestrictionBinder initHookObject() {
		return new HookRestrictionBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.RESTRICTIONS_SERVICE);

	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ReplaceCallingPkgHook("getApplicationRestrictions"));
		addHook(new ReplaceCallingPkgHook("notifyPermissionResponse"));
		addHook(new ReplaceCallingPkgHook("requestPermission"));
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.RESTRICTIONS_SERVICE) != getHookObject();
	}

}
