package com.lody.virtual.client.hook.patchs.pm;

import android.os.Process;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class GetPackageUid extends Hook {

	@Override
	public String getName() {
		return "getPackageUid";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return Process.myUid();
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
