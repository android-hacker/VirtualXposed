package com.lody.virtual.server.job;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.utils.collection.ArraySet;
import com.lody.virtual.helper.utils.collection.SparseArray;
import com.lody.virtual.os.VBinder;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.job.controllers.IdleController;
import com.lody.virtual.server.job.controllers.JobStatus;
import com.lody.virtual.server.job.controllers.StateController;
import com.lody.virtual.server.job.controllers.TimeController;
import com.lody.virtual.server.pm.VPackageManagerService;
import com.lody.virtual.service.IJobScheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Responsible for taking jobs representing work to be performed by a client app, and determining
 * based on the criteria specified when that job should be run against the client application's
 * endpoint.
 * Implements logic for scheduling, and rescheduling jobs. The JobSchedulerService knows nothing
 * about constraints, or the state of active jobs. It receives callbacks from the various
 * controllers and completed jobs and operates accordingly.
 *
 * Note on locking: Any operations that manipulate {@link #mJobs} need to lock on that object.
 * Any function with the suffix 'Locked' also needs to lock on {@link #mJobs}.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService implements StateChangedListener, JobCompletedListener {

    private static AtomicReference<JobSchedulerService> sThis = new AtomicReference<>();

    static final boolean DEBUG = false;
    /** The number of concurrent jobs we run at one time. */
    private static final int MAX_JOB_CONTEXTS_COUNT = 3;
    static final String TAG = "JobSchedulerService";
    /** Master list of jobs. */
    final JobStore mJobs;

    static final int MSG_JOB_EXPIRED = 0;
    static final int MSG_CHECK_JOB = 1;

    // Policy constants
    /**
     * Minimum # of idle jobs that must be ready in order to force the JMS to schedule things
     * early.
     */
    static final int MIN_IDLE_COUNT = 1;
    /**
     * Minimum # of charging jobs that must be ready in order to force the JMS to schedule things
     * early.
     */
    static final int MIN_CHARGING_COUNT = 1;
    /**
     * Minimum # of connectivity jobs that must be ready in order to force the JMS to schedule
     * things early.
     */
    static final int MIN_CONNECTIVITY_COUNT = 2;
    /**
     * Minimum # of jobs (with no particular constraints) for which the JMS will be happy running
     * some work early.
     * This is correlated with the amount of batching we'll be able to do.
     */
    static final int MIN_READY_JOBS_COUNT = 2;

    /**
     * Track Services that have currently active or pending jobs. The index is provided by
     * {@link JobStatus#getServiceToken()}
     */
    final List<JobServiceContext> mActiveServices = new ArrayList<JobServiceContext>();
    /** List of controllers that will notify this service of updates to jobs. */
    List<StateController> mControllers;
    /**
     * Queue of pending jobs. The JobServiceContext class will receive jobs from this list
     * when ready to execute them.
     */
    final ArrayList<JobStatus> mPendingJobs = new ArrayList<JobStatus>();

    final ArrayList<Integer> mStartedUsers = new ArrayList();

    final JobHandler mHandler;
    final JobSchedulerStub mJobSchedulerStub;
    
    /**
     * Set to true once we are allowed to run third party apps.
     */
    boolean mReadyToRock;

    /**
     * Cleans up outstanding jobs when a package is removed. Even if it's being replaced later we
     * still clean up. On reinstall the package will have a new uid.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        
        public void onReceive(Context context, Intent intent) {
            
            if (Constants.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                int uidRemoved = intent.getIntExtra(Intent.EXTRA_UID, -1);
                cancelJobsForUid(uidRemoved);
            } else if (Constants.ACTION_USER_REMOVED.equals(intent.getAction())) {
                final int userId = intent.getIntExtra(Constants.EXTRA_USER_HANDLE, 0);
                cancelJobsForUser(userId);
            }
        }
    };

    public static JobSchedulerService get() {
        return sThis.get();
    }

    public static JobSchedulerStub getStub() {
        return sThis.get().mJobSchedulerStub;
    }

    public static void systemReady(Context context) {
        JobSchedulerService service = new JobSchedulerService(context);
        service.onBootPhase();
        sThis.set(service);
    }

    public void onStartUser(int userHandle) {
        mStartedUsers.add(userHandle);
        // Let's kick any outstanding jobs for this user.
        mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    public void onStopUser(int userHandle) {
        mStartedUsers.remove(Integer.valueOf(userHandle));
    }

    /**
     * Entry point from client to schedule the provided job.
     * This cancels the job if it's already been scheduled, and replaces it with the one provided.
     * @param job JobInfo object containing execution parameters
     * @param uId The package identifier of the application this job is for.
     * @return Result of this operation. See <code>JobScheduler#RESULT_*</code> return codes.
     */
    public int schedule(JobInfo job, int uId) {
        JobStatus jobStatus = new JobStatus(job, uId);
        cancelJob(uId, job.getId());
        startTrackingJob(jobStatus);
        mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
        return JobScheduler.RESULT_SUCCESS;
    }

    public List<JobInfo> getPendingJobs(int uid) {
        ArrayList<JobInfo> outList = new ArrayList<JobInfo>();
        synchronized (mJobs) {
            ArraySet<JobStatus> jobs = mJobs.getJobs();
            for (int i=0; i<jobs.size(); i++) {
                JobStatus job = jobs.valueAt(i);
                if (job.getUid() == uid) {
                    outList.add(job.getJob());
                }
            }
        }
        return outList;
    }

    private void cancelJobsForUser(int userHandle) {
        List<JobStatus> jobsForUser;
        synchronized (mJobs) {
            jobsForUser = mJobs.getJobsByUser(userHandle);
        }
        for (int i=0; i<jobsForUser.size(); i++) {
            JobStatus toRemove = jobsForUser.get(i);
            cancelJobImpl(toRemove);
        }
    }

    /**
     * Entry point from client to cancel all jobs originating from their uid.
     * This will remove the job from the master list, and cancel the job if it was staged for
     * execution or being executed.
     * @param uid Uid to check against for removal of a job.
     */
    public void cancelJobsForUid(int uid) {
        List<JobStatus> jobsForUid;
        synchronized (mJobs) {
            jobsForUid = mJobs.getJobsByUid(uid);
        }
        for (int i=0; i<jobsForUid.size(); i++) {
            JobStatus toRemove = jobsForUid.get(i);
            cancelJobImpl(toRemove);
        }
    }

    /**
     * Entry point from client to cancel the job corresponding to the jobId provided.
     * This will remove the job from the master list, and cancel the job if it was staged for
     * execution or being executed.
     * @param uid Uid of the calling client.
     * @param jobId Id of the job, provided at schedule-time.
     */
    public void cancelJob(int uid, int jobId) {
        JobStatus toCancel;
        synchronized (mJobs) {
            toCancel = mJobs.getJobByUidAndJobId(uid, jobId);
        }
        if (toCancel != null) {
            cancelJobImpl(toCancel);
        }
    }

    private void cancelJobImpl(JobStatus cancelled) {
        if (DEBUG) {
            
        }
        stopTrackingJob(cancelled);
        synchronized (mJobs) {
            // Remove from pending queue.
            mPendingJobs.remove(cancelled);
            // Cancel if running.
            stopJobOnServiceContextLocked(cancelled);
        }
    }

    /**
     * Initializes the system service.
     * <p>
     * Subclasses must define a single argument constructor that accepts the context
     * and passes it to super.
     * </p>
     *
     * @param context The system server context.
     */
    public JobSchedulerService(Context context) {
        // Create the controllers.
        mControllers = new ArrayList<StateController>();
        mControllers.add(TimeController.get(this));
        mControllers.add(IdleController.get(this));

        mHandler = new JobHandler(context.getMainLooper());
        mJobSchedulerStub = new JobSchedulerStub();
        mJobs = JobStore.initAndGet(this);
    }


    public void onBootPhase() {
        synchronized (mJobs) {
            // Let's go!
            mReadyToRock = true;
            // Create the "runners".
            for (int i = 0; i < MAX_JOB_CONTEXTS_COUNT; i++) {
                mActiveServices.add(
                        new JobServiceContext(this, getContext().getMainLooper()));
            }
            // Attach jobs to their controllers.
            ArraySet<JobStatus> jobs = mJobs.getJobs();
            for (int i=0; i<jobs.size(); i++) {
                JobStatus job = jobs.valueAt(i);
                for (int controller = 0; controller<mControllers.size(); controller++) {
                    mControllers.get(controller).maybeStartTrackingJob(job);
                }
            }
            // GO GO GO!
            mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
        }
    }

    /**
     * Called when we have a job status object that we need to insert in our
     * {@link JobStore}, and make sure all the relevant controllers know
     * about.
     */
    private void startTrackingJob(JobStatus jobStatus) {
        boolean update;
        boolean rocking;
        synchronized (mJobs) {
            update = mJobs.add(jobStatus);
            rocking = mReadyToRock;
        }
        if (rocking) {
            for (int i=0; i<mControllers.size(); i++) {
                StateController controller = mControllers.get(i);
                if (update) {
                    controller.maybeStopTrackingJob(jobStatus);
                }
                controller.maybeStartTrackingJob(jobStatus);
            }
        }
    }

    /**
     * Called when we want to remove a JobStatus object that we've finished executing. Returns the
     * object removed.
     */
    private boolean stopTrackingJob(JobStatus jobStatus) {
        boolean removed;
        boolean rocking;
        synchronized (mJobs) {
            // Remove from store as well as controllers.
            removed = mJobs.remove(jobStatus);
            rocking = mReadyToRock;
        }
        if (removed && rocking) {
            for (int i=0; i<mControllers.size(); i++) {
                StateController controller = mControllers.get(i);
                controller.maybeStopTrackingJob(jobStatus);
            }
        }
        return removed;
    }

    private boolean stopJobOnServiceContextLocked(JobStatus job) {
        for (int i=0; i < mActiveServices.size(); i++) {
            JobServiceContext jsc = mActiveServices.get(i);
            final JobStatus executing = jsc.getRunningJob();
            if (executing != null && executing.matches(job.getUid(), job.getJobId())) {
                jsc.cancelExecutingJob();
                return true;
            }
        }
        return false;
    }

    /**
     * @param job JobStatus we are querying against.
     * @return Whether or not the job represented by the status object is currently being run or
     * is pending.
     */
    private boolean isCurrentlyActiveLocked(JobStatus job) {
        for (int i=0; i<mActiveServices.size(); i++) {
            JobServiceContext serviceContext = mActiveServices.get(i);
            final JobStatus running = serviceContext.getRunningJob();
            if (running != null && running.matches(job.getUid(), job.getJobId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * A job is rescheduled with exponential back-off if the client requests this from their
     * execution logic.
     * A caveat is for idle-mode jobs, for which the idle-mode constraint will usurp the
     * timeliness of the reschedule. For an idle-mode job, no deadline is given.
     * @param failureToReschedule Provided job status that we will reschedule.
     * @return A newly instantiated JobStatus with the same constraints as the last job except
     * with adjusted timing constraints.
     */
    private JobStatus getRescheduleJobForFailure(JobStatus failureToReschedule) {
        final long elapsedNowMillis = SystemClock.elapsedRealtime();
        final JobInfo job = failureToReschedule.getJob();

        final long initialBackoffMillis = job.getInitialBackoffMillis();
        final int backoffAttempts = failureToReschedule.getNumFailures() + 1;
        long delayMillis;

        switch (job.getBackoffPolicy()) {
            case JobInfo.BACKOFF_POLICY_LINEAR:
                delayMillis = initialBackoffMillis * backoffAttempts;
                break;
            default:
                if (DEBUG) {
                    
                }
            case JobInfo.BACKOFF_POLICY_EXPONENTIAL:
                delayMillis =
                        (long) Math.scalb(initialBackoffMillis, backoffAttempts - 1);
                break;
        }
        delayMillis =
                Math.min(delayMillis, JobInfo.MAX_BACKOFF_DELAY_MILLIS);
        return new JobStatus(failureToReschedule, elapsedNowMillis + delayMillis,
                JobStatus.NO_LATEST_RUNTIME, backoffAttempts);
    }

    /**
     * Called after a periodic has executed so we can to re-add it. We take the last execution time
     * of the job to be the time of completion (i.e. the time at which this function is called).
     * This could be inaccurate b/c the job can run for as long as
     * {@link JobServiceContext#EXECUTING_TIMESLICE_MILLIS}, but will lead
     * to underscheduling at least, rather than if we had taken the last execution time to be the
     * start of the execution.
     * @return A new job representing the execution criteria for this instantiation of the
     * recurring job.
     */
    private JobStatus getRescheduleJobForPeriodic(JobStatus periodicToReschedule) {
        final long elapsedNow = SystemClock.elapsedRealtime();
        // Compute how much of the period is remaining.
        long runEarly = Math.max(periodicToReschedule.getLatestRunTimeElapsed() - elapsedNow, 0);
        long newEarliestRunTimeElapsed = elapsedNow + runEarly;
        long period = periodicToReschedule.getJob().getIntervalMillis();
        long newLatestRuntimeElapsed = newEarliestRunTimeElapsed + period;
        return new JobStatus(periodicToReschedule, newEarliestRunTimeElapsed,
                newLatestRuntimeElapsed, 0 /* backoffAttempt */);
    }

    // JobCompletedListener implementations.

    /**
     * A job just finished executing. We fetch the
     * {@link JobStatus} from the store and depending on
     * whether we want to reschedule we readd it to the controllers.
     * @param jobStatus Completed job.
     * @param needsReschedule Whether the implementing class should reschedule this job.
     */
    
    public void onJobCompleted(JobStatus jobStatus, boolean needsReschedule) {
        if (!stopTrackingJob(jobStatus)) {
            return;
        }
        if (needsReschedule) {
            JobStatus rescheduled = getRescheduleJobForFailure(jobStatus);
            startTrackingJob(rescheduled);
        } else if (jobStatus.getJob().isPeriodic()) {
            JobStatus rescheduledPeriodic = getRescheduleJobForPeriodic(jobStatus);
            startTrackingJob(rescheduledPeriodic);
        }
        mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    // StateChangedListener implementations.

    /**
     * Posts a message to the {@link JobSchedulerService.JobHandler} that
     * some controller's state has changed, so as to run through the list of jobs and start/stop
     * any that are eligible.
     */
    
    public void onControllerStateChanged() {
        mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    
    public void onRunJobNow(JobStatus jobStatus) {
        mHandler.obtainMessage(MSG_JOB_EXPIRED, jobStatus).sendToTarget();
    }

    public Context getContext() {
        return VirtualCore.get().getContext();
    }

    private class JobHandler extends Handler {

        public JobHandler(Looper looper) {
            super(looper);
        }

        
        public void handleMessage(Message message) {
            synchronized (mJobs) {
                if (!mReadyToRock) {
                    return;
                }
            }
            switch (message.what) {
                case MSG_JOB_EXPIRED:
                    synchronized (mJobs) {
                        JobStatus runNow = (JobStatus) message.obj;
                        // runNow can be null, which is a controller's way of indicating that its
                        // state is such that all ready jobs should be run immediately.
                        if (runNow != null && !mPendingJobs.contains(runNow)
                                && mJobs.containsJob(runNow)) {
                            mPendingJobs.add(runNow);
                        }
                        queueReadyJobsForExecutionLockedH();
                    }
                    break;
                case MSG_CHECK_JOB:
                    synchronized (mJobs) {
                        // Check the list of jobs and run some of them if we feel inclined.
                        maybeQueueReadyJobsForExecutionLockedH();
                    }
                    break;
            }
            maybeRunPendingJobsH();
            // Don't remove JOB_EXPIRED in case one came along while processing the queue.
            removeMessages(MSG_CHECK_JOB);
        }

        /**
         * Run through list of jobs and execute all possible - at least one is expired so we do
         * as many as we can.
         */
        private void queueReadyJobsForExecutionLockedH() {
            ArraySet<JobStatus> jobs = mJobs.getJobs();
            if (DEBUG) {
                
            }
            for (int i=0; i<jobs.size(); i++) {
                JobStatus job = jobs.valueAt(i);
                if (isReadyToBeExecutedLocked(job)) {
                    if (DEBUG) {
                        
                    }
                    mPendingJobs.add(job);
                } else if (isReadyToBeCancelledLocked(job)) {
                    stopJobOnServiceContextLocked(job);
                }
            }
            if (DEBUG) {
                final int queuedJobs = mPendingJobs.size();
                if (queuedJobs == 0) {
                    
                } else {
                    
                }
            }
        }

        /**
         * The state of at least one job has changed. Here is where we could enforce various
         * policies on when we want to execute jobs.
         * Right now the policy is such:
         * If >1 of the ready jobs is idle mode we send all of them off
         * if more than 2 network connectivity jobs are ready we send them all off.
         * If more than 4 jobs total are ready we send them all off.
         * TODO: It would be nice to consolidate these sort of high-level policies somewhere.
         */
        private void maybeQueueReadyJobsForExecutionLockedH() {
            int chargingCount = 0;
            int idleCount =  0;
            int backoffCount = 0;
            int connectivityCount = 0;
            List<JobStatus> runnableJobs = new ArrayList<JobStatus>();
            ArraySet<JobStatus> jobs = mJobs.getJobs();
            for (int i=0; i<jobs.size(); i++) {
                JobStatus job = jobs.valueAt(i);
                if (isReadyToBeExecutedLocked(job)) {
                    if (job.getNumFailures() > 0) {
                        backoffCount++;
                    }
                    if (job.hasIdleConstraint()) {
                        idleCount++;
                    }
                    if (job.hasConnectivityConstraint() || job.hasUnmeteredConstraint()) {
                        connectivityCount++;
                    }
                    if (job.hasChargingConstraint()) {
                        chargingCount++;
                    }
                    runnableJobs.add(job);
                } else if (isReadyToBeCancelledLocked(job)) {
                    stopJobOnServiceContextLocked(job);
                }
            }
            if (backoffCount > 0 ||
                    idleCount >= MIN_IDLE_COUNT ||
                    connectivityCount >= MIN_CONNECTIVITY_COUNT ||
                    chargingCount >= MIN_CHARGING_COUNT ||
                    runnableJobs.size() >= MIN_READY_JOBS_COUNT) {
                for (int i=0; i<runnableJobs.size(); i++) {
                    mPendingJobs.add(runnableJobs.get(i));
                }
            }
        }

        /**
         * Criteria for moving a job into the pending queue:
         *      - It's ready.
         *      - It's not pending.
         *      - It's not already running on a JSC.
         *      - The user that requested the job is running.
         */
        private boolean isReadyToBeExecutedLocked(JobStatus job) {
            final boolean jobReady = job.isReady();
            final boolean jobPending = mPendingJobs.contains(job);
            final boolean jobActive = isCurrentlyActiveLocked(job);
            final boolean userRunning = mStartedUsers.contains(job.getUserId());
            return userRunning && jobReady && !jobPending && !jobActive;
        }

        /**
         * Criteria for cancelling an active job:
         *      - It's not ready
         *      - It's running on a JSC.
         */
        private boolean isReadyToBeCancelledLocked(JobStatus job) {
            return !job.isReady() && isCurrentlyActiveLocked(job);
        }

        /**
         * Reconcile jobs in the pending queue against available execution contexts.
         * A controller can force a job into the pending queue even if it's already running, but
         * here is where we decide whether to actually execute it.
         */
        private void maybeRunPendingJobsH() {
            synchronized (mJobs) {
                Iterator<JobStatus> it = mPendingJobs.iterator();
                if (DEBUG) {
                    
                }
                while (it.hasNext()) {
                    JobStatus nextPending = it.next();
                    JobServiceContext availableContext = null;
                    for (int i=0; i<mActiveServices.size(); i++) {
                        JobServiceContext jsc = mActiveServices.get(i);
                        final JobStatus running = jsc.getRunningJob();
                        if (running != null && running.matches(nextPending.getUid(),
                                nextPending.getJobId())) {
                            // Already running this job for this uId, skip.
                            availableContext = null;
                            break;
                        }
                        if (jsc.isAvailable()) {
                            availableContext = jsc;
                        }
                    }
                    if (availableContext != null) {
                        if (!availableContext.executeRunnableJob(nextPending)) {
                            if (DEBUG) {
                                
                            }
                            mJobs.remove(nextPending);
                        }
                        it.remove();
                    }
                }
            }
        }
    }

    /**
     * Binder stub trampoline implementation
     */
    final class JobSchedulerStub extends IJobScheduler.Stub {
        /** Cache determination of whether a given app can persist jobs
         * key is uid of the calling app; value is undetermined/true/false
         */
        private final SparseArray<Boolean> mPersistCache = new SparseArray<>();

        // Enforce that only the app itself (or shared uid participant) can schedule a
        // job that runs one of the app's services, as well as verifying that the
        // named service properly requires the BIND_JOB_SERVICE permission
        private void enforceValidJobRequest(int uid, JobInfo job) {
            final ComponentName service = job.getService();
            ServiceInfo si = VPackageManagerService.get().getServiceInfo(service, 0, VUserHandle.getUserId(uid));
            if (si == null) {
                throw new IllegalArgumentException("No such service " + service);
            }
            if (si.applicationInfo.uid != uid) {
                throw new IllegalArgumentException("uid " + uid +
                        " cannot schedule job in " + service.getPackageName());
            }
            if (!JobService.PERMISSION_BIND.equals(si.permission)) {
                throw new IllegalArgumentException("Scheduled service " + service
                        + " does not require android.permission.BIND_JOB_SERVICE permission");
            }
        }

        private boolean canPersistJobs(int pid, int uid) {
            // If we get this far we're good to go; all we need to do now is check
            // whether the app is allowed to persist its scheduled work.
            final boolean canPersist;
            synchronized (mPersistCache) {
                Boolean cached = mPersistCache.get(uid);
                if (cached != null) {
                    canPersist = cached;
                } else {
                    // Persisting jobs is tantamount to running at boot, so we permit
                    // it when the app has declared that it uses the RECEIVE_BOOT_COMPLETED
                    // permission
                    canPersist = true;
                    mPersistCache.put(uid, canPersist);
                }
            }
            return canPersist;
        }

        // IJobScheduler implementation
        
        public int schedule(JobInfo job) throws RemoteException {
            if (DEBUG) {
                
            }
            final int pid = VBinder.getCallingPid();
            final int uid = VBinder.getCallingUid();

            enforceValidJobRequest(uid, job);
            if (job.isPersisted()) {
                if (!canPersistJobs(pid, uid)) {
                    throw new IllegalArgumentException("Error: requested job be persisted without"
                            + " holding RECEIVE_BOOT_COMPLETED permission.");
                }
            }

            long ident = Binder.clearCallingIdentity();
            try {
                return JobSchedulerService.this.schedule(job, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        @Override
        public List<JobInfo> getAllPendingJobs() throws RemoteException {
            final int uid = VBinder.getCallingUid();

            long ident = Binder.clearCallingIdentity();
            try {
                return JobSchedulerService.this.getPendingJobs(uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        
        public void cancelAll() throws RemoteException {
            final int uid = VBinder.getCallingUid();

            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.cancelJobsForUid(uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        
        public void cancel(int jobId) throws RemoteException {
            final int uid = VBinder.getCallingUid();

            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.cancelJob(uid, jobId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

    };

}
