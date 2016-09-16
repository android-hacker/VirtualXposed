package com.lody.virtual.server.job.controllers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.lody.virtual.server.job.JobSchedulerService;
import com.lody.virtual.server.job.StateChangedListener;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class sets an alarm for the next expiring job, and determines whether a job's minimum
 * delay has been satisfied.
 */
public class TimeController extends StateController {
    private static final String TAG = "JobScheduler.Time";
    private static final String ACTION_JOB_EXPIRED =
            "android.content.jobscheduler.JOB_DEADLINE_EXPIRED";
    private static final String ACTION_JOB_DELAY_EXPIRED =
            "android.content.jobscheduler.JOB_DELAY_EXPIRED";

    /** Set an alarm for the next job expiry. */
    private final PendingIntent mDeadlineExpiredAlarmIntent;
    /** Set an alarm for the next job delay expiry. This*/
    private final PendingIntent mNextDelayExpiredAlarmIntent;

    private long mNextJobExpiredElapsedMillis;
    private long mNextDelayExpiredElapsedMillis;

    private AlarmManager mAlarmService = null;
    /** List of tracked jobs, sorted asc. by deadline */
    private final List<JobStatus> mTrackedJobs = new LinkedList<JobStatus>();
    /** Singleton. */
    private static TimeController mSingleton;

    public static synchronized TimeController get(JobSchedulerService jms) {
        if (mSingleton == null) {
            mSingleton = new TimeController(jms, jms.getContext());
        }
        return mSingleton;
    }

    private TimeController(StateChangedListener stateChangedListener, Context context) {
        super(stateChangedListener, context);
        mDeadlineExpiredAlarmIntent =
                PendingIntent.getBroadcast(mContext, 0 /* ignored */,
                        new Intent(ACTION_JOB_EXPIRED), 0);
        mNextDelayExpiredAlarmIntent =
                PendingIntent.getBroadcast(mContext, 0 /* ignored */,
                        new Intent(ACTION_JOB_DELAY_EXPIRED), 0);
        mNextJobExpiredElapsedMillis = Long.MAX_VALUE;
        mNextDelayExpiredElapsedMillis = Long.MAX_VALUE;

        // Register BR for these intents.
        IntentFilter intentFilter = new IntentFilter(ACTION_JOB_EXPIRED);
        intentFilter.addAction(ACTION_JOB_DELAY_EXPIRED);
        mContext.registerReceiver(mAlarmExpiredReceiver, intentFilter);
    }

    /**
     * Check if the job has a timing constraint, and if so determine where to insert it in our
     * list.
     */
    @Override
    public synchronized void maybeStartTrackingJob(JobStatus job) {
        if (job.hasTimingDelayConstraint() || job.hasDeadlineConstraint()) {
            maybeStopTrackingJob(job);
            ListIterator<JobStatus> it = mTrackedJobs.listIterator(mTrackedJobs.size());
            while (it.hasPrevious()) {
                JobStatus ts = it.previous();
                if (ts.getLatestRunTimeElapsed() < job.getLatestRunTimeElapsed()) {
                    // Insert
                    break;
                }
            }
            it.add(job);
            maybeUpdateAlarms(
                    job.hasTimingDelayConstraint() ? job.getEarliestRunTime() : Long.MAX_VALUE,
                    job.hasDeadlineConstraint() ? job.getLatestRunTimeElapsed() : Long.MAX_VALUE);
        }
    }

    /**
     * When we stop tracking a job, we only need to update our alarms if the job we're no longer
     * tracking was the one our alarms were based off of.
     * Really an == comparison should be enough, but why play with fate? We'll do <=.
     */
    @Override
    public synchronized void maybeStopTrackingJob(JobStatus job) {
        if (mTrackedJobs.remove(job)) {
            checkExpiredDelaysAndResetAlarm();
            checkExpiredDeadlinesAndResetAlarm();
        }
    }

    /**
     * Determines whether this controller can stop tracking the given job.
     * The controller is no longer interested in a job once its time constraint is satisfied, and
     * the job's deadline is fulfilled - unlike other controllers a time constraint can't toggle
     * back and forth.
     */
    private boolean canStopTrackingJob(JobStatus job) {
        return (!job.hasTimingDelayConstraint() ||
                job.timeDelayConstraintSatisfied.get()) &&
                (!job.hasDeadlineConstraint() ||
                        job.deadlineConstraintSatisfied.get());
    }

    private void ensureAlarmService() {
        if (mAlarmService == null) {
            mAlarmService = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        }
    }

    /**
     * Checks list of jobs for ones that have an expired deadline, sending them to the JobScheduler
     * if so, removing them from this list, and updating the alarm for the next expiry time.
     */
    private synchronized void checkExpiredDeadlinesAndResetAlarm() {
        long nextExpiryTime = Long.MAX_VALUE;
        final long nowElapsedMillis = SystemClock.elapsedRealtime();

        Iterator<JobStatus> it = mTrackedJobs.iterator();
        while (it.hasNext()) {
            JobStatus job = it.next();
            if (!job.hasDeadlineConstraint()) {
                continue;
            }
            final long jobDeadline = job.getLatestRunTimeElapsed();

            if (jobDeadline <= nowElapsedMillis) {
                job.deadlineConstraintSatisfied.set(true);
                mStateChangedListener.onRunJobNow(job);
                it.remove();
            } else {  // Sorted by expiry time, so take the next one and stop.
                nextExpiryTime = jobDeadline;
                break;
            }
        }
        setDeadlineExpiredAlarm(nextExpiryTime);
    }

