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

	public ActivityInfo stubActInfo;
	public int flags;
	public IBinder replaceToken;
	public IBinder newIntentToken;
	public IBinder targetClient;
	public boolean justReturn;

	public VActRedirectResult() {
		justReturn = true;
	}

	public VActRedirectResult(IBinder newIntentToken, IBinder targetClient) {
		this.newIntentToken = newIntentToken;
		this.targetClient = targetClient;
	}

	public VActRedirectResult(ActivityInfo stubActInfo, int flags) {
		this.stubActInfo = stubActInfo;
		this.flags = flags;
	}

	protected VActRedirectResult(Parcel in) {
		this.stubActInfo = in.readParcelable(ActivityInfo.class.getClassLoader());
		this.flags = in.readInt();
		this.replaceToken = in.readStrongBinder();
		this.newIntentToken = in.readStrongBinder();
		this.targetClient = in.readStrongBinder();
		this.justReturn = in.readByte() == 1;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.stubActInfo, flags);
		dest.writeInt(this.flags);
		dest.writeStrongBinder(this.replaceToken);
		dest.writeStrongBinder(this.newIntentToken);
		dest.writeStrongBinder(this.targetClient);
		dest.writeByte((byte) (justReturn ? 1 : 0));
	}
}
