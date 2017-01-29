package android.content.pm;

import android.content.pm.IPackageInstallObserver2;
import android.content.IntentSender;
import android.os.ParcelFileDescriptor;

interface IPackageInstallerSession {
    void setClientProgress(float progress);
    void addClientProgress(float progress);

    String[] getNames();
    ParcelFileDescriptor openWrite(String name, long offsetBytes, long lengthBytes);
    ParcelFileDescriptor openRead(String name);

    void removeSplit(String splitName);

    void close();
    void commit(in IntentSender statusReceiver);
    void abandon();
}
