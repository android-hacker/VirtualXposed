package com.lody.virtual.remote;

import android.app.PendingIntent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class PendingIntentData implements Parcelable {

    public static final Creator<PendingIntentData> CREATOR = new Creator<PendingIntentData>() {
        public final PendingIntentData createFromParcel(Parcel source) {
            return new PendingIntentData(source);
        }

        public final PendingIntentData[] newArray(int size) {
            return new PendingIntentData[size];
        }
    };
    public String creator;
    public PendingIntent pendingIntent;

    protected PendingIntentData(Parcel source) {
        this.creator = source.readString();
        this.pendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(source);
    }

    public PendingIntentData(String creator, IBinder binder) {
        this.creator = creator;
        this.pendingIntent = readPendingIntent(binder);
    }

    public static PendingIntent readPendingIntent(IBinder binder) {
        Parcel parcel = Parcel.obtain();
        parcel.writeStrongBinder(binder);
        parcel.setDataPosition(0);
        try {
            return PendingIntent.readPendingIntentOrNullFromParcel(parcel);
        } finally {
            parcel.recycle();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.creator);
        this.pendingIntent.writeToParcel(dest, flags);
    }
}