package com.lody.virtual.client.hook.patchs.restriction;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.binders.RestrictionBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RestrictionPatch extends PatchDelegate<RestrictionBinderDelegate> {

	@Override
	protected RestrictionBinderDelegate createHookDelegate() {
		return new RestrictionBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.RESTRICTIONS_SERVICE);

	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceCallingPkgHook("getApplicationRestrictions"));
		addHook(new ReplaceCallingPkgHook("notifyPermissionResponse"));
		addHook(new ReplaceCallingPkgHook("requestPermission"));
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call(Context.RESTRICTIONS_SERVICE) != getHookDelegate();
	}

}
