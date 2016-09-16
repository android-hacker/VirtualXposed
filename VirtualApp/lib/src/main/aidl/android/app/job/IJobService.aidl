package android.app.job;

import android.app.job.JobParameters;

/**
 * Interface that the framework uses to communicate with application code that implements a
 * JobService.  End user code does not implement this interface directly; instead, the app's
 * service implementation will extend android.app.job.JobService.
 */
interface IJobService {
    /** Begin execution of application's job. */
    void startJob(in JobParameters jobParams);
    /** Stop execution of application's job. */
    void stopJob(in JobParameters jobParams);
}
