package com.lody.virtual.server.log;

import android.os.RemoteException;

import com.lody.virtual.server.ILogManager;

import java.util.ArrayList;

/**
 * @author Lody
 */

public class LogReportService extends ILogManager.Stub {

    private final ArrayList<ILogAgent> mLogAgents = new ArrayList<>(1);
    private static final LogReportService sInstance = new LogReportService();

    public static LogReportService get() {
        return sInstance;
    }

    @Override
    public void reportLog(int level, String tag, String msg) throws RemoteException {
        for (ILogAgent agent : mLogAgents) {
            try {
                agent.reportLog(level, tag, msg);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void addLogAgent(ILogAgent agent) {
        mLogAgents.add(agent);
    }

    public void removeLogAgent(ILogAgent agent) {
        mLogAgents.remove(agent);
    }

    public void clearLogAgents() {
        mLogAgents.clear();
    }
}
