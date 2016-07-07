package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.local.LocalServiceManager;
import com.lody.virtual.client.hook.base.Hook;

import android.app.Notification;
import android.content.ComponentName;
import android.os.IBinder;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#setServiceForeground(ComponentName,
 *      IBinder, int, Notification, boolean)
 */
/* package */ class Hook_SetServiceForeground extends Hook<ActivityManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_SetServiceForeground(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "setServiceForeground";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		try {
			ComponentName componentName = (ComponentName) args[0];
			IBinder token = (IBinder) args[1];
			int id = (int) args[2];
			Notification notification = (Notification) args[3];
			boolean keep = (boolean) args[4];
			LocalServiceManager.getInstance().setServiceForeground(componentName, token, id, notification, keep);
			return 0;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
