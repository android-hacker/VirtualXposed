package com.lody.virtual.client.hook.patchs.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceUidHook;

import mirror.android.os.INetworkManagementService;

@TargetApi(Build.VERSION_CODES.M)
public class
NetworkManagementPatch extends PatchBinderDelegate {

	public NetworkManagementPatch() {
		super(INetworkManagementService.Stub.TYPE, "network_management");
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceUidHook("setUidCleartextNetworkPolicy", 0));
	}
}
