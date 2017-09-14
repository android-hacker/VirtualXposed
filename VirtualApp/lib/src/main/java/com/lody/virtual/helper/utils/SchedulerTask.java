package com.lody.virtual.helper.utils;

import android.os.Handler;

/**
 * @author Lody
 */

public abstract class SchedulerTask implements Runnable {
    private Handler mHandler;
    private long mDelay;

    public SchedulerTask(Handler handler, long delay) {
        this.mHandler = handler;
        this.mDelay = delay;
    }

    public void schedule() {
        mHandler.post(mInnerRunnable);
    }

    public void cancel() {
        mHandler.removeCallbacks(mInnerRunnable);
    }

    private final Runnable mInnerRunnable = new Runnable() {
        @Override
        public void run() {
            SchedulerTask.this.run();
            if(mDelay > 0) {
                mHandler.postDelayed(this, mDelay);
            }
        }
    };
}
