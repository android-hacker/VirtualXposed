package com.lody.virtual.remote;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 */

public class AppTaskInfo implements Parcelable {
	public static final Parcelable.Creator<AppTaskInfo> CREATOR = new Parcelable.Creator<AppTaskInfo>() {
		@Override
		public AppTaskInfo createFromParcel(Parcel source) {
			return new AppTaskInfo(source);
		}

		@Override
		public AppTaskInfo[] newArray(int size) {
			return new AppTaskInfo[size];
		}
	};
	public int taskId;
	public Intent baseIntent;
	public ComponentName baseActivity;
	public ComponentName topActivity;


	public AppTaskInfo(int taskId, Intent baseIntent, ComponentName baseActivity, ComponentName topActivity) {
		this.taskId = taskId;
		this.baseIntent = baseIntent;
		this.baseActivity = baseActivity;
		this.topActivity = topActivity;
	}

	protected AppTaskInfo(Parcel in) {
		taskId = in.readInt();
		baseIntent = in.readParcelable(Intent.class.getClassLoader());
		baseActivity = in.readParcelable(ComponentName.class.getClassLoader());
		topActivity = in.readParcelable(ComponentName.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(taskId);
		dest.writeParcelable(baseIntent, flags);
		dest.writeParcelable(baseActivity, flags);
		dest.writeParcelable(topActivity, flags);
	}
}
