package com.lody.virtual.client.hook.patchs.user;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookUserBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IUserManager;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see IUserManager
 */
@Patch({Hook_GetApplicationRestrictions.class, Hook_GetApplicationRestrictionsForUser.class,
		Hook_GetSerialNumberForUser.class, Hook_GetUserCount.class, Hook_GetUserForSerialNumber.class,
		Hook_GetUserInfo.class, Hook_GetUserProfiles.class, Hook_GetUserRestrictions.class,
		Hook_HasUserRestriction.class, Hook_IsUserRunning.class, Hook_IsUserRunningOrStopping.class,
		Hook_SetApplicationRestrictions.class, Hook_SetRestrictionsChallenge.class,})
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
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.USER_SERVICE);
	}
}
