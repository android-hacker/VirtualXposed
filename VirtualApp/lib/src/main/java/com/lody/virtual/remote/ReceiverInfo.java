package com.lody.virtual.remote;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 */

public class ReceiverInfo implements Parcelable {
	public static final Creator<ReceiverInfo> CREATOR = new Creator<ReceiverInfo>() {
		@Override
		public ReceiverInfo createFromParcel(Parcel source) {
			return new ReceiverInfo(source);
		}

		@Override
		public ReceiverInfo[] newArray(int size) {
			return new ReceiverInfo[size];
		}
	};
	public ComponentName component;
	public IntentFilter[] filters;
	public String permission;

	public ReceiverInfo(ComponentName component, IntentFilter[] filters, String permission) {
		this.component = component;
		this.filters = filters;
		this.permission = permission;
	}

	protected ReceiverInfo(Parcel in) {
		this.component = in.readParcelable(ComponentName.class.getClassLoader());
		this.filters = in.createTypedArray(IntentFilter.CREATOR);
		this.permission = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.component, flags);
		dest.writeTypedArray(this.filters, flags);
		dest.writeString(this.permission);
	}
}
