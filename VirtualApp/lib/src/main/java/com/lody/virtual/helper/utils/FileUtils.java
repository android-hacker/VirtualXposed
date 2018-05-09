package com.lody.virtual.helper.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.system.Os;
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
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 */
public class FileUtils {
    /**
     * @param path
     * @param mode {@link FileMode}
     * @throws Exception
     */
    public static void chmod(String path, int mode) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Os.chmod(path, mode);
                return;
            } catch (Exception e) {
                // ignore
            }
        }

        File file = new File(path);
        String cmd = "chmod ";
        if (file.isDirectory()) {
            cmd += " -R ";
        }
        String cmode = String.format("%o", mode);
        Runtime.getRuntime().exec(cmd + cmode + " " + path).waitFor();
    }

    public static void createSymlink(String oldPath, String newPath) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Os.symlink(oldPath, newPath);
        } else {
            Runtime.getRuntime().exec("ln -s " + oldPath + " " + newPath).waitFor();
        }
    }

    public static boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    public static void writeParcelToFile(Parcel p, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(p.marshall());
        fos.close();
    }

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
        if (dir == null) {
            return false;
        }
        boolean success = true;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                boolean ret = deleteDir(new File(dir, file));
                if (!ret) {
                    success = false;
                }
            }
            if (success) {
                // if all subdirectory are deleted, delete the dir itself.
                return dir.delete();
            }
        }
        return dir.delete();
    }

    public static boolean deleteDir(File dir, Set<File> ignores) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                boolean success = deleteDir(new File(dir, file), ignores);
                if (!success) {
                    return false;
                }
            }
        }
        return ignores != null && ignores.contains(dir) || dir.delete();
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

    public static String getFileFromUri(Context context, Uri packageUri) {

        if (packageUri == null) {
            return null;
        }

        final String SCHEME_FILE = "file";
        final String SCHEME_CONTENT = "content";
        String sourcePath = null;

        if (SCHEME_FILE.equals(packageUri.getScheme())) {
            sourcePath = packageUri.getPath();
        } else if (SCHEME_CONTENT.equals(packageUri.getScheme())){
            InputStream inputStream = null;
            OutputStream outputStream = null;
            File sharedFileCopy = new File(context.getCacheDir(), packageUri.getLastPathSegment());
            try {
                inputStream = context.getContentResolver().openInputStream(packageUri);
                outputStream = new FileOutputStream(sharedFileCopy);
                byte[] buffer = new byte[1024];
                int count;
                while ((count = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, count);
                }
                outputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                FileUtils.closeQuietly(inputStream);
                FileUtils.closeQuietly(outputStream);
            }
            sourcePath = sharedFileCopy.getPath();
        }
        return sourcePath;
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

    public static void copyFile(File source, File target) throws IOException {

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
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
    }

    public static void copyFile(String source, String target) throws IOException {
        File from = new File(source);
        if (!from.exists()) {
            return;
        }
        if (from.isFile()) {
            copyFile(from, new File(target));
        } else {
            copyDir(source, target);
        }
    }

    public static void copyDir(String sourcePath, String targetPath) throws IOException {
        File from = new File(sourcePath);
        if (!from.exists()) {
            return;
        }

        File to = new File(targetPath);
        if (!to.exists()) {
            boolean mkdirs = to.mkdirs();
            if (!mkdirs) {
                return;
            }
        }

        String[] child = from.list();
        for (String file : child) {
            File childSource = new File(sourcePath, file);
            if (childSource.isDirectory()) {
                copyDir(sourcePath + File.separator + file, targetPath + File.separator + file);
            } else {
                copyFile(childSource, new File(targetPath, file));
            }
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

    public interface FileMode {
        int MODE_ISUID = 04000;
        int MODE_ISGID = 02000;
        int MODE_ISVTX = 01000;
        int MODE_IRUSR = 00400;
        int MODE_IWUSR = 00200;
        int MODE_IXUSR = 00100;
        int MODE_IRGRP = 00040;
        int MODE_IWGRP = 00020;
        int MODE_IXGRP = 00010;
        int MODE_IROTH = 00004;
        int MODE_IWOTH = 00002;
        int MODE_IXOTH = 00001;

        int MODE_755 = MODE_IRUSR | MODE_IWUSR | MODE_IXUSR
                | MODE_IRGRP | MODE_IXGRP
                | MODE_IROTH | MODE_IXOTH;
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
