package com.lody.virtual.client.hook.patchs.user;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ResultStaticHook;

import java.util.Collections;

import mirror.android.os.IUserManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class UserManagerPatch extends PatchBinderDelegate {

	public UserManagerPatch() {
		super(IUserManager.Stub.TYPE, Context.USER_SERVICE);
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
}
