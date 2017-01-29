package com.lody.virtual.helper.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 */
public class FileUtils {
    public static byte[] toByteArray(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        return swapStream.toByteArray();
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                boolean success = deleteDir(new File(dir, file));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static boolean deleteDir(String dir) {
        return deleteDir(new File(dir));
    }

    public static void writeToFile(InputStream dataIns, File target) throws IOException {
        final int BUFFER = 1024;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = dataIns.read(data, 0, BUFFER)) != -1) {
            bos.write(data, 0, count);
        }
        bos.close();
    }

    public static void writeToFile(byte[] data, File target) throws IOException {
        FileOutputStream fo = null;
        ReadableByteChannel src = null;
        FileChannel out = null;
        try {
            src = Channels.newChannel(new ByteArrayInputStream(data));
            fo = new FileOutputStream(target);
            out = fo.getChannel();
            out.transferFrom(src, 0, data.length);
        } finally {
            if (fo != null) {
                fo.close();
            }
            if (src != null) {
                src.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static void copyFile(File source, File target) {

        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(target);
            FileChannel iChannel = inputStream.getChannel();
            FileChannel oChannel = outputStream.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                buffer.clear();
                int r = iChannel.read(buffer);
                if (r == -1)
                    break;
                buffer.limit(buffer.position());
                buffer.position(0);
                oChannel.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }


    public static int peekInt(byte[] bytes, int value, ByteOrder endian) {
        int v2;
        int v0;
        if (endian == ByteOrder.BIG_ENDIAN) {
            v0 = value + 1;
            v2 = v0 + 1;
            v0 = (bytes[v0] & 255) << 16 | (bytes[value] & 255) << 24 | (bytes[v2] & 255) << 8 | bytes[v2 + 1] & 255;
        } else {
            v0 = value + 1;
            v2 = v0 + 1;
            v0 = (bytes[v0] & 255) << 8 | bytes[value] & 255 | (bytes[v2] & 255) << 16 | (bytes[v2 + 1] & 255) << 24;
        }

        return v0;
    }

    private static boolean isValidExtFilenameChar(char c) {
        switch (c) {
            case '\0':
            case '/':
                return false;
            default:
                return true;
        }
    }

    /**
     * Check if given filename is valid for an ext4 filesystem.
     */
    public static boolean isValidExtFilename(String name) {
        return (name != null) && name.equals(buildValidExtFilename(name));
    }

    /**
     * Mutate the given filename to make it valid for an ext4 filesystem,
     * replacing any invalid characters with "_".
     */
    public static String buildValidExtFilename(String name) {
        if (TextUtils.isEmpty(name) || ".".equals(name) || "..".equals(name)) {
            return "(invalid)";
        }
        final StringBuilder res = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (isValidExtFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        return res.toString();
    }
    /**
     * Lock the specified fle
     */
    public static class FileLock {
        private static FileLock singleton;
        private Map<String, FileLockCount> mRefCountMap = new ConcurrentHashMap<String, FileLockCount>();

        public static FileLock getInstance() {
            if (singleton == null) {
                singleton = new FileLock();
            }
            return singleton;
        }

        private int RefCntInc(String filePath, java.nio.channels.FileLock fileLock, RandomAccessFile randomAccessFile,
                              FileChannel fileChannel) {
            int refCount;
            if (this.mRefCountMap.containsKey(filePath)) {
                FileLockCount fileLockCount = this.mRefCountMap.get(filePath);
                int i = fileLockCount.mRefCount;
                fileLockCount.mRefCount = i + 1;
                refCount = i;
            } else {
                refCount = 1;
                this.mRefCountMap.put(filePath, new FileLockCount(fileLock, refCount, randomAccessFile, fileChannel));

            }
            return refCount;
        }

        private int RefCntDec(String filePath) {
            int refCount = 0;
            if (this.mRefCountMap.containsKey(filePath)) {
                FileLockCount fileLockCount = this.mRefCountMap.get(filePath);
                int i = fileLockCount.mRefCount - 1;
                fileLockCount.mRefCount = i;
                refCount = i;
                if (refCount <= 0) {
                    this.mRefCountMap.remove(filePath);
                }
            }
            return refCount;
        }

        public boolean LockExclusive(File targetFile) {

            if (targetFile == null) {
                return false;
            }
            try {
                File lockFile = new File(targetFile.getParentFile().getAbsolutePath().concat("/lock"));
                if (!lockFile.exists()) {
                    lockFile.createNewFile();
                }
                RandomAccessFile randomAccessFile = new RandomAccessFile(lockFile.getAbsolutePath(), "rw");
                FileChannel channel = randomAccessFile.getChannel();
                java.nio.channels.FileLock lock = channel.lock();
                if (!lock.isValid()) {
                    return false;
                }
                RefCntInc(lockFile.getAbsolutePath(), lock, randomAccessFile, channel);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * unlock odex file
         **/
        public void unLock(File targetFile) {

            File lockFile = new File(targetFile.getParentFile().getAbsolutePath().concat("/lock"));
            if (!lockFile.exists()) {
                return;
            }
            if (this.mRefCountMap.containsKey(lockFile.getAbsolutePath())) {
                FileLockCount fileLockCount = this.mRefCountMap.get(lockFile.getAbsolutePath());
                if (fileLockCount != null) {
                    java.nio.channels.FileLock fileLock = fileLockCount.mFileLock;
                    RandomAccessFile randomAccessFile = fileLockCount.fOs;
                    FileChannel fileChannel = fileLockCount.fChannel;
                    try {
                        if (RefCntDec(lockFile.getAbsolutePath()) <= 0) {
                            if (fileLock != null && fileLock.isValid()) {
                                fileLock.release();
                            }
                            if (randomAccessFile != null) {
                                randomAccessFile.close();
                            }
                            if (fileChannel != null) {
                                fileChannel.close();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private class FileLockCount {
            FileChannel fChannel;
            RandomAccessFile fOs;
            java.nio.channels.FileLock mFileLock;
            int mRefCount;

            FileLockCount(java.nio.channels.FileLock fileLock, int mRefCount, RandomAccessFile fOs,
                          FileChannel fChannel) {
                this.mFileLock = fileLock;
                this.mRefCount = mRefCount;
                this.fOs = fOs;
                this.fChannel = fChannel;
            }
        }
    }
}
