package com.lody.virtual.helper.proto;

import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 *
 */
public class VRedirectActRequest implements Parcelable {

	public static final Creator<VRedirectActRequest> CREATOR = new Creator<VRedirectActRequest>() {
		@Override
		public VRedirectActRequest createFromParcel(Parcel source) {
			return new VRedirectActRequest(source);
		}

		@Override
		public VRedirectActRequest[] newArray(int size) {
			return new VRedirectActRequest[size];
		}
	};
	public ActivityInfo targetActInfo;
	public IBinder resultTo;
	public int targetFlags;
	public boolean fromHost;

	public VRedirectActRequest(ActivityInfo targetActInfo, int targetFlags) {
		this.targetActInfo = targetActInfo;
		this.targetFlags = targetFlags;
	}

	public VRedirectActRequest(ActivityInfo targetActInfo, IBinder resultTo, int targetFlags) {
		this.targetActInfo = targetActInfo;
		this.resultTo = resultTo;
		this.targetFlags = targetFlags;
	}

	protected VRedirectActRequest(Parcel in) {
		this.targetActInfo = in.readParcelable(ActivityInfo.class.getClassLoader());
		this.resultTo = in.readStrongBinder();
		this.targetFlags = in.readInt();
		this.fromHost = in.readByte() != 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.targetActInfo, flags);
		dest.writeStrongBinder(this.resultTo);
		dest.writeInt(this.targetFlags);
		dest.writeByte(this.fromHost ? (byte) 1 : (byte) 0);
	}
}
