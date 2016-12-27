package com.lody.virtual.client.hook.patchs.restriction;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;

import mirror.android.content.IRestrictionsManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)

public class RestrictionPatch extends PatchBinderDelegate {
	public RestrictionPatch() {
		super(IRestrictionsManager.Stub.TYPE, Context.RESTRICTIONS_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceCallingPkgHook("getApplicationRestrictions"));
		addHook(new ReplaceCallingPkgHook("notifyPermissionResponse"));
		addHook(new ReplaceCallingPkgHook("requestPermission"));
	}
}
