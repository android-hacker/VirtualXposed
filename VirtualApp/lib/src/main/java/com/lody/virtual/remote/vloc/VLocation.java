package com.lody.virtual.remote.vloc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 */

public class VLocation implements Parcelable {

    public double latitude = 0.0;
    public double longitude = 0.0;
    public double altitude = 0.0f;
    public float accuracy = 0.0f;
    public float speed;
    public float bearing;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeDouble(this.altitude);
        dest.writeFloat(this.accuracy);
        dest.writeFloat(this.speed);
        dest.writeFloat(this.bearing);
    }

    public VLocation() {
    }

    public VLocation(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.altitude = in.readDouble();
        this.accuracy = in.readFloat();
        this.speed = in.readFloat();
        this.bearing = in.readFloat();
    }

    public static final Parcelable.Creator<VLocation> CREATOR = new Parcelable.Creator<VLocation>() {
        @Override
        public VLocation createFromParcel(Parcel source) {
            return new VLocation(source);
        }

        @Override
        public VLocation[] newArray(int size) {
            return new VLocation[size];
        }
    };
}
