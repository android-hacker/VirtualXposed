package com.lody.virtual.client.hook.patchs.telephony;

import android.content.Context;
import android.os.ServiceManager;

import com.android.internal.telephony.ITelephony;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.HookTelephonyBinder;

/**
 * @author Lody
 *
 *
 * @see ITelephony
 */
public class TelephonyPatch extends PatchObject<ITelephony, HookTelephonyBinder> {

	@Override
	protected HookTelephonyBinder initHookObject() {
		return new HookTelephonyBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.TELEPHONY_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ReplaceCallingPkgHook("getDeviceId"));
		addHook(new ReplaceCallingPkgHook("getNeighboringCellInfo"));
		addHook(new ReplaceCallingPkgHook("getAllCellInfo"));
		addHook(new ReplaceCallingPkgHook("getCellLocation"));
		addHook(new ReplaceCallingPkgHook("isOffhook"));
		addHook(new ReplaceLastPkgHook("getLine1NumberForDisplay"));
		addHook(new ReplaceLastPkgHook("isOffhookForSubscriber"));
		addHook(new ReplaceLastPkgHook("isRingingForSubscriber"));
		addHook(new ReplaceCallingPkgHook("call"));
		addHook(new ReplaceCallingPkgHook("isRinging"));
		addHook(new ReplaceCallingPkgHook("isIdle"));
		addHook(new ReplaceLastPkgHook("isIdleForSubscriber"));
		addHook(new ReplaceCallingPkgHook("isRadioOn"));
		addHook(new ReplaceLastPkgHook("isRadioOnForSubscriber"));
		addHook(new ReplaceLastPkgHook("isSimPinEnabled"));
		addHook(new ReplaceLastPkgHook("getCdmaEriIconIndex"));
		addHook(new ReplaceLastPkgHook("getCdmaEriIconIndexForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getCdmaEriIconMode"));
		addHook(new ReplaceLastPkgHook("getCdmaEriIconModeForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getCdmaEriText"));
		addHook(new ReplaceLastPkgHook("getCdmaEriTextForSubscriber"));
		addHook(new ReplaceLastPkgHook("getNetworkTypeForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getDataNetworkType"));
		addHook(new ReplaceLastPkgHook("getDataNetworkTypeForSubscriber"));
		addHook(new ReplaceLastPkgHook("getVoiceNetworkTypeForSubscriber"));
		addHook(new ReplaceCallingPkgHook("getLteOnCdmaMode"));
		addHook(new ReplaceLastPkgHook("getLteOnCdmaModeForSubscriber"));
		addHook(new ReplaceLastPkgHook("getCalculatedPreferredNetworkType"));
		addHook(new ReplaceLastPkgHook("getPcscfAddress"));
		addHook(new ReplaceLastPkgHook("getLine1AlphaTagForDisplay"));
		addHook(new ReplaceCallingPkgHook("getMergedSubscriberIds"));
		addHook(new ReplaceLastPkgHook("getRadioAccessFamily"));
		addHook(new ReplaceCallingPkgHook("isVideoCallingEnabled"));
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.TELEPHONY_SERVICE);
	}
}
