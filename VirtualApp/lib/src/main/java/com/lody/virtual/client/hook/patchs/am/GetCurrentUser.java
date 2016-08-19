package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.content.pm.UserInfo;

/**
 * @author Lody
 */

public class GetCurrentUser extends Hook {

	@Override
	public String getName() {
		return "getCurrentUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		try {
			return new UserInfo(0, "user", UserInfo.FLAG_PRIMARY);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
