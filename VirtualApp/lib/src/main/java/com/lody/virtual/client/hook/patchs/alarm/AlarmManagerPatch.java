package com.lody.virtual.client.hook.patchs.alarm;

import android.content.Context;
import android.os.Build;
import android.os.WorkSource;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

import mirror.android.app.IAlarmManager;

/**
 * @author Lody
 *
 * @see android.app.AlarmManager
 */
public class AlarmManagerPatch extends PatchBinderDelegate {

	public AlarmManagerPatch() {
		super(IAlarmManager.Stub.TYPE, Context.ALARM_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new Set());
		addHook(new SetTime());
		addHook(new SetTimeZone());
	}

	private static class SetTimeZone extends Hook {
		@Override
		public String getName() {
			return "setTimeZone";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			return null;
		}
	}

	private static class SetTime extends Hook {
		@Override
		public String getName() {
			return "setTime";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				return false;
			}
			return null;
		}
	}

	private static class Set extends Hook {

        @Override
        public String getName() {
            return "set";
        }

        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && args[0] instanceof String) {
				args[0] = getHostPkg();
			}
            int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
            if (index >= 0) {
                args[index] = null;
            }
            return true;
        }
    }
}
