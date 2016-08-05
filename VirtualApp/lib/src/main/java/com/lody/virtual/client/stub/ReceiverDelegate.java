package com.lody.virtual.client.stub;

import com.lody.virtual.helper.ExtraConstants;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Lody
 *
 */
public class ReceiverDelegate extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		String type = intent.getStringExtra(ExtraConstants.EXTRA_INTENT_TYPE);
		if (type == null) {
			return;
		}
		if (type.equals(ExtraConstants.TYPE_INTENT_SENDER)) {
			onReceiveIntentSender(context, intent);
		}

	}

	private void onReceiveIntentSender(Context context, Intent intent) {
		int flags = intent.getIntExtra(ExtraConstants.EXTRA_FLAGS, -1);
		Intent senderIntent = intent.getParcelableExtra(ExtraConstants.EXTRA_INTENT);
		if (flags != -1 && senderIntent != null) {
			switch (flags) {
				case ActivityManager.INTENT_SENDER_ACTIVITY :
					try {
						// 非activity的context得加这个
						senderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(senderIntent);
					} catch (Throwable e) {
						// Ignore
					}
					break;
				case ActivityManager.INTENT_SENDER_SERVICE :
					try {
						context.startService(senderIntent);
					} catch (Throwable e) {
						// Ignore
					}
			}
		}
	}
}
