package com.lody.virtual.helper.component;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Lody
 *
 *
 */
public abstract class BaseService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
