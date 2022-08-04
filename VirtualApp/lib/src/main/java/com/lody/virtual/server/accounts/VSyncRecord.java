package com.lody.virtual.server.accounts;

import android.accounts.Account;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lody
 */

public class VSyncRecord {

    public int userId;
    public SyncRecordKey key;
    public int syncable = -1;
    public boolean isPeriodic = false;
    public Map<SyncExtras, PeriodicSyncConfig> configs = new HashMap<>();
    public List<SyncExtras> extras = new ArrayList<>();

    public VSyncRecord(int userId, Account account, String authority) {
        this.userId = userId;
        key = new SyncRecordKey(account, authority);
    }

    public static class SyncExtras implements Parcelable {
        Bundle extras;

        public SyncExtras(Bundle extras) {
            this.extras = extras;
        }

        SyncExtras(Parcel in) {
            this.extras = in.readBundle(getClass().getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeBundle(this.extras);
        }

        public static final Parcelable.Creator<SyncExtras> CREATOR = new Parcelable.Creator<SyncExtras>() {
            @Override
            public SyncExtras createFromParcel(Parcel source) {
                return new SyncExtras(source);
            }

            @Override
            public SyncExtras[] newArray(int size) {
                return new SyncExtras[size];
            }
        };

        @Override
        public boolean equals(Object obj) {
            return VSyncRecord.equals(this.extras, ((SyncExtras) obj).extras, false);
        }
    }

    public static class SyncRecordKey implements Parcelable {

        Account account;
        String authority;

        SyncRecordKey(Account account, String authority) {
            this.account = account;
            this.authority = authority;
        }

        SyncRecordKey(Parcel in) {
            this.account = in.readParcelable(Account.class.getClassLoader());
            this.authority = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.account, flags);
            dest.writeString(this.authority);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SyncRecordKey that = (SyncRecordKey) o;

            if (account != null ? !account.equals(that.account) : that.account != null)
                return false;
            return authority != null ? authority.equals(that.authority) : that.authority == null;
        }

        public static final Parcelable.Creator<SyncRecordKey> CREATOR = new Parcelable.Creator<SyncRecordKey>() {
            @Override
            public SyncRecordKey createFromParcel(Parcel source) {
                return new SyncRecordKey(source);
            }

            @Override
            public SyncRecordKey[] newArray(int size) {
                return new SyncRecordKey[size];
            }
        };
    }

    public static boolean equals(Bundle a, Bundle b, boolean sameSize) {
        if (a == b) {
            return true;
        }
        if (sameSize && a.size() != b.size()) {
            return false;
        }
        if (a.size() <= b.size()) {
            Bundle smaller = a;
            a = b;
            b = smaller;
        }
        for (String key : a.keySet()) {
            if (sameSize || !isIgnoredKey(key)) {
                if (!b.containsKey(key)) {
                    return false;
                }
                //noinspection ConstantConditions
                if (!a.get(key).equals(b.get(key))) {
                    return false;
                }
            }
        }
        return true;
    }

    static class PeriodicSyncConfig implements Parcelable {

        long syncRunTimeSecs;

        public PeriodicSyncConfig(long syncRunTimeSecs) {
            this.syncRunTimeSecs = syncRunTimeSecs;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.syncRunTimeSecs);
        }

        PeriodicSyncConfig(Parcel in) {
            this.syncRunTimeSecs = in.readLong();
        }

        public static final Parcelable.Creator<PeriodicSyncConfig> CREATOR = new Parcelable.Creator<PeriodicSyncConfig>() {
            @Override
            public PeriodicSyncConfig createFromParcel(Parcel source) {
                return new PeriodicSyncConfig(source);
            }

            @Override
            public PeriodicSyncConfig[] newArray(int size) {
                return new PeriodicSyncConfig[size];
            }
        };
    }

    private static boolean isIgnoredKey(String str) {
        return str.equals("expedited")
                || str.equals("ignore_settings")
                || str.equals("ignore_backoff")
                || str.equals("do_not_retry")
                || str.equals("force")
                || str.equals("upload")
                || str.equals("deletions_override")
                || str.equals("discard_deletions")
                || str.equals("expected_upload")
                || str.equals("expected_download")
                || str.equals("sync_priority")
                || str.equals("allow_metered")
                || str.equals("initialize");
    }
}
