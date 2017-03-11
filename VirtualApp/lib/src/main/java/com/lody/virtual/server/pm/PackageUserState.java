package com.lody.virtual.server.pm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 */

public class PackageUserState implements Parcelable {

    public static final Parcelable.Creator<PackageUserState> CREATOR = new Parcelable.Creator<PackageUserState>() {
        @Override
        public PackageUserState createFromParcel(Parcel source) {
            return new PackageUserState(source);
        }

        @Override
        public PackageUserState[] newArray(int size) {
            return new PackageUserState[size];
        }
    };
    public boolean launched;
    public boolean hidden;
    public boolean installed;

    public PackageUserState() {
        installed = false;
        launched = true;
        hidden = false;
    }

    protected PackageUserState(Parcel in) {
        this.launched = in.readByte() != 0;
        this.hidden = in.readByte() != 0;
        this.installed = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.launched ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hidden ? (byte) 1 : (byte) 0);
        dest.writeByte(this.installed ? (byte) 1 : (byte) 0);
    }
}
