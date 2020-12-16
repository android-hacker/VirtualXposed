package com.lody.virtual.server.pm.installer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.IPackageInstallerSession;
import android.content.pm.PackageInstaller;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.SparseArray;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.ObjectsCompat;
import com.lody.virtual.helper.utils.Singleton;
import com.lody.virtual.os.VBinder;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.VParceledListSlice;
import com.lody.virtual.server.IPackageInstaller;
import com.lody.virtual.server.pm.VAppManagerService;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.lody.virtual.server.pm.installer.PackageHelper.installStatusToPublicStatus;
import static com.lody.virtual.server.pm.installer.PackageHelper.installStatusToString;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VPackageInstallerService extends IPackageInstaller.Stub {

    private static final String TAG = "PackageInstaller";

    /**
     * Upper bound on number of active sessions for a UID
     */
    private static final long MAX_ACTIVE_SESSIONS = 1024;
    private static final Singleton<VPackageInstallerService> gDefault = new Singleton<VPackageInstallerService>() {
        @Override
        protected VPackageInstallerService create() {
            return new VPackageInstallerService();
        }
    };
    /**
     * Used for generating session IDs. Since this is created at boot time,
     * normal random might be predictable.
     */
    private final Random mRandom = new SecureRandom();
    private final SparseArray<PackageInstallerSession> mSessions = new SparseArray<>();
    private final Handler mInstallHandler;
    private final Callbacks mCallbacks;
    private final HandlerThread mInstallThread;
    private final InternalCallback mInternalCallback = new InternalCallback();
    private Context mContext;

    private VPackageInstallerService() {
        mContext = VirtualCore.get().getContext();
        mInstallThread = new HandlerThread("PackageInstaller");
        mInstallThread.start();
        mInstallHandler = new Handler(mInstallThread.getLooper());
        mCallbacks = new Callbacks(mInstallThread.getLooper());
    }

    public static VPackageInstallerService get() {
        return gDefault.get();
    }

    private static int getSessionCount(SparseArray<PackageInstallerSession> sessions,
                                       int installerUid) {
        int count = 0;
        final int size = sessions.size();
        for (int i = 0; i < size; i++) {
            final PackageInstallerSession session = sessions.valueAt(i);
            if (session.installerUid == installerUid) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int createSession(SessionParams params, String installerPackageName, int userId) throws RemoteException {
        try {
            return createSessionInternal(params, installerPackageName, userId);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private int createSessionInternal(SessionParams params, String installerPackageName, int userId)
            throws IOException {
        final int callingUid = VBinder.getCallingUid();
        final int sessionId;
        final PackageInstallerSession session;
        synchronized (mSessions) {
            // Sanity check that installer isn't going crazy
            final int activeCount = getSessionCount(mSessions, callingUid);
            if (activeCount >= MAX_ACTIVE_SESSIONS) {
                throw new IllegalStateException(
                        "Too many active sessions for UID " + callingUid);
            }
            sessionId = allocateSessionIdLocked();
            File sessionDir = new File(VEnvironment.getPackageInstallerStageDir(), "vmd-" + sessionId);
            session = new PackageInstallerSession(mInternalCallback, mContext, mInstallHandler.getLooper(), installerPackageName, sessionId, userId, callingUid, params, sessionDir);
        }
        synchronized (mSessions) {
            mSessions.put(sessionId, session);
        }
        mCallbacks.notifySessionCreated(session.sessionId, session.userId);
        return sessionId;
    }

    @Override
    public void updateSessionAppIcon(int sessionId, Bitmap appIcon) {
        synchronized (mSessions) {
            final PackageInstallerSession session = mSessions.get(sessionId);
            if (session == null || !isCallingUidOwner(session)) {
                throw new SecurityException("Caller has no access to session " + sessionId);
            }

            session.params.appIcon = appIcon;
            session.params.appIconLastModified = -1;

            mInternalCallback.onSessionBadgingChanged(session);
        }
    }

    @Override
    public void updateSessionAppLabel(int sessionId, String appLabel) throws RemoteException {
        synchronized (mSessions) {
            final PackageInstallerSession session = mSessions.get(sessionId);
            if (session == null || !isCallingUidOwner(session)) {
                throw new SecurityException("Caller has no access to session " + sessionId);
            }
            session.params.appLabel = appLabel;
            mInternalCallback.onSessionBadgingChanged(session);
        }
    }

    @Override
    public void abandonSession(int sessionId) throws RemoteException {
        synchronized (mSessions) {
            final PackageInstallerSession session = mSessions.get(sessionId);
            if (session == null || !isCallingUidOwner(session)) {
                throw new SecurityException("Caller has no access to session " + sessionId);
            }
            session.abandon();
        }
    }

    @Override
    public IPackageInstallerSession openSession(int sessionId) throws RemoteException {
        try {
            return openSessionInternal(sessionId);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private IPackageInstallerSession openSessionInternal(int sessionId) throws IOException {
        synchronized (mSessions) {
            final PackageInstallerSession session = mSessions.get(sessionId);
            if (session == null || !isCallingUidOwner(session)) {
                throw new SecurityException("Caller has no access to session " + sessionId);
            }
            session.open();
            return session;
        }
    }

    @Override
    public SessionInfo getSessionInfo(int sessionId) throws RemoteException {
        synchronized (mSessions) {
            final PackageInstallerSession session = mSessions.get(sessionId);
            return session != null ? session.generateInfo() : null;
        }
    }

    @Override
    public VParceledListSlice getAllSessions(int userId) throws RemoteException {
        final List<SessionInfo> result = new ArrayList<>();
        synchronized (mSessions) {
            for (int i = 0; i < mSessions.size(); i++) {
                final PackageInstallerSession session = mSessions.valueAt(i);
                if (session.userId == userId) {
                    result.add(session.generateInfo());
                }
            }
        }
        return new VParceledListSlice<>(result);
    }

    @Override
    public VParceledListSlice getMySessions(String installerPackageName, int userId) throws RemoteException {
        final List<SessionInfo> result = new ArrayList<>();
        synchronized (mSessions) {
            for (int i = 0; i < mSessions.size(); i++) {
                final PackageInstallerSession session = mSessions.valueAt(i);
                if (ObjectsCompat.equals(session.installerPackageName, installerPackageName)
                        && session.userId == userId) {
                    result.add(session.generateInfo());
                }
            }
        }
        return new VParceledListSlice<>(result);
    }

    @Override
    public void registerCallback(IPackageInstallerCallback callback, int userId) throws RemoteException {
        mCallbacks.register(callback, userId);
    }

    @Override
    public void unregisterCallback(IPackageInstallerCallback callback) throws RemoteException {
        mCallbacks.unregister(callback);
    }

    @Override
    public void uninstall(String packageName, String callerPackageName, int flags, IntentSender statusReceiver, int userId) throws RemoteException {
        boolean success = VAppManagerService.get().uninstallPackage(packageName);
        if (statusReceiver != null) {
            final Intent fillIn = new Intent();
            fillIn.putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, packageName);
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS, success ? PackageInstaller.STATUS_SUCCESS : PackageInstaller.STATUS_FAILURE);
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS_MESSAGE, PackageHelper.deleteStatusToString(success));
            fillIn.putExtra("android.content.pm.extra.LEGACY_STATUS", success ? 1 : -1);
            try {
                statusReceiver.sendIntent(mContext, 0, fillIn, null, null);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setPermissionsResult(int sessionId, boolean accepted) throws RemoteException {
        synchronized (mSessions) {
            PackageInstallerSession session = mSessions.get(sessionId);
            if (session != null) {
                session.setPermissionsResult(accepted);
            }
        }
    }

    private boolean isCallingUidOwner(PackageInstallerSession session) {
        return true;
    }

    private int allocateSessionIdLocked() {
        int n = 0;
        int sessionId;
        do {
            sessionId = mRandom.nextInt(Integer.MAX_VALUE - 1) + 1;
            if (mSessions.get(sessionId) == null) {
                return sessionId;
            }
        } while (n++ < 32);

        throw new IllegalStateException("Failed to allocate session ID");
    }

    private static class Callbacks extends Handler {
        private static final int MSG_SESSION_CREATED = 1;
        private static final int MSG_SESSION_BADGING_CHANGED = 2;
        private static final int MSG_SESSION_ACTIVE_CHANGED = 3;
        private static final int MSG_SESSION_PROGRESS_CHANGED = 4;
        private static final int MSG_SESSION_FINISHED = 5;

        private final RemoteCallbackList<IPackageInstallerCallback>
                mCallbacks = new RemoteCallbackList<>();

        public Callbacks(Looper looper) {
            super(looper);
        }

        public void register(IPackageInstallerCallback callback, int userId) {
            mCallbacks.register(callback, new VUserHandle(userId));
        }

        public void unregister(IPackageInstallerCallback callback) {
            mCallbacks.unregister(callback);
        }

        @Override
        public void handleMessage(Message msg) {
            final int userId = msg.arg2;
            final int n = mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                final IPackageInstallerCallback callback = mCallbacks.getBroadcastItem(i);
                final VUserHandle user = (VUserHandle) mCallbacks.getBroadcastCookie(i);
                // TODO: dispatch notifications for slave profiles
                if (userId == user.getIdentifier()) {
                    try {
                        invokeCallback(callback, msg);
                    } catch (RemoteException ignored) {
                    }
                }
            }
            mCallbacks.finishBroadcast();
        }

        private void invokeCallback(IPackageInstallerCallback callback, Message msg)
                throws RemoteException {
            final int sessionId = msg.arg1;
            switch (msg.what) {
                case MSG_SESSION_CREATED:
                    callback.onSessionCreated(sessionId);
                    break;
                case MSG_SESSION_BADGING_CHANGED:
                    callback.onSessionBadgingChanged(sessionId);
                    break;
                case MSG_SESSION_ACTIVE_CHANGED:
                    callback.onSessionActiveChanged(sessionId, (boolean) msg.obj);
                    break;
                case MSG_SESSION_PROGRESS_CHANGED:
                    callback.onSessionProgressChanged(sessionId, (float) msg.obj);
                    break;
                case MSG_SESSION_FINISHED:
                    callback.onSessionFinished(sessionId, (boolean) msg.obj);
                    break;
            }
        }

        private void notifySessionCreated(int sessionId, int userId) {
            obtainMessage(MSG_SESSION_CREATED, sessionId, userId).sendToTarget();
        }

        private void notifySessionBadgingChanged(int sessionId, int userId) {
            obtainMessage(MSG_SESSION_BADGING_CHANGED, sessionId, userId).sendToTarget();
        }

        private void notifySessionActiveChanged(int sessionId, int userId, boolean active) {
            obtainMessage(MSG_SESSION_ACTIVE_CHANGED, sessionId, userId, active).sendToTarget();
        }

        private void notifySessionProgressChanged(int sessionId, int userId, float progress) {
            obtainMessage(MSG_SESSION_PROGRESS_CHANGED, sessionId, userId, progress).sendToTarget();
        }

        public void notifySessionFinished(int sessionId, int userId, boolean success) {
            obtainMessage(MSG_SESSION_FINISHED, sessionId, userId, success).sendToTarget();
        }
    }

    static class PackageInstallObserverAdapter extends PackageInstallObserver {
        private final Context mContext;
        private final IntentSender mTarget;
        private final int mSessionId;
        private final int mUserId;

        PackageInstallObserverAdapter(Context context, IntentSender target, int sessionId, int userId) {
            mContext = context;
            mTarget = target;
            mSessionId = sessionId;
            mUserId = userId;
        }

        @Override
        public void onUserActionRequired(Intent intent) {
            final Intent fillIn = new Intent();
            fillIn.putExtra(PackageInstaller.EXTRA_SESSION_ID, mSessionId);
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_PENDING_USER_ACTION);
            fillIn.putExtra(Intent.EXTRA_INTENT, intent);
            try {
                mTarget.sendIntent(mContext, 0, fillIn, null, null);
            } catch (IntentSender.SendIntentException ignored) {
            }
        }

        @Override
        public void onPackageInstalled(String basePackageName, int returnCode, String msg,
                                       Bundle extras) {
            final Intent fillIn = new Intent();
            fillIn.putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, basePackageName);
            fillIn.putExtra(PackageInstaller.EXTRA_SESSION_ID, mSessionId);
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS,
                    installStatusToPublicStatus(returnCode));
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS_MESSAGE,
                    installStatusToString(returnCode, msg));
            fillIn.putExtra("android.content.pm.extra.LEGACY_STATUS", returnCode);
            if (extras != null) {
                final String existing = extras.getString("android.content.pm.extra.FAILURE_EXISTING_PACKAGE");
                if (!TextUtils.isEmpty(existing)) {
                    fillIn.putExtra(PackageInstaller.EXTRA_OTHER_PACKAGE_NAME, existing);
                }
            }
            try {
                mTarget.sendIntent(mContext, 0, fillIn, null, null);
            } catch (IntentSender.SendIntentException ignored) {
            }
        }
    }

    class InternalCallback {
        public void onSessionBadgingChanged(PackageInstallerSession session) {
            mCallbacks.notifySessionBadgingChanged(session.sessionId, session.userId);
        }

        public void onSessionActiveChanged(PackageInstallerSession session, boolean active) {
            mCallbacks.notifySessionActiveChanged(session.sessionId, session.userId, active);
        }

        public void onSessionProgressChanged(PackageInstallerSession session, float progress) {
            mCallbacks.notifySessionProgressChanged(session.sessionId, session.userId, progress);
        }

        public void onSessionFinished(final PackageInstallerSession session, boolean success) {
            mCallbacks.notifySessionFinished(session.sessionId, session.userId, success);

            mInstallHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSessions) {
                        mSessions.remove(session.sessionId);
                    }
                }
            });
        }

        public void onSessionPrepared(PackageInstallerSession session) {
            // We prepared the destination to write into; we want to persist
            // this, but it's not critical enough to block for.
        }

        public void onSessionSealedBlocking(PackageInstallerSession session) {
            // It's very important that we block until we've recorded the
            // session as being sealed, since we never want to allow mutation
            // after sealing.
        }
    }

}
