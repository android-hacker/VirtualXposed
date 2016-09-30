package com.lody.virtual.client.hook.patchs.net_management;

import android.annotation.TargetApi;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceUidHook;
import com.lody.virtual.client.hook.binders.NetworkManagementBinderDelegate;

import mirror.android.os.ServiceManager;

@TargetApi(Build.VERSION_CODES.M)
public class NetworkManagementPatch extends PatchDelegate<NetworkManagementBinderDelegate> {

	@Override
	protected NetworkManagementBinderDelegate createHookDelegate() {
		return new NetworkManagementBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(NetworkManagementBinderDelegate.NETWORKMANAGEMENT_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceUidHook("setUidCleartextNetworkPolicy", 0));
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call(NetworkManagementBinderDelegate.NETWORKMANAGEMENT_SERVICE);
	}
}
