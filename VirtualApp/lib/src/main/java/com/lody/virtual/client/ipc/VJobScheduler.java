package com.lody.virtual.client.ipc;

import android.app.job.JobInfo;
import android.os.RemoteException;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.helper.ipcbus.IPCSingleton;
import com.lody.virtual.server.IJobScheduler;

import java.util.List;

/**
 * @author Lody
 */

public class VJobScheduler {

    private static final VJobScheduler sInstance = new VJobScheduler();

    private IPCSingleton<IJobScheduler> singleton = new IPCSingleton<>(IJobScheduler.class);

    public static VJobScheduler get() {
        return sInstance;
    }

    public IJobScheduler getService() {
        return singleton.get();
    }

    public int schedule(JobInfo job) {
        try {
            return getService().schedule(job);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<JobInfo> getAllPendingJobs() {
        try {
            return getService().getAllPendingJobs();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void cancelAll() {
        try {
            getService().cancelAll();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void cancel(int jobId) {
        try {
            getService().cancel(jobId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
