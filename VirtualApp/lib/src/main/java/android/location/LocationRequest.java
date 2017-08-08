package android.location;

import android.os.Parcel;
import android.os.Parcelable;

public final class LocationRequest implements Parcelable {

    public String getProvider() {
        return null;
    }


    public static final Creator<LocationRequest> CREATOR = new Creator<LocationRequest>() {
        @Override
        public LocationRequest createFromParcel(Parcel in) {
            return null;
        }

        @Override
        public LocationRequest[] newArray(int size) {
            return null;
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}