package com.lody.virtual.helper.proto;

import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 *
 */
public class VActRedirectResult implements Parcelable {

	public ActivityInfo stubActInfo;
	/**
	 * 已经拦截Activity的启动,转为其它的操作,比如onNewIntent...
	 */
	public boolean intercepted;
	public int flags;
	public boolean reopen;
	public IBinder replaceToken;


	public VActRedirectResult(boolean reopen) {
		this.intercepted = true;
		this.reopen = reopen;
	}

	public VActRedirectResult(ActivityInfo stubActInfo, int flags) {
		this.intercepted = false;
		this.stubActInfo = stubActInfo;
		this.flags = flags;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.stubActInfo, flags);
		dest.writeByte(this.intercepted ? (byte) 1 : (byte) 0);
		dest.writeInt(this.flags);
		dest.writeByte(this.reopen ? (byte) 1 : (byte) 0);
		dest.writeStrongBinder(this.replaceToken);
	}

	protected VActRedirectResult(Parcel in) {
		this.stubActInfo = in.readParcelable(ActivityInfo.class.getClassLoader());
		this.intercepted = in.readByte() != 0;
		this.flags = in.readInt();
		this.reopen = in.readByte() != 0;
		this.replaceToken = in.readStrongBinder();
	}

	public static final Creator<VActRedirectResult> CREATOR = new Creator<VActRedirectResult>() {
		@Override
		public VActRedirectResult createFromParcel(Parcel source) {
			return new VActRedirectResult(source);
		}

		@Override
		public VActRedirectResult[] newArray(int size) {
			return new VActRedirectResult[size];
		}
	};
}
