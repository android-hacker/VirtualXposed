package com.lody.virtual.client.hook.proxies.window.session;

import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.MethodInvocationProxy;
import com.lody.virtual.client.hook.base.MethodInvocationStub;

/**
 * @author Lody
 */
public class WindowSessionPatch extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {

	public WindowSessionPatch(IInterface session) {
		super(new MethodInvocationStub<>(session));
	}

	@Override
	public void onBindMethods() {
		addMethodProxy(new BaseMethodProxy("add"));
		addMethodProxy(new BaseMethodProxy("addToDisplay"));
		addMethodProxy(new BaseMethodProxy("addToDisplayWithoutInputChannel"));
		addMethodProxy(new BaseMethodProxy("addWithoutInputChannel"));
		addMethodProxy(new BaseMethodProxy("relayout"));

		// http://aospxref.com/android-11.0.0_r21/xref/frameworks/base/core/java/android/view/IWindowSession.aidl#51
		if (Build.VERSION.SDK_INT >= 30) {
			addMethodProxy(new BaseMethodProxy("addToDisplayAsUser"));
			addMethodProxy(new BaseMethodProxy("grantInputChannel"));
		}
	}


	@Override
	public void inject() throws Throwable {
		// <EMPTY>
	}

	@Override
	public boolean isEnvBad() {
		return getInvocationStub().getProxyInterface() != null;
	}
}
