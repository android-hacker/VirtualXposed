package com.lody.virtual.helper.proto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 *
 */
public class InstallResult implements Parcelable {

	public boolean isSuccess;
	public boolean isUpdate;
	public String packageName;
	public String error;

	public InstallResult() {
	}

	protected InstallResult(Parcel in) {
		this.isSuccess = in.readByte() != 0;
		this.isUpdate = in.readByte() != 0;
		this.packageName = in.readString();
		this.error = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (isSuccess ? 1 : 0));
		dest.writeByte((byte) (isUpdate ? 1 : 0));
		dest.writeString(packageName);
		dest.writeString(error);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<InstallResult> CREATOR = new Creator<InstallResult>() {
		@Override
		public InstallResult createFromParcel(Parcel in) {
			return new InstallResult(in);
		}

		@Override
		public InstallResult[] newArray(int size) {
			return new InstallResult[size];
		}
	};

	public static InstallResult makeFailure(String error) {
		InstallResult res = new InstallResult();
		res.error = error;
		return res;
	}
}
