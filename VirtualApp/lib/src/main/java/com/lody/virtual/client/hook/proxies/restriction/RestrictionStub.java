package com.lody.virtual.client.hook.proxies.restriction;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.android.content.IRestrictionsManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)

public class RestrictionStub extends BinderInvocationProxy {
	public RestrictionStub() {
		super(IRestrictionsManager.Stub.asInterface, Context.RESTRICTIONS_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceCallingPkgMethodProxy("getApplicationRestrictions"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("notifyPermissionResponse"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("requestPermission"));
	}
}
