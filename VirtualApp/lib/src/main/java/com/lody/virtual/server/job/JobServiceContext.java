package com.lody.virtual.server.job;

import android.annotation.TargetApi;
import android.app.job.IJobCallback;
import android.app.job.IJobService;
import android.app.job.JobParameters;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;

import com.lody.virtual.os.VBinder;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.am.VActivityManagerService;
import com.lody.virtual.server.job.controllers.JobStatus;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Handles client binding and lifecycle of a job. Jobs execute one at a time on an instance of this
 * class.
 *
 * There are two important interactions into this class from the
 * {@link JobSchedulerService}. To execute a job and to cancel a job.
 * - Execution of a new job is handled by the {@link #mAvailable}. This bit is flipped once when a
 * job lands, and again when it is complete.
 * - Cancelling is trickier, because there are also interactions from the client. It's possible
 * the {@link JobServiceContext.JobServiceHandler} tries to process a
 * {@link #MSG_CANCEL} after the client has already finished. This is handled by having
 * {@link JobServiceContext.JobServiceHandler#handleCancelH} check whether
 * the context is still valid.
 * To mitigate this, tearing down the context removes all messages from the handler, including any
 * tardy {@link #MSG_CANCEL}s. Additionally, we avoid sending duplicate onStopJob()
 * calls to the client after they've specified jobFinished().
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobServiceContext extends IJobCallback.Stub implements ServiceConnection {
    private static final String TAG = "JobServiceContext";
    /** Amount of time a job is allowed to execute for before being considered timed-out. */
    private static final long EXECUTING_TIMESLICE_MILLIS = 60 * 1000;
    /** Amount of time the JobScheduler will wait for a response from an app for a message. */
    private static final long OP_TIMEOUT_MILLIS = 8 * 1000;

    // States that a job occupies while interacting with the client.
    static final int VERB_BINDING = 0;
    static final int VERB_STARTING = 1;
    static final int VERB_EXECUTING = 2;
    static final int VERB_STOPPING = 3;

    // Messages that result from interactions with the client service.
    /** System timed out waiting for a response. */
    private static final int MSG_TIMEOUT = 0;
    /** Received a callback from client. */
    private static final int MSG_CALLBACK = 1;
    /** Run through list and start any ready jobs.*/
    private static final int MSG_SERVICE_BOUND = 2;
    /** Cancel a job. */
    private static final int MSG_CANCEL = 3;
    /** Shutdown the job. Used when the client crashes and we can't die gracefully.*/
    private static final int MSG_SHUTDOWN_EXECUTION = 4;

    private final Handler mCallbackHandler;
    /** Make callbacks to {@link JobSchedulerService} to inform on job completion status. */
    private final JobCompletedListener mCompletedListener;
    /** Used for service binding, etc. */
    private final Context mContext;

    // Execution state.
    private JobParameters mParams;
   
    int mVerb;
    private AtomicBoolean mCancelled = new AtomicBoolean();

    /** All the information maintained about the job currently being executed. */
    private JobStatus mRunningJob;
    /** Binder to the client service. */
    IJobService service;

    private final Object mLock = new Object();
    /**
     * Whether this context is free. This is set to false at the start of execution, and reset to
     * true when execution is complete.
     */
    private boolean mAvailable;
    /** Track start time. */
    private long mExecutionStartTimeElapsed;
    /** Track when job will timeout. */
    private long mTimeoutElapsed;

    JobServiceContext(JobSchedulerService service, Looper looper) {
        this(service.getContext(), service, looper);
    }

   
    JobServiceContext(Context context, JobCompletedListener completedListener, Looper looper) {
        mContext = context;
        mCallbackHandler = new JobServiceHandler(looper);
        mCompletedListener = completedListener;
        mAvailable = true;
    }

    /**
     * Give a job to this context for execution. Callers must first check {@link #isAvailable()}
     * to make sure this is a valid context.
     * @param job The status of the job that we are going to run.
     * @return True if the job is valid and is running. False if the job cannot be executed.
     */
    boolean executeRunnableJob(JobStatus job) {
        synchronized (mLock) {
            if (!mAvailable) {
                
                return false;
            }

            mRunningJob = job;
            mParams = mirror.android.app.job.JobParameters.ctor.newInstance(this, job.getJobId(), job.getExtras(),
                    !job.isConstraintsSatisfied());
            mExecutionStartTimeElapsed = SystemClock.elapsedRealtime();

            mVerb = VERB_BINDING;
            scheduleOpTimeOut();
            final Intent intent = new Intent().setComponent(job.getServiceComponent());
            boolean binding = VActivityManagerService.get().bindServiceAsUser(intent, this,
                    Context.BIND_AUTO_CREATE | Context.BIND_NOT_FOREGROUND,
                    new VUserHandle(job.getUserId()));
            if (!binding) {
                mRunningJob = null;
                mParams = null;
                mExecutionStartTimeElapsed = 0L;
                removeOpTimeOut();
                return false;
            }
            mAvailable = false;
            return true;
        }
    }

    /**
     * Used externally to query the running job. Will return null if there is no job running.
     * Be careful when using this function, at any moment it's possible that the job returned may
     * stop executing.
     */
    JobStatus getRunningJob() {
        synchronized (mLock) {
            return mRunningJob;
        }
    }

    /** Called externally when a job that was scheduled for execution should be cancelled. */
    void cancelExecutingJob() {
        mCallbackHandler.obtainMessage(MSG_CANCEL).sendToTarget();
    }

    /**
     * @return Whether this context is available to handle incoming work.
     */
    boolean isAvailable() {
        synchronized (mLock) {
            return mAvailable;
        }
    }

    long getExecutionStartTimeElapsed() {
        return mExecutionStartTimeElapsed;
    }

    long getTimeoutElapsed() {
        return mTimeoutElapsed;
    }

    @Override
    public void jobFinished(int jobId, boolean reschedule) {
        if (!verifyCallingUid()) {
            return;
        }
        mCallbackHandler.obtainMessage(MSG_CALLBACK, jobId, reschedule ? 1 : 0)
                .sendToTarget();
    }

    @Override
    public void acknowledgeStopMessage(int jobId, boolean reschedule) {
        if (!verifyCallingUid()) {
            return;
        }
        mCallbackHandler.obtainMessage(MSG_CALLBACK, jobId, reschedule ? 1 : 0)
                .sendToTarget();
    }

    @Override
    public void acknowledgeStartMessage(int jobId, boolean ongoing) {
        if (!verifyCallingUid()) {
            return;
        }
        mCallbackHandler.obtainMessage(MSG_CALLBACK, jobId, ongoing ? 1 : 0).sendToTarget();
    }

    /**
     * We acquire/release a wakelock on onServiceConnected/unbindService. This mirrors the work
     * we intend to send to the client - we stop sending work when the service is unbound so until
     * then we keep the wakelock.
     * @param name The concrete component name of the service that has been connected.
     * @param service The IBinder of the Service's communication channel,
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (!name.equals(mRunningJob.getServiceComponent())) {
            mCallbackHandler.obtainMessage(MSG_SHUTDOWN_EXECUTION).sendToTarget();
            return;
        }
        this.service = IJobService.Stub.asInterface(service);
        mCallbackHandler.obtainMessage(MSG_SERVICE_BOUND).sendToTarget();
    }

    /** If the client service crashes we reschedule this job and clean up. */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        mCallbackHandler.obtainMessage(MSG_SHUTDOWN_EXECUTION).sendToTarget();
    }

    /**
     * This class is reused across different clients, and passes itself in as a callback. Check
     * whether the client exercising the callback is the client we expect.
     * @return True if the binder calling is coming from the client we expect.
     */
    private boolean verifyCallingUid() {
        if (mRunningJob == null || VBinder.getCallingUid() != mRunningJob.getUid()) {
            return false;
        }
        return true;
    }

    /**
     * Handles the lifecycle of the JobService binding/callbacks, etc. The convention within this
     * class is to append 'H' to each function name that can only be called on this handler. This
     * isn't strictly necessary because all of these functions are private, but helps clarity.
     */
    private class JobServiceHandler extends Handler {
        JobServiceHandler(Looper looper) {
            super(looper);
        }

        
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_SERVICE_BOUND:
                    removeOpTimeOut();
                    handleServiceBoundH();
                    break;
                case MSG_CALLBACK:
                    removeOpTimeOut();

                    if (mVerb == VERB_STARTING) {
                        final boolean workOngoing = message.arg2 == 1;
                        handleStartedH(workOngoing);
                    } else if (mVerb == VERB_EXECUTING ||
                            mVerb == VERB_STOPPING) {
                        final boolean reschedule = message.arg2 == 1;
                        handleFinishedH(reschedule);
                    }
                    break;
                case MSG_CANCEL:
                    handleCancelH();
                    break;
                case MSG_TIMEOUT:
                    handleOpTimeoutH();
                    break;
                case MSG_SHUTDOWN_EXECUTION:
                    closeAndCleanupJobH(true /* needsReschedule */);
                    break;
                default:
                    
            }
        }

        /** Start the job on the service. */
        private void handleServiceBoundH() {
            if (mVerb != VERB_BINDING) {
                closeAndCleanupJobH(false /* reschedule */);
                return;
            }
            if (mCancelled.get()) {
                closeAndCleanupJobH(true /* reschedule */);
                return;
            }
            try {
                mVerb = VERB_STARTING;
                scheduleOpTimeOut();
                service.startJob(mParams);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * State behaviours.
         * VERB_STARTING   -> Successful start, change job to VERB_EXECUTING and post timeout.
         *     _PENDING    -> Error
         *     _EXECUTING  -> Error
         *     _STOPPING   -> Error
         */
        private void handleStartedH(boolean workOngoing) {
            switch (mVerb) {
                case VERB_STARTING:
                    mVerb = VERB_EXECUTING;
                    if (!workOngoing) {
                        // Job is finished already so fast-forward to handleFinished.
                        handleFinishedH(false);
                        return;
                    }
                    if (mCancelled.get()) {
                        // Cancelled *while* waiting for acknowledgeStartMessage from client.
                        handleCancelH();
                        return;
                    }
                    scheduleOpTimeOut();
                    break;
                default:
            }
        }

        /**
         * VERB_EXECUTING  -> Client called jobFinished(), clean up and notify done.
         *     _STOPPING   -> Successful finish, clean up and notify done.
         *     _STARTING   -> Error
         *     _PENDING    -> Error
         */
        private void handleFinishedH(boolean reschedule) {
            switch (mVerb) {
                case VERB_EXECUTING:
                case VERB_STOPPING:
                    closeAndCleanupJobH(reschedule);
                    break;
                default:
                    break;
            }
        }

        /**
         * A job can be in various states when a cancel request comes in:
         * VERB_BINDING    -> Cancelled before bind completed. Mark as cancelled and wait for
         *                    {@link #onServiceConnected(ComponentName, IBinder)}
         *     _STARTING   -> Mark as cancelled and wait for
         *                    {@link JobServiceContext#acknowledgeStartMessage(int, boolean)}
         *     _EXECUTING  -> call {@link #sendStopMessageH}}, but only if there are no callbacks
         *                      in the message queue.
         *     _ENDING     -> No point in doing anything here, so we ignore.
         */
        private void handleCancelH() {
            if (mRunningJob == null) {
                return;
            }
            switch (mVerb) {
                case VERB_BINDING:
                case VERB_STARTING:
                    mCancelled.set(true);
                    break;
                case VERB_EXECUTING:
                    if (hasMessages(MSG_CALLBACK)) {
                        // If the client has called jobFinished, ignore this cancel.
                        return;
                    }
                    sendStopMessageH();
                    break;
                case VERB_STOPPING:
                    // Nada.
                    break;
                default:
                    break;
            }
        }

        /** Process MSG_TIMEOUT here. */
        private void handleOpTimeoutH() {
            switch (mVerb) {
                case VERB_BINDING:
                    closeAndCleanupJobH(false /* needsReschedule */);
                    break;
                case VERB_STARTING:
                    // Client unresponsive - wedged or failed to respond in time. We don't really
                    // know what happened so let's log it and notify the JobScheduler
                    // FINISHED/NO-RETRY.
                    closeAndCleanupJobH(false /* needsReschedule */);
                    break;
                case VERB_STOPPING:
                    // At least we got somewhere, so fail but ask the JobScheduler to reschedule.

                    closeAndCleanupJobH(true /* needsReschedule */);
                    break;
                case VERB_EXECUTING:
                    // Not an error - client ran out of time.
                    sendStopMessageH();
                    break;
                default:
                    closeAndCleanupJobH(false /* needsReschedule */);
            }
        }

        /**
         * Already running, need to stop. Will switch {@link #mVerb} from VERB_EXECUTING ->
         * VERB_STOPPING.
         */
        private void sendStopMessageH() {
            removeOpTimeOut();
            if (mVerb != VERB_EXECUTING) {
                
                closeAndCleanupJobH(false /* reschedule */);
                return;
            }
            try {
                mVerb = VERB_STOPPING;
                scheduleOpTimeOut();
                service.stopJob(mParams);
            } catch (RemoteException e) {
                
                closeAndCleanupJobH(false /* reschedule */);
            }
        }

        /**
         * The provided job has finished, either by calling
         * {@link android.app.job.JobService#jobFinished(JobParameters, boolean)}
         * or from acknowledging the stop message we sent. Either way, we're done tracking it and
         * we want to clean up internally.
         */
        private void closeAndCleanupJobH(boolean reschedule) {
            final JobStatus completedJob = mRunningJob;
            synchronized (mLock) {
                mContext.unbindService(JobServiceContext.this);
                mRunningJob = null;
                mParams = null;
                mVerb = -1;
                mCancelled.set(false);
                service = null;
                mAvailable = true;
            }
            removeOpTimeOut();
            removeMessages(MSG_CALLBACK);
            removeMessages(MSG_SERVICE_BOUND);
            removeMessages(MSG_CANCEL);
            removeMessages(MSG_SHUTDOWN_EXECUTION);
            mCompletedListener.onJobCompleted(completedJob, reschedule);
        }
    }

    /**
     * Called when sending a message to the client, over whose execution we have no control. If
     * we haven't received a response in a certain amount of time, we want to give up and carry
     * on with life.
     */
    private void scheduleOpTimeOut() {
        removeOpTimeOut();

        final long timeoutMillis = (mVerb == VERB_EXECUTING) ?
                EXECUTING_TIMESLICE_MILLIS : OP_TIMEOUT_MILLIS;
        Message m = mCallbackHandler.obtainMessage(MSG_TIMEOUT);
        mCallbackHandler.sendMessageDelayed(m, timeoutMillis);
        mTimeoutElapsed = SystemClock.elapsedRealtime() + timeoutMillis;
    }


    private void removeOpTimeOut() {
        mCallbackHandler.removeMessages(MSG_TIMEOUT);
    }
}
