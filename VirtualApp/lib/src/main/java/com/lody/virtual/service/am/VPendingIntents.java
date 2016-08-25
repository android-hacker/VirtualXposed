package com.lody.virtual.service.am;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.lody.virtual.helper.proto.PendingIntentData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author Lody
 */
public final class VPendingIntents {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Map<IBinder, PendingIntentData> mLruHistory = new HashMap<>();

    private Runnable mCleanScheduler = new Runnable() {
        public final void run() {
            synchronized (mLruHistory) {
                Iterator<Entry<IBinder, PendingIntentData>> it = mLruHistory.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<IBinder, PendingIntentData> entry = it.next();
                    if (entry.getValue().pendingIntent.getTargetPackage() == null) {
                        it.remove();
                    }
                }
            }
            mHandler.postDelayed(this, 300000);
        }
    };

    final PendingIntentData getPendingIntent(IBinder binder) {
        synchronized (mLruHistory) {
            return mLruHistory.get(binder);
        }
    }

    final void addPendingIntent(IBinder binder, String creator) {
        synchronized (mLruHistory) {
            if (mLruHistory.isEmpty()) {
                mHandler.postDelayed(mCleanScheduler, 300000);
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
            if (mLruHistory.isEmpty()) {
                mHandler.removeCallbacks(mCleanScheduler);
            }
        }
    }
}