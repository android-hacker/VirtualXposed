package com.lody.virtual.server.job;


import com.lody.virtual.server.job.controllers.JobStatus;

/**
 * Used for communication between {@link JobServiceContext} and the
 * {@link JobSchedulerService}.
 */
public interface JobCompletedListener {

    /**
     * Callback for when a job is completed.
     * @param needsReschedule Whether the implementing class should reschedule this job.
     */
    void onJobCompleted(JobStatus jobStatus, boolean needsReschedule);
}
