// IProcessManager.aidl
package com.lody.virtual.service;

import com.lody.virtual.helper.proto.VComponentInfo;
import com.lody.virtual.service.interfaces.IProcessObserver;

interface IProcessManager {

    void attachClient(in IBinder clinet);

    boolean isAppProcess(String processName);

    boolean isAppPid(int pid);

    String getAppProcessName(int pid);

    List<String> getProcessPkgList(int pid);

    void killAllApps();

    void killAppByPkg(String pkg);

    void killApplicationProcess(String procName, int uid);

    void dump();

    void registerProcessObserver(in IProcessObserver observer);

    void unregisterProcessObserver(in IProcessObserver observer);

    String getInitialPackage(int pid);

    void handleApplicationCrash();

    void appDoneExecuting(in String packageName);

}
