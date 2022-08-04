//package com.lody.virtual.server;
//
//import android.app.job.JobInfo;
//import android.app.job.JobParameters;
//
// /**
//  * IPC interface that supports the app-facing {@link #JobScheduler} api.
//  */
//interface IJobScheduler {
//    int schedule(in JobInfo job);
//    void cancel(int jobId);
//    void cancelAll();
//    List<JobInfo> getAllPendingJobs();
//    int enqueue(in JobInfo job, in JobParameters work);
//    JobInfo getPendingJob(int i);
//}
