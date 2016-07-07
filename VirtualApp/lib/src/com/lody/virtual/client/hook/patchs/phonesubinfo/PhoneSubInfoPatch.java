package com.lody.virtual.client.hook.patchs.phonesubinfo;

import com.android.internal.telephony.IPhoneSubInfo;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookPhoneSubInfoBinder;

import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@Patch({Hook_GetDeviceId.class, Hook_GetDeviceSvn.class, Hook_GetDeviceSvnUsingSubId.class, Hook_GetGroupIdLevel1.class,
		Hook_GetGroupIdLevel1ForSubscriber.class, Hook_GetIccSerialNumber.class,
		Hook_GetIccSerialNumberForSubscriber.class, Hook_GetImeiForSubscriber.class, Hook_GetLine1AlphaTag.class,
		Hook_GetLine1AlphaTagForSubscriber.class, Hook_GetLine1Number.class, Hook_GetLine1NumberForSubscriber.class,
		Hook_GetMsisdn.class, Hook_GetMsisdnForSubscriber.class, Hook_GetNaiForSubscriber.class,
		Hook_GetSubscriberId.class, Hook_GetSubscriberIdForSubscriber.class, Hook_GetVoiceMailAlphaTag.class,
		Hook_GetVoiceMailAlphaTagForSubscriber.class, Hook_GetVoiceMailNumber.class,
		Hook_GetVoiceMailNumberForSubscriber.class,})
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
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService("iphonesubinfo");
	}
}
