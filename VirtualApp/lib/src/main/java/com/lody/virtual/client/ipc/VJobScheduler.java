package com.lody.virtual.client.ipc;

import android.app.job.JobInfo;
import android.os.RemoteException;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.service.IJobScheduler;

import java.util.List;

/**
 * @author Lody
 */

public class VJobScheduler {

    private static final VJobScheduler sInstance = new VJobScheduler();

    private IJobScheduler mRemote;

    public IJobScheduler getRemote() {
        if (mRemote == null) {
            synchronized (this) {
                if (mRemote == null) {
                    Object remote = IJobScheduler.Stub.asInterface(ServiceManagerNative.getService(ServiceManagerNative.JOB));
                    mRemote = LocalProxyUtils.genProxy(IJobScheduler.class, remote);
                }
            }
        }
        return mRemote;
    }

    public static VJobScheduler get() {
        return sInstance;
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
}
