package com.lody.virtual.client.hook.patchs.miui.security;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.ExtraConstants;

import android.content.Intent;

/**
 * @author Lody
 */

public class ActivityResume extends Hook {

	@Override
	public String getName() {
		return "activityResume";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Intent intent = (Intent) args[0];
		if (intent == null) {
			return method.invoke(who, args);
		}
		intent = intent.getParcelableExtra(ExtraConstants.EXTRA_STUB_INTENT);
		if (intent != null) {
			args[0] = intent;
		}
		return super.onHook(who, method, args);
	}
}
