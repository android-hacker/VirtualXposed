package com.lody.virtual.client.hook.patchs.telephony;

import android.content.Context;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.TelephonyBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 */
@Patch({Hook_GetDeviceId.class})
public class TelephonyPatch extends PatchDelegate<TelephonyBinderDelegate> {

	@Override
	protected TelephonyBinderDelegate createHookDelegate() {
		return new TelephonyBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.TELEPHONY_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		//addHook(new ReplaceCallingPkgHook("getDeviceId"));
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
		return getHookDelegate() != ServiceManager.getService.call(Context.TELEPHONY_SERVICE);
	}
}
