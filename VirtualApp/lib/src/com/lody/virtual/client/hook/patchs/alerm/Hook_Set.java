package com.lody.virtual.client.hook.patchs.alerm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.ArrayIndex;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.WorkSource;

/**
 * @author Lody
 *
 *
 * @see android.app.IAlarmManager#set(int, long, long, long, int, PendingIntent,
 *      WorkSource, AlarmManager.AlarmClockInfo)
 *
 *      原型： public void set(int type, long triggerAtTime, long windowLength,
 *      long interval, int flags, PendingIntent operation, WorkSource
 *      workSource, AlarmClockInfo alarmClock)
 */
/* package */ class Hook_Set extends Hook<AlarmManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_Set(AlarmManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "set";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int index = ArrayIndex.indexOfFirst(args, WorkSource.class);
		if (index >= 0) {
			args[index] = null;
		}
		return method.invoke(who, args);
	}
}
