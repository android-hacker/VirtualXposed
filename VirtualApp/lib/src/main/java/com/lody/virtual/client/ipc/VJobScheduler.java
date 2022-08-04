package com.lody.virtual.client.ipc;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobWorkItem;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.server.IJobScheduler;

import java.util.List;

/**
 * @author Lody
 */

public class VJobScheduler {

    private static final VJobScheduler sInstance = new VJobScheduler();

    private IJobScheduler mRemote;

    public static VJobScheduler get() {
        return sInstance;
    }

    public IJobScheduler getRemote() {
        if (mRemote == null ||
                (!mRemote.asBinder().pingBinder() && !VirtualCore.get().isVAppProcess())) {
            synchronized (this) {
                Object remote = getRemoteInterface();
                mRemote = LocalProxyUtils.genProxy(IJobScheduler.class, remote);
            }
        }
        return mRemote;
    }

    private Object getRemoteInterface() {
        final IBinder binder = ServiceManagerNative.getService(ServiceManagerNative.JOB);
        return IJobScheduler.Stub.asInterface(binder);
    }

    public int schedule(JobInfo job) {
        try {
            return getRemote().schedule(job);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<JobInfo> getAllPendingJobs() {
        try {
            return getRemote().getAllPendingJobs();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void cancelAll() {
        try {
            getRemote().cancelAll();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void cancel(int jobId) {
        try {
            getRemote().cancel(jobId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public JobInfo getPendingJob(int jobId) {
        try {
            return getRemote().getPendingJob(jobId);
        } catch (RemoteException e) {
            return (JobInfo) VirtualRuntime.crash(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public int enqueue(JobInfo job, Object workItem) {
        if (workItem == null) {
            return -1;
        }
        try {
            return getRemote().enqueue(job, (JobWorkItem) workItem);
        } catch (RemoteException e) {
            return (Integer) VirtualRuntime.crash(e);
        }
    }
}
