package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
/* package */ class GetPermissionFlags extends Hook {

	@Override
	public String getName() {
		return "getPermissionFlags";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		// TODO
		return method.invoke(who, args);
	}

}
