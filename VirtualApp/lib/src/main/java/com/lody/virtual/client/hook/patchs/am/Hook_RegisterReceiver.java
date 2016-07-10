package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#registerReceiver(IApplicationThread,
 *      String, IIntentReceiver, IntentFilter, String, int)
 */
/* package */ class Hook_RegisterReceiver extends Hook<ActivityManagerPatch> {

	private WeakHashMap<Object, IIntentReceiver.Stub> mProxyIIntentReceiver = new WeakHashMap<>();

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_RegisterReceiver(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "registerReceiver";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		HookUtils.replaceFirstAppPkg(args);

		final int indexOfRequiredPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
				? 4
				: 3;
		if (args != null && args.length > indexOfRequiredPermission
				&& args[indexOfRequiredPermission] instanceof String) {
			args[indexOfRequiredPermission] = VirtualCore.getPermissionBroadcast();
		}
		final int indexOfIIntentReceiver = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 ? 2 : 1;
		if (args != null && args.length > indexOfIIntentReceiver
				&& IIntentReceiver.class.isInstance(args[indexOfIIntentReceiver])) {
			final IIntentReceiver old = (IIntentReceiver) args[indexOfIIntentReceiver];
			IIntentReceiver.Stub proxyIIntentReceiver = mProxyIIntentReceiver.get(old);
			if (proxyIIntentReceiver == null) {
				proxyIIntentReceiver = new IIntentReceiver.Stub() {
					@Override
					public void performReceive(Intent intent, int resultCode, String data, Bundle extras,
							boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
						try {
							String action = intent.getAction();
							ComponentName oldComponent = VirtualCore.getOriginComponentName(action);
							if (oldComponent != null) {
								intent.setComponent(oldComponent);
								intent.setAction(null);
							}

							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
								old.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
							} else {
								Method performReceive = old.getClass().getDeclaredMethod("performReceive", Intent.class,
										int.class, String.class, Bundle.class, boolean.class, boolean.class);
								performReceive.setAccessible(true);
								performReceive.invoke(old, intent, resultCode, data, extras, ordered, sticky);
							}
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					// @Override
					public void performReceive(Intent intent, int resultCode, String data, Bundle extras,
							boolean ordered, boolean sticky) throws android.os.RemoteException {
						this.performReceive(intent, resultCode, data, extras, ordered, sticky, 0);
					}
				};
				mProxyIIntentReceiver.put(old, proxyIIntentReceiver);
			}
			args[indexOfIIntentReceiver] = proxyIIntentReceiver;
		}

		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
