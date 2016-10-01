package com.lody.virtual.client.hook.patchs.am;

import android.annotation.TargetApi;
import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author prife
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/* package */ class SetTaskDescription extends Hook {
	@Override
	public String getName() {
		return "setTaskDescription";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}