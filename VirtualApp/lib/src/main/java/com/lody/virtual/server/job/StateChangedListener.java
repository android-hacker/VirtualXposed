package com.lody.virtual.server.job;


import com.lody.virtual.server.job.controllers.JobStatus;

/**
 * Interface through which a {@link com.lody.virtual.server.job.controllers.StateController} informs
 * the {@link JobSchedulerService} that there are some tasks potentially
 * ready to be run.
 */
public interface StateChangedListener {
    /**
     * Called by the controller to notify the JobManager that it should check on the state of a
     * task.
     */
    public void onControllerStateChanged();

    /**
     * Called by the controller to notify the JobManager that regardless of the state of the task,
     * it must be run immediately.
     * @param jobStatus The state of the task which is to be run immediately. <strong>null
     *                  indicates to the scheduler that any ready jobs should be flushed.</strong>
     */
    public void onRunJobNow(JobStatus jobStatus);
}
