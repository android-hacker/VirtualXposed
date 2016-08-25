package com.lody.virtual.client.hook.patchs.user;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.hook.binders.UserBinderDelegate;

import java.util.Collections;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class UserManagerPatch extends PatchDelegate<UserBinderDelegate> {

	@Override
	protected UserBinderDelegate createHookDelegate() {
		return new UserBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.USER_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceCallingPkgHook("setApplicationRestrictions"));
		addHook(new ReplaceCallingPkgHook("getApplicationRestrictions"));
		addHook(new ReplaceCallingPkgHook("getApplicationRestrictionsForUser"));
		addHook(new ResultStaticHook("getProfileParent", null));
		addHook(new ResultStaticHook("getUserIcon", null));
		addHook(new ResultStaticHook("getUserInfo", null));
		addHook(new ResultStaticHook("getDefaultGuestRestrictions", null));
		addHook(new ResultStaticHook("setDefaultGuestRestrictions", null));
		addHook(new ResultStaticHook("removeRestrictions", null));
		addHook(new ResultStaticHook("getUsers", Collections.EMPTY_LIST));
		addHook(new ResultStaticHook("createUser", null));
		addHook(new ResultStaticHook("createProfileForUser", null));
		addHook(new ResultStaticHook("getProfiles", Collections.EMPTY_LIST));
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call(Context.USER_SERVICE);
	}
}
