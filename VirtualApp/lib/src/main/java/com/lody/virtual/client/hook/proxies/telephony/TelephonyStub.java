package com.lody.virtual.client.hook.proxies.telephony;

import android.content.Context;

import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;

import mirror.com.android.internal.telephony.ITelephony;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
public class TelephonyStub extends BinderInvocationProxy {

	public TelephonyStub() {
		super(ITelephony.Stub.asInterface, Context.TELEPHONY_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceCallingPkgMethodProxy("isOffhook"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getLine1NumberForDisplay"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("isOffhookForSubscriber"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("isRingingForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("call"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("isRinging"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("isIdle"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("isIdleForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("isRadioOn"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("isRadioOnForSubscriber"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("isSimPinEnabled"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getCdmaEriIconIndex"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getCdmaEriIconIndexForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getCdmaEriIconMode"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getCdmaEriIconModeForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getCdmaEriText"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getCdmaEriTextForSubscriber"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getNetworkTypeForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getDataNetworkType"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getDataNetworkTypeForSubscriber"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getVoiceNetworkTypeForSubscriber"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getLteOnCdmaMode"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getLteOnCdmaModeForSubscriber"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getCalculatedPreferredNetworkType"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getPcscfAddress"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getLine1AlphaTagForDisplay"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getMergedSubscriberIds"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("getRadioAccessFamily"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("isVideoCallingEnabled"));
	}
}
