// IAppRequestListener.aidl
package com.lody.virtual.server.interfaces;

interface IAppRequestListener {
    void onRequestInstall(in String path);
    void onRequestUninstall(in String pkg);
}
