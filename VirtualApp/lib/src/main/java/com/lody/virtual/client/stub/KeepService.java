package com.lody.virtual.client.stub;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;

import com.lody.virtual.helper.component.BaseService;

/**
 * @author Lody
 *
 */
public class KeepService extends BaseService {

	public static void startup(Context context) {
		context.startService(new Intent(context, KeepService.class));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		startup(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			Notification notification = new Notification();
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			startForeground(0, notification);
		} catch (Throwable e) {
			// Ignore
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}


}
