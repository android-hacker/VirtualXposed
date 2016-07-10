// IAppObserver.aidl
package com.lody.virtual.service.interfaces;

interface IAppObserver {
    void onNewApp(String pkg);
    void onRemoveApp(String pkg);
}
