package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Lody
 *
 */
/* package */ class QueryIntentReceivers extends Hook {

	@Override
	public String getName() {
		return "queryIntentReceivers";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		int userId = VUserHandle.myUserId();
		List<ResolveInfo> appResult =  VPackageManager.get().queryIntentReceivers((Intent) args[0], (String) args[1],
				(Integer) args[2], userId);
		if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
			return ParceledListSliceCompat.create(appResult);
		}
		return appResult;
	}
}
