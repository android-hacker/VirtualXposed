package com.lody.virtual.helper.proto;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lody
 */

public class StubActivityRecord implements Parcelable {
        public Intent intent;
        public ActivityInfo info;
        public ComponentName caller;
        public int userId;

        public StubActivityRecord(Intent intent, ActivityInfo info, ComponentName caller, int userId) {
            this.intent = intent;
            this.info = info;
            this.caller = caller;
            this.userId = userId;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(intent, flags);
            dest.writeParcelable(info, flags);
            dest.writeParcelable(caller, flags);
            dest.writeInt(userId);
        }

        protected StubActivityRecord(Parcel in) {
            intent = in.readParcelable(Intent.class.getClassLoader());
            info = in.readParcelable(ActivityInfo.class.getClassLoader());
            caller = in.readParcelable(ComponentName.class.getClassLoader());
            userId = in.readInt();
        }

        public static final Parcelable.Creator<StubActivityRecord> CREATOR = new Parcelable.Creator<StubActivityRecord>() {
            @Override
            public StubActivityRecord createFromParcel(Parcel source) {
                return new StubActivityRecord(source);
            }

            @Override
            public StubActivityRecord[] newArray(int size) {
                return new StubActivityRecord[size];
            }
        };
}
