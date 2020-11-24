package com.lody.virtual.server.pm.installer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageInstallerSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;

import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.server.pm.VAppManagerService;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static android.system.OsConstants.O_CREAT;
import static android.system.OsConstants.O_RDONLY;
import static android.system.OsConstants.O_WRONLY;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PackageInstallerSession extends IPackageInstallerSession.Stub {

    public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;
    public static final int INSTALL_FAILED_ABORTED = -115;
    public static final int INSTALL_SUCCEEDED = 1;
    public static final int INSTALL_FAILED_INVALID_APK = -2;

    private static final String TAG = "PackageInstaller";
    private static final String REMOVE_SPLIT_MARKER_EXTENSION = ".removed";

    private static final int MSG_COMMIT = 0;


    private final VPackageInstallerService.InternalCallback mCallback;
    private final Context mContext;
    private final Handler mHandler;

    final int sessionId;
    final int userId;
    final int installerUid;

    final SessionParams params;
    final String installerPackageName;
    private boolean mPermissionsAccepted;

    /**
     * Staging location where client data is written.
     */
    final File stageDir;

    private final AtomicInteger mActiveCount = new AtomicInteger();

    private final Object mLock = new Object();

    private float mClientProgress = 0;
    private float mInternalProgress = 0;
    private float mProgress = 0;
    private float mReportedProgress = -1;
    private boolean mPrepared = false;
    private boolean mSealed = false;
    private boolean mDestroyed = false;
    private int mFinalStatus;
    private String mFinalMessage;

    private IPackageInstallObserver2 mRemoteObserver;

    private ArrayList<FileBridge> mBridges = new ArrayList<>();

    private File mResolvedStageDir;

    /**
     * Fields derived from commit parsing
     */
    private String mPackageName;

    private File mResolvedBaseFile;
    private final List<File> mResolvedStagedFiles = new ArrayList<>();


    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            synchronized (mLock) {
                if (msg.obj != null) {
                    mRemoteObserver = (IPackageInstallObserver2) msg.obj;
                }
                try {
                    commitLocked();
                } catch (PackageManagerException e) {
                    final String completeMsg = getCompleteMessage(e);
                    VLog.e(TAG, "Commit of session " + sessionId + " failed: " + completeMsg);
                    destroyInternal();
                    dispatchSessionFinished(e.error, completeMsg, null);
                }

                return true;
            }
        }
    };

    public PackageInstallerSession(VPackageInstallerService.InternalCallback callback, Context context, Looper looper, String installerPackageName, int sessionId, int userId, int installerUid, SessionParams params, File stageDir) {
        this.mCallback = callback;
        this.mContext = context;
        this.mHandler = new Handler(looper, mHandlerCallback);
        this.installerPackageName = installerPackageName;
        this.sessionId = sessionId;
        this.userId = userId;
        this.installerUid = installerUid;
        this.mPackageName = params.appPackageName;
        this.params = params;
        this.stageDir = stageDir;
    }

    public SessionInfo generateInfo() {
        final SessionInfo info = new SessionInfo();
        synchronized (mLock) {
            info.sessionId = sessionId;
            info.installerPackageName = installerPackageName;
            info.resolvedBaseCodePath = (mResolvedBaseFile != null) ?
                    mResolvedBaseFile.getAbsolutePath() : null;
            info.progress = mProgress;
            info.sealed = mSealed;
            info.active = mActiveCount.get() > 0;

            info.mode = params.mode;
            info.sizeBytes = params.sizeBytes;
            info.appPackageName = params.appPackageName;
            info.appIcon = params.appIcon;
            info.appLabel = params.appLabel;
        }
        return info;
    }

    private void commitLocked() throws PackageManagerException {
        if (mDestroyed) {
            throw new PackageManagerException(INSTALL_FAILED_INTERNAL_ERROR, "Session destroyed");
        }
        if (!mSealed) {
            throw new PackageManagerException(INSTALL_FAILED_INTERNAL_ERROR, "Session not sealed");
        }
        try {
            resolveStageDir();
        } catch (IOException e) {
            e.printStackTrace();
        }
        validateInstallLocked();
        mInternalProgress = 0.5f;
        computeProgressLocked(true);
        // We've reached point of no return; call into PMS to install the stage.
        // Regardless of success or failure we always destroy session.
        final IPackageInstallObserver2 localObserver = new IPackageInstallObserver2.Stub() {
            @Override
            public void onUserActionRequired(Intent intent) {
                throw new IllegalStateException();
            }

            @Override
            public void onPackageInstalled(String basePackageName, int returnCode, String msg,
                                           Bundle extras) {
                destroyInternal();
                dispatchSessionFinished(returnCode, msg, extras);
            }
        };

        InstallResult installResult = VAppManagerService.get().installPackage(stageDir.getPath(), 0);
        destroyInternal();
        dispatchSessionFinished(installResult.isSuccess ? INSTALL_SUCCEEDED : INSTALL_FAILED_INTERNAL_ERROR, installResult.toString(), null);
    }

    private void validateInstallLocked() throws PackageManagerException {
        mResolvedBaseFile = null;
        mResolvedStagedFiles.clear();
        File[] addedFiles = this.mResolvedStageDir.listFiles();
        if (addedFiles == null || addedFiles.length == 0) {
            throw new PackageManagerException(INSTALL_FAILED_INVALID_APK, "No packages staged");
        }
        for (File addedFile : addedFiles) {
            if (!addedFile.isDirectory()) {
                final String targetName = "base.apk";
                final File targetFile = new File(mResolvedStageDir, targetName);
                if (!addedFile.equals(targetFile)) {
                    mResolvedStagedFiles.add(addedFile);
                } else {
                    mResolvedBaseFile = targetFile;
                }
            }
        }
        if (mResolvedBaseFile == null && mResolvedStagedFiles.isEmpty()) {
            throw new PackageManagerException(INSTALL_FAILED_INVALID_APK,
                    "Full install must include a base package");
        }
    }

    @Override
    public void setClientProgress(float progress) throws RemoteException {
        synchronized (mLock) {
            // Always publish first staging movement
            final boolean forcePublish = (mClientProgress == 0);
            mClientProgress = progress;
            computeProgressLocked(forcePublish);
        }
    }


    private static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    private void computeProgressLocked(boolean forcePublish) {
        mProgress = constrain(mClientProgress * 0.8f, 0f, 0.8f)
                + constrain(mInternalProgress * 0.2f, 0f, 0.2f);

        // Only publish when meaningful change
        if (forcePublish || Math.abs(mProgress - mReportedProgress) >= 0.01) {
            mReportedProgress = mProgress;
            mCallback.onSessionProgressChanged(this, mProgress);
        }
    }

    @Override
    public void addClientProgress(float progress) throws RemoteException {
        synchronized (mLock) {
            setClientProgress(mClientProgress + progress);
        }
    }

    @Override
    public String[] getNames() throws RemoteException {
        assertPreparedAndNotSealed("getNames");
        try {
            return resolveStageDir().list();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Resolve the actual location where staged data should be written. This
     * might point at an ASEC mount point, which is why we delay path resolution
     * until someone actively works with the session.
     */
    private File resolveStageDir() throws IOException {
        synchronized (mLock) {
            if (mResolvedStageDir == null && stageDir != null) {
                mResolvedStageDir = stageDir;
                if (!stageDir.exists()) {
                    stageDir.mkdirs();
                }
            }
            return mResolvedStageDir;
        }
    }

    @Override
    public ParcelFileDescriptor openWrite(String name, long offsetBytes, long lengthBytes) throws RemoteException {
        try {
            return openWriteInternal(name, offsetBytes, lengthBytes);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void assertPreparedAndNotSealed(String cookie) {
        synchronized (mLock) {
            if (!mPrepared) {
                throw new IllegalStateException(cookie + " before prepared");
            }
            if (mSealed) {
                throw new SecurityException(cookie + " not allowed after commit");
            }
        }
    }


    private ParcelFileDescriptor openWriteInternal(String name, long offsetBytes, long lengthBytes)
            throws IOException {
        // Quick sanity check of state, and allocate a pipe for ourselves. We
        // then do heavy disk allocation outside the lock, but this open pipe
        // will block any attempted install transitions.
        final FileBridge bridge;
        synchronized (mLock) {
            assertPreparedAndNotSealed("openWrite");

            bridge = new FileBridge();
            mBridges.add(bridge);
        }
        try {
            final File target = new File(resolveStageDir(), name);
            // TODO: this should delegate to DCS so the system process avoids
            // holding open FDs into containers.
            final FileDescriptor targetFd = Os.open(target.getAbsolutePath(),
                    O_CREAT | O_WRONLY, 0644);
            // If caller specified a total length, allocate it for them. Free up
            // cache space to grow, if needed.
            if (lengthBytes > 0) {
                Os.posix_fallocate(targetFd, 0, lengthBytes);
            }
            if (offsetBytes > 0) {
                Os.lseek(targetFd, offsetBytes, OsConstants.SEEK_SET);
            }
            bridge.setTargetFile(targetFd);
            bridge.start();
            return ParcelFileDescriptor.dup(bridge.getClientSocket());

        } catch (ErrnoException e) {
            throw new IOException(e);
        }
    }

    @Override
    public ParcelFileDescriptor openRead(String name) throws RemoteException {
        try {
            return openReadInternal(name);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private ParcelFileDescriptor openReadInternal(String name) throws IOException {
        assertPreparedAndNotSealed("openRead");

        try {
            if (!FileUtils.isValidExtFilename(name)) {
                throw new IllegalArgumentException("Invalid name: " + name);
            }
            final File target = new File(resolveStageDir(), name);

            final FileDescriptor targetFd = Os.open(target.getAbsolutePath(), O_RDONLY, 0);
            return ParcelFileDescriptor.dup(targetFd);

        } catch (ErrnoException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void removeSplit(String splitName) throws RemoteException {
        if (TextUtils.isEmpty(params.appPackageName)) {
            throw new IllegalStateException("Must specify package name to remove a split");
        }
        try {
            createRemoveSplitMarker(splitName);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void createRemoveSplitMarker(String splitName) throws IOException {
        try {
            final String markerName = splitName + REMOVE_SPLIT_MARKER_EXTENSION;
            if (!FileUtils.isValidExtFilename(markerName)) {
                throw new IllegalArgumentException("Invalid marker: " + markerName);
            }
            final File target = new File(resolveStageDir(), markerName);
            target.createNewFile();
            Os.chmod(target.getAbsolutePath(), 0 /*mode*/);
        } catch (ErrnoException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws RemoteException {
        if (mActiveCount.decrementAndGet() == 0) {
            mCallback.onSessionActiveChanged(this, false);
        }
    }

    // https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/core/java/android/content/pm/IPackageInstallerSession.aidl;l=39
    public void commit(IntentSender statusReceiver, boolean forTransferred) throws RemoteException {
        commit(statusReceiver);
    }

    @Override
    public void commit(IntentSender statusReceiver) throws RemoteException {
        final boolean wasSealed;
        synchronized (mLock) {
            wasSealed = mSealed;
            if (!mSealed) {
                // Verify that all writers are hands-off
                for (FileBridge bridge : mBridges) {
                    if (!bridge.isClosed()) {
                        throw new SecurityException("Files still open");
                    }
                }
                mSealed = true;
            }

            // Client staging is fully done at this point
            mClientProgress = 1f;
            computeProgressLocked(true);
        }

        if (!wasSealed) {
            // Persist the fact that we've sealed ourselves to prevent
            // mutations of any hard links we create. We do this without holding
            // the session lock, since otherwise it's a lock inversion.
            mCallback.onSessionSealedBlocking(this);
        }

        // This ongoing commit should keep session active, even though client
        // will probably close their end.
        mActiveCount.incrementAndGet();

        final VPackageInstallerService.PackageInstallObserverAdapter adapter
                = new VPackageInstallerService.PackageInstallObserverAdapter(mContext,
                statusReceiver, sessionId, userId);
        mHandler.obtainMessage(MSG_COMMIT, adapter.getBinder()).sendToTarget();
    }

    @Override
    public void abandon() throws RemoteException {
        destroyInternal();
        dispatchSessionFinished(INSTALL_FAILED_ABORTED, "Session was abandoned", null);
    }

    private void destroyInternal() {
        synchronized (mLock) {
            mSealed = true;
            mDestroyed = true;

            // Force shut down all bridges
            for (FileBridge bridge : mBridges) {
                bridge.forceClose();
            }
        }
        if (stageDir != null) {
            FileUtils.deleteDir(stageDir.getAbsolutePath());
        }
    }

    private void dispatchSessionFinished(int returnCode, String msg, Bundle extras) {
        mFinalStatus = returnCode;
        mFinalMessage = msg;

        if (mRemoteObserver != null) {
            try {
                mRemoteObserver.onPackageInstalled(mPackageName, returnCode, msg, extras);
            } catch (RemoteException ignored) {
            }
        }

        final boolean success = (returnCode == INSTALL_SUCCEEDED);
        mCallback.onSessionFinished(this, success);
    }

    void setPermissionsResult(boolean accepted) {
        if (!mSealed) {
            throw new SecurityException("Must be sealed to accept permissions");
        }

        if (accepted) {
            // Mark and kick off another install pass
            synchronized (mLock) {
                mPermissionsAccepted = true;
            }
            mHandler.obtainMessage(MSG_COMMIT).sendToTarget();
        } else {
            destroyInternal();
            dispatchSessionFinished(INSTALL_FAILED_ABORTED, "User rejected permissions", null);
        }
    }

    public void open() throws IOException {
        if (mActiveCount.getAndIncrement() == 0) {
            mCallback.onSessionActiveChanged(this, true);
        }

        synchronized (mLock) {
            if (!mPrepared) {
                if (stageDir == null) {
                    throw new IllegalArgumentException(
                            "Exactly one of stageDir or stageCid stage must be set");
                }
                mPrepared = true;
                mCallback.onSessionPrepared(this);
            }
        }
    }


    public static String getCompleteMessage(Throwable t) {
        final StringBuilder builder = new StringBuilder();
        builder.append(t.getMessage());
        while ((t = t.getCause()) != null) {
            builder.append(": ").append(t.getMessage());
        }
        return builder.toString();
    }

    private class PackageManagerException extends Exception {
        public final int error;

        PackageManagerException(int error, String detailMessage) {
            super(detailMessage);
            this.error = error;
        }
    }

}
