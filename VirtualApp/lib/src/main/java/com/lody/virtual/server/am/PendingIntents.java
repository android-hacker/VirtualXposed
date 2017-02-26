package com.lody.virtual.server.am;

import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.remote.PendingIntentData;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Lody
 */
public final class PendingIntents {

    private final Map<IBinder, PendingIntentData> mLruHistory = new HashMap<>();

    final PendingIntentData getPendingIntent(IBinder binder) {
        synchronized (mLruHistory) {
            return mLruHistory.get(binder);
        }
    }

    final void addPendingIntent(final IBinder binder, String creator) {
        synchronized (mLruHistory) {
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        binder.unlinkToDeath(this, 0);
                        mLruHistory.remove(binder);
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            PendingIntentData pendingIntentData = mLruHistory.get(binder);
            if (pendingIntentData == null) {
                mLruHistory.put(binder, new PendingIntentData(creator, binder));
            } else {
                pendingIntentData.creator = creator;
            }
        }
    }

    final void removePendingIntent(IBinder binder) {
        synchronized (mLruHistory) {
            mLruHistory.remove(binder);
        }
    }
}