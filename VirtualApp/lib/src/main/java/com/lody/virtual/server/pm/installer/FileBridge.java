package com.lody.virtual.server.pm.installer;

import android.annotation.TargetApi;
import android.os.Build;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import com.lody.virtual.helper.utils.FileUtils;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteOrder;

import static android.system.OsConstants.AF_UNIX;
import static android.system.OsConstants.SOCK_STREAM;

/**
 * Simple bridge that allows file access across process boundaries without
 * returning the underlying {@link FileDescriptor}. This is useful when the
 * server side needs to strongly assert that a client side is completely
 * hands-off.
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FileBridge extends Thread {
    private static final String TAG = "FileBridge";

    // TODO: consider extending to support bidirectional IO

    private static final int MSG_LENGTH = 8;

    /** CMD_WRITE [len] [data] */
    private static final int CMD_WRITE = 1;
    /** CMD_FSYNC */
    private static final int CMD_FSYNC = 2;
    /** CMD_CLOSE */
    private static final int CMD_CLOSE = 3;

    private FileDescriptor mTarget;

    private final FileDescriptor mServer = new FileDescriptor();
    private final FileDescriptor mClient = new FileDescriptor();

    private volatile boolean mClosed;


    public FileBridge() {
        try {
            Os.socketpair(AF_UNIX, SOCK_STREAM, 0, mServer, mClient);
        } catch (ErrnoException e) {
            throw new RuntimeException("Failed to create bridge");
        }
    }

    public boolean isClosed() {
        return mClosed;
    }

    public void forceClose() {
        FileUtils.closeQuietly(mTarget);
        FileUtils.closeQuietly(mServer);
        FileUtils.closeQuietly(mClient);
        mClosed = true;
    }

    public void setTargetFile(FileDescriptor target) {
        mTarget = target;
    }

    public FileDescriptor getClientSocket() {
        return mClient;
    }

    @Override
    public void run() {
        final byte[] temp = new byte[8192];
        try {
            while (FileUtils.read(mServer, temp, 0, MSG_LENGTH) == MSG_LENGTH) {
                final int cmd = FileUtils.peekInt(temp, 0, ByteOrder.BIG_ENDIAN);
                if (cmd == CMD_WRITE) {
                    // Shuttle data into local file
                    int len = FileUtils.peekInt(temp, 4, ByteOrder.BIG_ENDIAN);
                    while (len > 0) {
                        int n = FileUtils.read(mServer, temp, 0, Math.min(temp.length, len));
                        if (n == -1) {
                            throw new IOException(
                                    "Unexpected EOF; still expected " + len + " bytes");
                        }
                        FileUtils.write(mTarget, temp, 0, n);
                        len -= n;
                    }

                } else if (cmd == CMD_FSYNC) {
                    // Sync and echo back to confirm
                    Os.fsync(mTarget);
                    FileUtils.write(mServer, temp, 0, MSG_LENGTH);

                } else if (cmd == CMD_CLOSE) {
                    // Close and echo back to confirm
                    Os.fsync(mTarget);
                    Os.close(mTarget);
                    mClosed = true;
                    FileUtils.write(mServer, temp, 0, MSG_LENGTH);
                    break;
                }
            }

        } catch (ErrnoException | IOException e) {
            Log.wtf(TAG, "Failed during bridge", e);
        } finally {
            forceClose();
        }
    }
}
