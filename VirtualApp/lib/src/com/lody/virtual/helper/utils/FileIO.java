package com.lody.virtual.helper.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Lody
 *
 */
public class FileIO {
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

		FileInputStream fi = null;
		FileOutputStream fo = null;

		FileChannel in = null;

		FileChannel out = null;

		try {
			fi = new FileInputStream(source);

			fo = new FileOutputStream(target);

			in = fi.getChannel();

			out = fo.getChannel();

			in.transferTo(0, in.size(), out);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fi != null) {
					fi.close();
				}

				if (in != null) {
					in.close();
				}

				if (fo != null) {
					fo.close();
				}

				if (out != null) {
					out.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
