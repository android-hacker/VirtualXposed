package com.lody.virtual.client.hook.proxies.display;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.MethodInvocationProxy;
import com.lody.virtual.client.hook.base.MethodInvocationStub;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.android.hardware.display.DisplayManagerGlobal;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class DisplayStub extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {
	public DisplayStub() {
		super(new MethodInvocationStub<IInterface>(
				DisplayManagerGlobal.mDm.get(DisplayManagerGlobal.getInstance.call())));
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceCallingPkgMethodProxy("createVirtualDisplay"));
	}

	@Override
	public void inject() throws Throwable {
		Object dmg = DisplayManagerGlobal.getInstance.call();
		DisplayManagerGlobal.mDm.set(dmg, getInvocationStub().getProxyInterface());
	}

	@Override
	public boolean isEnvBad() {
		Object dmg = DisplayManagerGlobal.getInstance.call();
		IInterface mDm = DisplayManagerGlobal.mDm.get(dmg);
		return mDm != getInvocationStub().getProxyInterface();
	}
}
