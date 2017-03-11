package io.virtualapp.home.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 */

public class AppInfoLite implements Parcelable {

    public static final Creator<AppInfoLite> CREATOR = new Creator<AppInfoLite>() {
        @Override
        public AppInfoLite createFromParcel(Parcel source) {
            return new AppInfoLite(source);
        }

        @Override
        public AppInfoLite[] newArray(int size) {
            return new AppInfoLite[size];
        }
    };
    public String packageName;
    public String path;
    public boolean fastOpen;

    public AppInfoLite(String packageName, String path, boolean fastOpen) {
        this.packageName = packageName;
        this.path = path;
        this.fastOpen = fastOpen;
    }

    protected AppInfoLite(Parcel in) {
        this.packageName = in.readString();
        this.path = in.readString();
        this.fastOpen = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.path);
        dest.writeByte(this.fastOpen ? (byte) 1 : (byte) 0);
    }
}
