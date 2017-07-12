package com.lody.virtual.remote;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable.Creator;

/**
 * Information about the sync operation that is currently underway.
 */
public class SyncInfo {

    public final int authorityId;

    /**
     * The {@link Account} that is currently being synced.
     */
    public final Account account;

    /**
     * The authority of the provider that is currently being synced.
     */
    public final String authority;

    /**
     * The start time of the current sync operation in milliseconds since boot.
     * This is represented in elapsed real time.
     * See {@link android.os.SystemClock#elapsedRealtime()}.
     */
    public final long startTime;

    public SyncInfo(int authorityId, Account account, String authority,
                    long startTime) {
        this.authorityId = authorityId;
        this.account = account;
        this.authority = authority;
        this.startTime = startTime;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(authorityId);
        account.writeToParcel(parcel, 0);
        parcel.writeString(authority);
        parcel.writeLong(startTime);
    }

    SyncInfo(Parcel parcel) {
        authorityId = parcel.readInt();
        account = new Account(parcel);
        authority = parcel.readString();
        startTime = parcel.readLong();
    }

    public android.content.SyncInfo create() {
        return mirror.android.content.SyncInfo.ctor.newInstance(authorityId, account, authority, startTime);
    }

    public static final Creator<SyncInfo> CREATOR = new Creator<SyncInfo>() {
        public SyncInfo createFromParcel(Parcel in) {
            return new SyncInfo(in);
        }

        public SyncInfo[] newArray(int size) {
            return new SyncInfo[size];
        }
    };

}
