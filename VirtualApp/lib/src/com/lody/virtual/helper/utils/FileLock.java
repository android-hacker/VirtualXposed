package com.lody.virtual.helper.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件锁
 */
public class FileLock {
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

		FileLockCount(java.nio.channels.FileLock fileLock, int mRefCount, RandomAccessFile fOs, FileChannel fChannel) {
			this.mFileLock = fileLock;
			this.mRefCount = mRefCount;
			this.fOs = fOs;
			this.fChannel = fChannel;
		}
	}
}