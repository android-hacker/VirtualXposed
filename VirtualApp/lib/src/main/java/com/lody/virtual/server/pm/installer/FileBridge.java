package com.lody.virtual.server.pm.installer;

import android.annotation.TargetApi;
import android.os.Build;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import com.lody.virtual.helper.utils.ArrayUtils;
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
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FileBridge extends Thread {
    private static final String TAG = "FileBridge";

    // TODO: consider extending to support bidirectional IO

    private static final int MSG_LENGTH = 8;

    /**
     * CMD_WRITE [len] [data]
     */
    private static final int CMD_WRITE = 1;
    /**
     * CMD_FSYNC
     */
    private static final int CMD_FSYNC = 2;
    /**
     * CMD_CLOSE
     */
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
        closeQuietly(mTarget);
        closeQuietly(mServer);
        closeQuietly(mClient);
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
            while (read(mServer, temp, 0, MSG_LENGTH) == MSG_LENGTH) {
                final int cmd = FileUtils.peekInt(temp, 0, ByteOrder.BIG_ENDIAN);
                if (cmd == CMD_WRITE) {
                    // Shuttle data into local file
                    int len = FileUtils.peekInt(temp, 4, ByteOrder.BIG_ENDIAN);
                    while (len > 0) {
                        int n = read(mServer, temp, 0, Math.min(temp.length, len));
                        if (n == -1) {
                            throw new IOException(
                                    "Unexpected EOF; still expected " + len + " bytes");
                        }
                        write(mTarget, temp, 0, n);
                        len -= n;
                    }

                } else if (cmd == CMD_FSYNC) {
                    // Sync and echo back to confirm
                    Os.fsync(mTarget);
                    write(mServer, temp, 0, MSG_LENGTH);

                } else if (cmd == CMD_CLOSE) {
                    // Close and echo back to confirm
                    Os.fsync(mTarget);
                    Os.close(mTarget);
                    mClosed = true;
                    write(mServer, temp, 0, MSG_LENGTH);
                    break;
                }
            }

        } catch (ErrnoException | IOException e) {
            Log.wtf(TAG, "Failed during bridge", e);
        } finally {
            forceClose();
        }
    }


    public static void closeQuietly(FileDescriptor fd) {

        if (fd != null && fd.valid()) {
            try {
                Os.close(fd);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * java.io thinks that a read at EOF is an error and should return -1, contrary to traditional
     * Unix practice where you'd read until you got 0 bytes (and any future read would return -1).
     */
    public static int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws IOException {
        ArrayUtils.checkOffsetAndCount(bytes.length, byteOffset, byteCount);
        if (byteCount == 0) {
            return 0;
        }
        try {
            int readCount = Os.read(fd, bytes, byteOffset, byteCount);
            if (readCount == 0) {
                return -1;
            }
            return readCount;
        } catch (ErrnoException errnoException) {
            if (errnoException.errno == OsConstants.EAGAIN) {
                // We return 0 rather than throw if we try to read from an empty non-blocking pipe.
                return 0;
            }
            throw new IOException(errnoException);
        }
    }

    /**
     * java.io always writes every byte it's asked to, or fails with an error. (That is, unlike
     * Unix it never just writes as many bytes as happens to be convenient.)
     */
    public static void write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws IOException {
        ArrayUtils.checkOffsetAndCount(bytes.length, byteOffset, byteCount);
        if (byteCount == 0) {
            return;
        }
        try {
            while (byteCount > 0) {
                int bytesWritten = Os.write(fd, bytes, byteOffset, byteCount);
                byteCount -= bytesWritten;
                byteOffset += bytesWritten;
            }
        } catch (ErrnoException errnoException) {
            throw new IOException(errnoException);
        }
    }
}
