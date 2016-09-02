package com.lody.virtual.client.hook.patchs.pm;


import android.os.IInterface;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Lody
 *
 *
 */

/* package */ class GetPackageInstaller extends Hook {

	@Override
	public String getName() {
		return "getPackageInstaller";
	}

	@Override
	public Object call(final Object who, Method method, Object... args) throws Throwable {
		final IInterface installer = (IInterface) method.invoke(who, args);

		return Proxy.newProxyInstance(installer.getClass().getClassLoader(), installer.getClass().getInterfaces(),
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						String name = method.getName();
						if (name.equals("getMySessions")) {
							HookUtils.replaceFirstAppPkg(args);
						} else if (name.equals("createSession")) {
							HookUtils.replaceFirstAppPkg(args);
						}
						return method.invoke(installer, args);
					}
				});
	}
}
