// IProcessObserver.aidl
package com.lody.virtual.service.interfaces;

interface IProcessObserver {
    void onProcessCreated(in String pkg, in String processName);

    void onProcessDied(in String pkg, in String processName);
}
