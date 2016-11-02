package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.os.VUserInfo;

import java.lang.reflect.Method;

import mirror.android.content.pm.UserInfo;

/**
 * @author Lody
 */

/*package*/ class GetCurrentUser extends Hook {

	@Override
	public String getName() {
		return "getCurrentUser";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		try {
			return UserInfo.ctor.newInstance(0, "user", VUserInfo.FLAG_PRIMARY);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
