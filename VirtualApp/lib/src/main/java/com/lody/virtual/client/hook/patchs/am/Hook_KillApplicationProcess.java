package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#killApplicationProcess(String, int)
 */
/* package */ class Hook_KillApplicationProcess extends Hook {
	{
		replaceUid(1);
	}

	@Override
	public String getName() {
		return "killApplicationProcess";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args.length > 1 && args[0] instanceof String && args[1] instanceof Integer) {
			String procName = (String) args[0];
			int uid = (int) args[1];
			VActivityManager.getInstance().killApplicationProcess(procName, uid);
			return 0;
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
