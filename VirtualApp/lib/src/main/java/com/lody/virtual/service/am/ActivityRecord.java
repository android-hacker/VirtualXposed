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
	ActivityInfo caller;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ActivityRecord that = (ActivityRecord) o;

		if (pid != that.pid) return false;
		return token != null ? token.equals(that.token) : that.token == null;

	}

	@Override
	public int hashCode() {
		int result = pid;
		result = 31 * result + (token != null ? token.hashCode() : 0);
		return result;
	}
}
