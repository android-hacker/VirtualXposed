package com.lody.virtual.service.am;

import android.content.pm.ActivityInfo;
import android.os.IBinder;

/**
 * @author Lody
 */

/*package*/ class ActivityRecord {
	int pid;
	IBinder token;
	ActivityInfo activityInfo;
}
