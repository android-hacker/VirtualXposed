package com.lody.virtual.client.hook.patchs.user;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IUserManager;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.hook.binders.HookUserBinder;

import java.util.Collections;

/**
 * @author Lody
 *
 *
 * @see IUserManager
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class UserManagerPatch extends PatchObject<IUserManager, HookUserBinder> {

	@Override
	protected HookUserBinder initHookObject() {
		return new HookUserBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.USER_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
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
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.USER_SERVICE);
	}
}
