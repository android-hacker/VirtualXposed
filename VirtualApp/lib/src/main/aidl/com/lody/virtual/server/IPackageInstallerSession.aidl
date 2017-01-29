package com.lody.virtual.server;

import android.content.IntentSender;
import android.os.ParcelFileDescriptor;

interface IPackageInstallerSession {
    void setClientProgress(float progress);
    void addClientProgress(float progress);

    String[] getNames();
    ParcelFileDescriptor openWrite(String name, long offsetBytes, long lengthBytes);
    ParcelFileDescriptor openRead(String name);
    void close();
    void commit(in IntentSender statusReceiver);
    void abandon();
}
