package com.lody.virtual.client.hook.patchs.phonesubinfo;

import android.os.ServiceManager;

import com.android.internal.telephony.IPhoneSubInfo;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.HookPhoneSubInfoBinder;

/**
 * @author Lody
 *
 */
public class PhoneSubInfoPatch extends PatchObject<IPhoneSubInfo, HookPhoneSubInfoBinder> {
	@Override
	protected HookPhoneSubInfoBinder initHookObject() {
		return new HookPhoneSubInfoBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService("iphonesubinfo");
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
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

		//The following method maybe need to fake
		addHook(new ReplaceCallingPkgHook("getDeviceId"));
		addHook(new ReplaceCallingPkgHook("getIccSerialNumber"));

	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService("iphonesubinfo");
	}
}