    /**
     * Handles alarm that notifies us that a job's delay has expired. Iterates through the list of
     * tracked jobs and marks them as ready as appropriate.
     */
    private synchronized void checkExpiredDelaysAndResetAlarm() {
        final long nowElapsedMillis = SystemClock.elapsedRealtime();
        long nextDelayTime = Long.MAX_VALUE;
        boolean ready = false;
        Iterator<JobStatus> it = mTrackedJobs.iterator();
        while (it.hasNext()) {
            final JobStatus job = it.next();
            if (!job.hasTimingDelayConstraint()) {
                continue;
            }
            final long jobDelayTime = job.getEarliestRunTime();
            if (jobDelayTime <= nowElapsedMillis) {
                job.timeDelayConstraintSatisfied.set(true);
                if (canStopTrackingJob(job)) {
                    it.remove();
                }
                if (job.isReady()) {
                    ready = true;
                }
            } else {  // Keep going through list to get next delay time.
                if (nextDelayTime > jobDelayTime) {
                    nextDelayTime = jobDelayTime;
                }
            }
        }
        if (ready) {
            mStateChangedListener.onControllerStateChanged();
        }
        setDelayExpiredAlarm(nextDelayTime);
    }

    private void maybeUpdateAlarms(long delayExpiredElapsed, long deadlineExpiredElapsed) {
        if (delayExpiredElapsed < mNextDelayExpiredElapsedMillis) {
            setDelayExpiredAlarm(delayExpiredElapsed);
        }
        if (deadlineExpiredElapsed < mNextJobExpiredElapsedMillis) {
            setDeadlineExpiredAlarm(deadlineExpiredElapsed);
        }
    }

    /**
     * Set an alarm with the {@link AlarmManager} for the next time at which a job's
     * delay will expire.
     * This alarm <b>will not</b> wake up the phone.
     */
    private void setDelayExpiredAlarm(long alarmTimeElapsedMillis) {
        alarmTimeElapsedMillis = maybeAdjustAlarmTime(alarmTimeElapsedMillis);
        mNextDelayExpiredElapsedMillis = alarmTimeElapsedMillis;
        updateAlarmWithPendingIntent(mNextDelayExpiredAlarmIntent, mNextDelayExpiredElapsedMillis);
    }

    /**
     * Set an alarm with the {@link AlarmManager} for the next time at which a job's
     * deadline will expire.
     * This alarm <b>will</b> wake up the phone.
     */
    private void setDeadlineExpiredAlarm(long alarmTimeElapsedMillis) {
        alarmTimeElapsedMillis = maybeAdjustAlarmTime(alarmTimeElapsedMillis);
        mNextJobExpiredElapsedMillis = alarmTimeElapsedMillis;
        updateAlarmWithPendingIntent(mDeadlineExpiredAlarmIntent, mNextJobExpiredElapsedMillis);
    }

    private long maybeAdjustAlarmTime(long proposedAlarmTimeElapsedMillis) {
        final long earliestWakeupTimeElapsed = SystemClock.elapsedRealtime();
        if (proposedAlarmTimeElapsedMillis < earliestWakeupTimeElapsed) {
            return earliestWakeupTimeElapsed;
        }
        return proposedAlarmTimeElapsedMillis;
    }

    private void updateAlarmWithPendingIntent(PendingIntent pi, long alarmTimeElapsed) {
        ensureAlarmService();
        if (alarmTimeElapsed == Long.MAX_VALUE) {
            mAlarmService.cancel(pi);
        } else {
            mAlarmService.set(AlarmManager.ELAPSED_REALTIME, alarmTimeElapsed, pi);
        }
    }

    private final BroadcastReceiver mAlarmExpiredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // A job has just expired, so we run through the list of jobs that we have and
            // notify our StateChangedListener.
            if (ACTION_JOB_EXPIRED.equals(intent.getAction())) {
                checkExpiredDeadlinesAndResetAlarm();
            } else if (ACTION_JOB_DELAY_EXPIRED.equals(intent.getAction())) {
                checkExpiredDelaysAndResetAlarm();
            }
        }
    };

    @Override
    public void dumpControllerState(PrintWriter pw) {
        final long nowElapsed = SystemClock.elapsedRealtime();
        pw.println("Alarms (" + SystemClock.elapsedRealtime() + ")");
        pw.println(
                "Next delay alarm in " + (mNextDelayExpiredElapsedMillis - nowElapsed)/1000 + "s");
        pw.println("Next deadline alarm in " + (mNextJobExpiredElapsedMillis - nowElapsed)/1000
                + "s");
        pw.println("Tracking:");
        for (JobStatus ts : mTrackedJobs) {
            pw.println(String.valueOf(ts.hashCode()).substring(0, 3) + ".."
                    + ": (" + (ts.hasTimingDelayConstraint() ? ts.getEarliestRunTime() : "N/A")
                    + ", " + (ts.hasDeadlineConstraint() ?ts.getLatestRunTimeElapsed() : "N/A")
                    + ")");
        }
    }
}