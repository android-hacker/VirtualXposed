package com.lody.virtual.client.hook.patchs.phonesubinfo;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.PhoneSubInfoBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@Patch({Hook_GetDeviceId.class})
public class PhoneSubInfoPatch extends PatchDelegate<PhoneSubInfoBinderDelegate> {
	@Override
	protected PhoneSubInfoBinderDelegate createHookDelegate() {
		return new PhoneSubInfoBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService("iphonesubinfo");
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceLastPkgHook("getNaiForSubscriber"));
		addHook(new ReplaceLastPkgHook("getImeiForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getDeviceSvn"));
		addHook(new ReplaceLastPkgHook("getDeviceSvnUsingSubId"));
		addHook(new ReplaceCallingPkgHook("getSubscriberId"));
		addHook(new ReplaceLastPkgHook("getSubscriberIdForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getGroupIdLevel1"));
		addHook(new ReplaceLastPkgHook("getGroupIdLevel1ForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getLine1Number"));
		addHook(new ReplaceLastPkgHook("getLine1NumberForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getLine1AlphaTag"));
		addHook(new ReplaceLastPkgHook("getLine1AlphaTagForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getMsisdn"));
		addHook(new ReplaceLastPkgHook("getMsisdnForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getVoiceMailNumber"));
		addHook(new ReplaceLastPkgHook("getVoiceMailNumberForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getVoiceMailAlphaTag"));
		addHook(new ReplaceLastPkgHook("getVoiceMailAlphaTagForSubscriber"));
		// The following method maybe need to fake
		//addHook(new ReplaceCallingPkgHook("getDeviceId"));
		addHook(new ReplaceCallingPkgHook("getIccSerialNumber"));
		addHook(new ReplaceLastPkgHook("getIccSerialNumberForSubscriber"));

	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call("iphonesubinfo");
	}
}
