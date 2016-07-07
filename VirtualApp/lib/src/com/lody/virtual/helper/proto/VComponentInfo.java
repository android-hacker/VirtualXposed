package com.lody.virtual.helper.proto;

import android.content.pm.ComponentInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 *
 */
public class VComponentInfo extends ComponentInfo implements Parcelable {

	public static final Creator<VComponentInfo> CREATOR = new Creator<VComponentInfo>() {
		@Override
		public VComponentInfo createFromParcel(Parcel in) {
			return new VComponentInfo(in);
		}

		@Override
		public VComponentInfo[] newArray(int size) {
			return new VComponentInfo[size];
		}
	};

	protected VComponentInfo(Parcel in) {
		super();
	}

	public VComponentInfo(ComponentInfo orig) {
		super(orig);
	}

	public static VComponentInfo wrap(ComponentInfo componentInfo) {
		return new VComponentInfo(componentInfo);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
