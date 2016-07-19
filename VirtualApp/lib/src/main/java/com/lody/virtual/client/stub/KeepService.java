package com.lody.virtual.client.stub;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;

import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.component.BaseService;

/**
 * @author Lody
 *
 *
 *         与ServiceContentProvider同进程，用于维持进程不死。
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
		if (intent != null) {
			int action = intent.getIntExtra(ExtraConstants.EXTRA_WHAT, -1);
			if (action == ExtraConstants.WHAT_PENDING_INTENT) {
				handlePendingIntent(intent);
			}
		}
		return START_STICKY;
	}

	private void handlePendingIntent(Intent intent) {
		int flags = intent.getIntExtra(ExtraConstants.EXTRA_FLAGS, -1);
		Intent originIntent = intent.getParcelableExtra(ExtraConstants.EXTRA_INTENT);
		if (originIntent == null) {
			return;
		}
		switch (flags) {
			case ActivityManager.INTENT_SENDER_ACTIVITY:
			{
				originIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					startActivity(originIntent);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				break;
			}
			case ActivityManager.INTENT_SENDER_SERVICE:
			{
				try {
					startService(originIntent);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				break;
			}
		}

	}


}
