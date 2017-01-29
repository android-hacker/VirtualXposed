// IAppObserver.aidl
package com.lody.virtual.server.interfaces;

interface IAppObserver {
    void onNewApp(String pkg);
    void onRemoveApp(String pkg);
}
