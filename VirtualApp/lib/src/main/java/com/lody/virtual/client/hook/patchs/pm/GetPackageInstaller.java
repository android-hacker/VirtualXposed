package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageManager;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Lody
 *
 * @see IPackageManager#getPackageInstaller()
 *
 */

/* package */ class GetPackageInstaller extends Hook {

	@Override
	public String getName() {
		return "getPackageInstaller";
	}

	@Override
	public Object onHook(final Object who, Method method, Object... args) throws Throwable {
		final IPackageInstaller installer = (IPackageInstaller) method.invoke(who, args);

		return Proxy.newProxyInstance(installer.getClass().getClassLoader(), new Class[]{IPackageInstaller.class},
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
