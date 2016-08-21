package com.lody.virtual.client.hook.patchs.miui.security;

import android.content.Intent;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

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
		intent = intent.getParcelableExtra("_VA_|_stub_intent_");
		if (intent != null) {
			args[0] = intent;
		}
		return super.onHook(who, method, args);
	}
}
