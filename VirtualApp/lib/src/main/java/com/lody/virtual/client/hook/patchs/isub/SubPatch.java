package com.lody.virtual.client.hook.patchs.isub;

import com.android.internal.telephony.ISub;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.HookSubBinder;

import android.os.ServiceManager;

/**
 * @author Lody
 */

public class SubPatch extends PatchObject<ISub, HookSubBinder> {
	@Override
	protected HookSubBinder initHookObject() {
		return new HookSubBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(HookSubBinder.SERVICE_NAME);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ReplaceCallingPkgHook("getAllSubInfoList"));
		addHook(new ReplaceCallingPkgHook("getAllSubInfoCount"));
		addHook(new ReplaceLastPkgHook("getActiveSubscriptionInfo"));
		addHook(new ReplaceLastPkgHook("getActiveSubscriptionInfoForIccId"));
		addHook(new ReplaceLastPkgHook("getActiveSubscriptionInfoForSimSlotIndex"));
		addHook(new ReplaceLastPkgHook("getActiveSubscriptionInfoList"));
		addHook(new ReplaceLastPkgHook("getActiveSubInfoCount"));
		addHook(new ReplaceLastPkgHook("getSubscriptionProperty"));
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(HookSubBinder.SERVICE_NAME) != getHookObject();
	}
}
