package com.lody.virtual.server;

import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.IPackageInstallerSession;
import android.content.IntentSender;
import android.graphics.Bitmap;

import com.lody.virtual.remote.VParceledListSlice;
import com.lody.virtual.server.pm.installer.SessionParams;
import com.lody.virtual.server.pm.installer.SessionInfo;

interface IPackageInstaller {
    int createSession(in SessionParams params, String installerPackageName, int userId);

    void updateSessionAppIcon(int sessionId, in Bitmap appIcon);
    void updateSessionAppLabel(int sessionId, String appLabel);

    void abandonSession(int sessionId);

    IPackageInstallerSession openSession(int sessionId);

    SessionInfo getSessionInfo(int sessionId);

    VParceledListSlice getAllSessions(int userId);
    VParceledListSlice getMySessions(String installerPackageName, int userId);

    void registerCallback(IPackageInstallerCallback callback, int userId);
    void unregisterCallback(IPackageInstallerCallback callback);

    void uninstall(String packageName, String callerPackageName, int flags,
            in IntentSender statusReceiver, int userId);

    void setPermissionsResult(int sessionId, boolean accepted);
}
