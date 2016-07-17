package com.lody.virtual.client.hook.patchs.am;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Bundle;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.ExtraConstants;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.app.ActivityManagerNative#getIntentSender(int, String, IBinder,
 *      String, int, Intent[], String[], int, Bundle, int)
 */
/* package */ class Hook_GetIntentSender extends Hook<ActivityManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetIntentSender(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getIntentSender";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		if (args[1] instanceof String && isAppPkg((String) args[1])) {
			args[1] = getHostPkg();
		}
//		if (true) {
//			return method.invoke(who, args);
//		}
		Object intentOrIntents = args[5];
		int flags = (int) args[0];
		boolean replaced = false;
		if (intentOrIntents instanceof Intent) {
			Intent intent = (Intent) args[5];
			Intent proxyIntent = redirectIntentSender(flags, intent);
			if (proxyIntent != null) {
				args[5] = proxyIntent;
				replaced = true;
			}
		} else if (intentOrIntents instanceof Intent[]) {
			Intent[] intents = (Intent[]) args[5];
			int N = intents.length;
			while (N-- > 0) {
				Intent intent = intents[N];
				Intent proxyIntent = redirectIntentSender(flags, intent);
				if (proxyIntent != null) {
					intents[N] = proxyIntent;
					replaced = true;
				}
			}
		}
		if (replaced) {
			if (args.length > 7 && args[7] instanceof Integer) {
			//	args[7] = PendingIntent.FLAG_UPDATE_CURRENT;
			}
			args[0] = ActivityManager.INTENT_SENDER_BROADCAST;
		}
		return method.invoke(who, args);
	}

	private Intent redirectIntentSender(int flags, Intent intent) {
		ComponentInfo componentInfo = null;
		switch (flags) {
			case ActivityManager.INTENT_SENDER_ACTIVITY :
				componentInfo = VirtualCore.getCore().resolveActivityInfo(intent);
				break;
			case ActivityManager.INTENT_SENDER_SERVICE :
				componentInfo = VirtualCore.getCore().resolveServiceInfo(intent);
				break;
			case ActivityManager.INTENT_SENDER_BROADCAST :
				// INTENT_SENDER_BROADCAST不需要处理
				break;
		}
		if (componentInfo != null && isAppPkg(componentInfo.packageName)) {
			Intent newIntent = new Intent(Constants.ACTION_DELEGATE_PENDING_INTENT);
			newIntent.putExtra(ExtraConstants.EXTRA_INTENT, intent);
			newIntent.putExtra(ExtraConstants.EXTRA_INTENT_TYPE, ExtraConstants.TYPE_INTENT_SENDER);
			newIntent.putExtra(ExtraConstants.EXTRA_FLAGS, flags);
			return newIntent;
		}
		return null;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
