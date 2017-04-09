package com.lody.virtual.client.hook.proxies.alarm;

import android.content.Context;
import android.os.Build;
import android.os.WorkSource;

import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

import mirror.android.app.IAlarmManager;

/**
 * @author Lody
 *
 * @see android.app.AlarmManager
 */
public class AlarmManagerStub extends BinderInvocationProxy {

	public AlarmManagerStub() {
		super(IAlarmManager.Stub.asInterface, Context.ALARM_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new Set());
		addMethodProxy(new SetTime());
		addMethodProxy(new SetTimeZone());
	}

	private static class SetTimeZone extends MethodProxy {
		@Override
		public String getMethodName() {
			return "setTimeZone";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			return null;
		}
	}

	private static class SetTime extends MethodProxy {
		@Override
		public String getMethodName() {
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

	private static class Set extends MethodProxy {

        @Override
        public String getMethodName() {
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
