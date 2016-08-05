package com.lody.virtual.client.hook.patchs.location;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.ApiLimit;
import com.lody.virtual.client.hook.base.Hook;

import android.app.PendingIntent;
import android.location.ILocationListener;
import android.location.LocationRequest;
import android.os.Build;

/**
 * @author Lody
 *
 * @see android.location.ILocationManager#requestLocationUpdates(LocationRequest,
 *      ILocationListener, PendingIntent, String)
 *
 */
@ApiLimit(start = Build.VERSION_CODES.M)
public class Hook_RequestLocationUpdates extends Hook {
	@Override
	public String getName() {
		return "requestLocationUpdates";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		if (args[0] instanceof LocationRequest) {
			LocationRequest request = (LocationRequest) args[0];
			try {
				request.setHideFromAppOps(false);
			} catch (Throwable e) {
				// Ignore
			}
		}
		return super.beforeHook(who, method, args);
	}
}
