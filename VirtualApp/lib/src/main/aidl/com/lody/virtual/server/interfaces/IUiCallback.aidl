// IUiCallback.aidl
package com.lody.virtual.server.interfaces;

interface IUiCallback {
    void onAppOpened(in String packageName, in int userId);
}
