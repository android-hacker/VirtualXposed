package com.lody.virtual.helper.compat;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import mirror.android.app.ActivityManagerNative;
import mirror.android.app.IActivityManagerICS;
import mirror.android.app.IActivityManagerL;
import mirror.android.app.IActivityManagerN;

/**
 * @author Lody
 */

public class ActivityManagerCompat {
	/** Type for IActivityManager.serviceDoneExecuting: anonymous operation */
	public static final int SERVICE_DONE_EXECUTING_ANON = 0;
	/** Type for IActivityManager.serviceDoneExecuting: done with an onStart call */
	public static final int SERVICE_DONE_EXECUTING_START = 1;
	/** Type for IActivityManager.serviceDoneExecuting: done stopping (destroying) service */
	public static final int SERVICE_DONE_EXECUTING_STOP = 2;

	/**
	 * Result for IActivityManager.startActivity: an error where the
	 * given Intent could not be resolved to an activity.
	 */
	public static final int START_INTENT_NOT_RESOLVED = -1;

	/**
	 * Result for IActivityManager.startActivity: trying to start a background user
	 * activity that shouldn't be displayed for all users.
	 */
	public static final int START_NOT_CURRENT_USER_ACTIVITY = -8;

	/**
	 * Result for IActivityManaqer.startActivity: activity wasn't really started, but
	 * a task was simply brought to the foreground.
	 */
	public static final int START_TASK_TO_FRONT = 2;

	/**
	 * Type for IActivityManaqer.getIntentSender: this PendingIntent is
	 * for a sendBroadcast operation.
	 */
	public static final int INTENT_SENDER_BROADCAST = 1;

	/**
	 * Type for IActivityManaqer.getIntentSender: this PendingIntent is
	 * for a startActivity operation.
	 */
	public static final int INTENT_SENDER_ACTIVITY = 2;

	/**
	 * Type for IActivityManaqer.getIntentSender: this PendingIntent is
	 * for an activity result operation.
	 */
	public static final int INTENT_SENDER_ACTIVITY_RESULT = 3;

	/**
	 * Type for IActivityManaqer.getIntentSender: this PendingIntent is
	 * for a startService operation.
	 */
	public static final int INTENT_SENDER_SERVICE = 4;

	/** User operation call: success! */
	public static final int USER_OP_SUCCESS = 0;

	public static boolean finishActivity(IBinder token, int code, Intent data) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return IActivityManagerN.finishActivity.call(
					ActivityManagerNative.getDefault.call(),
					token, code, data, 0);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return IActivityManagerL.finishActivity.call(
						ActivityManagerNative.getDefault.call(),
						token, code, data, false);
		} else {
			IActivityManagerICS.finishActivity.call(
					ActivityManagerNative.getDefault.call(),
					token, code, data
			);
		}

		return false;
	}
}
