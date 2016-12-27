package com.lody.virtual.client.hook.patchs.phonesubinfo;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;

import mirror.com.android.internal.telephony.IPhoneSubInfo;

/**
 * @author Lody
 */
@Patch({GetDeviceId.class, GetDeviceIdForSubscriber.class})
public class PhoneSubInfoPatch extends PatchBinderDelegate {
	public PhoneSubInfoPatch() {
		super(IPhoneSubInfo.Stub.TYPE, "iphonesubinfo");
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
}
