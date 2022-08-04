package com.lody.virtual.client.hook.proxies.telephony;

import android.telephony.PhoneStateListener;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.ReplaceSequencePkgMethodProxy;

import java.lang.reflect.Method;

import mirror.com.android.internal.telephony.ITelephonyRegistry;

public class TelephonyRegistryStub extends BinderInvocationProxy {

	public TelephonyRegistryStub() {
		super(ITelephonyRegistry.Stub.asInterface, "telephony.registry");
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceCallingPkgMethodProxy("listen"));
		addMethodProxy(new ReplaceSequencePkgMethodProxy("listenForSubscriber", 1) {
			@Override
			public boolean beforeCall(Object who, Method method, Object... args) {
				if (android.os.Build.VERSION.SDK_INT >= 17) {
					if (isFakeLocationEnable()) {
						for (int i = args.length - 1; i > 0; i--) {
							if (args[i] instanceof Integer) {
								int events = (Integer) args[i];
								events ^= PhoneStateListener.LISTEN_CELL_INFO;
								events ^= PhoneStateListener.LISTEN_CELL_LOCATION;
								args[i] = events;
								break;
							}
						}
					}
				}
				return super.beforeCall(who, method, args);
			}
		});
	}
}
