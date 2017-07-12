package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

public class SyncStatusInfo implements Parcelable {
    static final int VERSION = 2;

    public final int authorityId;
    public long totalElapsedTime;
    public int numSyncs;
    public int numSourcePoll;
    public int numSourceServer;
    public int numSourceLocal;
    public int numSourceUser;
    public int numSourcePeriodic;
    public long lastSuccessTime;
    public int lastSuccessSource;
    public long lastFailureTime;
    public int lastFailureSource;
    public String lastFailureMesg;
    public long initialFailureTime;
    public boolean pending;
    public boolean initialize;
    
  // Warning: It is up to the external caller to ensure there are
  // no race conditions when accessing this list
  private ArrayList<Long> periodicSyncTimes;

    private static final String TAG = "Sync";

    public SyncStatusInfo(int authorityId) {
        this.authorityId = authorityId;
    }

    public int getLastFailureMesgAsInt(int def) {
        return 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(VERSION);
        parcel.writeInt(authorityId);
        parcel.writeLong(totalElapsedTime);
        parcel.writeInt(numSyncs);
        parcel.writeInt(numSourcePoll);
        parcel.writeInt(numSourceServer);
        parcel.writeInt(numSourceLocal);
        parcel.writeInt(numSourceUser);
        parcel.writeLong(lastSuccessTime);
        parcel.writeInt(lastSuccessSource);
        parcel.writeLong(lastFailureTime);
        parcel.writeInt(lastFailureSource);
        parcel.writeString(lastFailureMesg);
        parcel.writeLong(initialFailureTime);
        parcel.writeInt(pending ? 1 : 0);
        parcel.writeInt(initialize ? 1 : 0);
        if (periodicSyncTimes != null) {
            parcel.writeInt(periodicSyncTimes.size());
            for (long periodicSyncTime : periodicSyncTimes) {
                parcel.writeLong(periodicSyncTime);
            }
        } else {
            parcel.writeInt(-1);
        }
    }

    public SyncStatusInfo(Parcel parcel) {
        int version = parcel.readInt();
        if (version != VERSION && version != 1) {
            Log.w("SyncStatusInfo", "Unknown version: " + version);
        }
        authorityId = parcel.readInt();
        totalElapsedTime = parcel.readLong();
        numSyncs = parcel.readInt();
        numSourcePoll = parcel.readInt();
        numSourceServer = parcel.readInt();
        numSourceLocal = parcel.readInt();
        numSourceUser = parcel.readInt();
        lastSuccessTime = parcel.readLong();
        lastSuccessSource = parcel.readInt();
        lastFailureTime = parcel.readLong();
        lastFailureSource = parcel.readInt();
        lastFailureMesg = parcel.readString();
        initialFailureTime = parcel.readLong();
        pending = parcel.readInt() != 0;
        initialize = parcel.readInt() != 0;
        if (version == 1) {
            periodicSyncTimes = null;
        } else {
            int N = parcel.readInt();
            if (N < 0) {
                periodicSyncTimes = null;
            } else {
                periodicSyncTimes = new ArrayList<Long>();
                for (int i=0; i<N; i++) {
                    periodicSyncTimes.add(parcel.readLong());
                }
            }
        }
    }

    public SyncStatusInfo(SyncStatusInfo other) {
        authorityId = other.authorityId;
        totalElapsedTime = other.totalElapsedTime;
        numSyncs = other.numSyncs;
        numSourcePoll = other.numSourcePoll;
        numSourceServer = other.numSourceServer;
        numSourceLocal = other.numSourceLocal;
        numSourceUser = other.numSourceUser;
        numSourcePeriodic = other.numSourcePeriodic;
        lastSuccessTime = other.lastSuccessTime;
        lastSuccessSource = other.lastSuccessSource;
        lastFailureTime = other.lastFailureTime;
        lastFailureSource = other.lastFailureSource;
        lastFailureMesg = other.lastFailureMesg;
        initialFailureTime = other.initialFailureTime;
        pending = other.pending;
        initialize = other.initialize;
        if (other.periodicSyncTimes != null) {
            periodicSyncTimes = new ArrayList<Long>(other.periodicSyncTimes);
        }
    }

    public void setPeriodicSyncTime(int index, long when) {
        // The list is initialized lazily when scheduling occurs so we need to make sure
        // we initialize elements < index to zero (zero is ignore for scheduling purposes)
        ensurePeriodicSyncTimeSize(index);
        periodicSyncTimes.set(index, when);
    }

    public long getPeriodicSyncTime(int index) {
        if (periodicSyncTimes != null && index < periodicSyncTimes.size()) {
            return periodicSyncTimes.get(index);
        } else {
            return 0;
        }
    }

    public void removePeriodicSyncTime(int index) {
        if (periodicSyncTimes != null && index < periodicSyncTimes.size()) {
            periodicSyncTimes.remove(index);
        }
    }

    public static final Creator<SyncStatusInfo> CREATOR = new Creator<SyncStatusInfo>() {
        public SyncStatusInfo createFromParcel(Parcel in) {
            return new SyncStatusInfo(in);
        }

        public SyncStatusInfo[] newArray(int size) {
            return new SyncStatusInfo[size];
        }
    };

    private void ensurePeriodicSyncTimeSize(int index) {
        if (periodicSyncTimes == null) {
            periodicSyncTimes = new ArrayList<>(0);
        }

        final int requiredSize = index + 1;
        if (periodicSyncTimes.size() < requiredSize) {
            for (int i = periodicSyncTimes.size(); i < requiredSize; i++) {
                periodicSyncTimes.add((long) 0);
            }
        }
    }
}