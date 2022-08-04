package com.lody.virtual.remote.vloc;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.client.env.VirtualGPSSatalines;
import com.lody.virtual.helper.utils.Reflect;

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
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(altitude);
        dest.writeFloat(accuracy);
        dest.writeFloat(speed);
        dest.writeFloat(bearing);
    }

    public VLocation() {
    }

    public VLocation(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        altitude = in.readDouble();
        accuracy = in.readFloat();
        speed = in.readFloat();
        bearing = in.readFloat();
    }

    public boolean isEmpty() {
        return latitude == 0 && longitude == 0;
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

    @Override
    public String toString() {
        return "VLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", accuracy=" + accuracy +
                ", speed=" + speed +
                ", bearing=" + bearing +
                '}';
    }

    public Location toSysLocation() {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setAccuracy(8f);
        Bundle extraBundle = new Bundle();
        location.setBearing(bearing);
        Reflect.on(location).call("setIsFromMockProvider", false);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setSpeed(speed);
        location.setTime(System.currentTimeMillis());
        location.setExtras(extraBundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(277000000);
        }
        int svCount = VirtualGPSSatalines.get().getSvCount();
        extraBundle.putInt("satellites", svCount);
        extraBundle.putInt("satellitesvalue", svCount);
        return location;
    }
}
