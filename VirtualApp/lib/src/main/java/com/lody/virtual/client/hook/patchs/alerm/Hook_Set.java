package com.lody.virtual.client.hook.patchs.alerm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.WorkSource;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

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
/* package */ class Hook_Set extends Hook {

	@Override
	public String getName() {
		return "set";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
		if (index >= 0) {
			args[index] = null;
		}
		return true;
	}
}
