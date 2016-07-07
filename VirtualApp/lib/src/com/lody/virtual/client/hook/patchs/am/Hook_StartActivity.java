package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.lody.virtual.client.core.PatchManager;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.ArrayIndex;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_StartActivity extends Hook<ActivityManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_StartActivity(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "startActivity";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int intentIndex = ArrayIndex.indexOfFirst(args, Intent.class);
		int resultToIndex;
		if (Build.VERSION.SDK_INT <= 15) {
			resultToIndex = 5;
		} else if (Build.VERSION.SDK_INT <= 17) {
			resultToIndex = 3;
		} else if (Build.VERSION.SDK_INT <= 19) {
			resultToIndex = 4;
		} else {
			resultToIndex = 4;
		}
		IBinder resultTo = (IBinder) args[resultToIndex];
		if (!ActivityUtils.replaceIntent(resultTo, args, intentIndex)) {
			return 0;
		}
		if (isAppProcess()) {
			PatchManager.getInstance().checkEnv(HCallbackHook.class);
		}
		return method.invoke(who, args);
	}

}
