package com.lody.virtual.remote;

import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.os.VUserHandle;

/**
 * @author Lody
 */
public class BadgerInfo implements Parcelable {

    public int userId;
    public String packageName;
    public int badgerCount;
    public String className;

    public BadgerInfo() {
        userId = VUserHandle.myUserId();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(packageName);
        dest.writeInt(badgerCount);
        dest.writeString(className);
    }

    protected BadgerInfo(Parcel in) {
        userId = in.readInt();
        packageName = in.readString();
        badgerCount = in.readInt();
        className = in.readString();
    }

    public static final Parcelable.Creator<BadgerInfo> CREATOR = new Parcelable.Creator<BadgerInfo>() {
        @Override
        public BadgerInfo createFromParcel(Parcel source) {
            return new BadgerInfo(source);
        }

        @Override
        public BadgerInfo[] newArray(int size) {
            return new BadgerInfo[size];
        }
    };
}
