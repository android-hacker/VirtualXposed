package com.lody.virtual.helper.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;

/**
 * @author Lody
 *
 *
 */
public class MD5Utils {

	/**
	 * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
	 */
	protected static char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f'};
	protected static MessageDigest MESSAGE_DIGEST_5 = null;

	static {
		try {
			MESSAGE_DIGEST_5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	public static String getFileMD5String(File file) throws IOException {
		InputStream fis;
		fis = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		int numRead;
		while ((numRead = fis.read(buffer)) > 0) {
			MESSAGE_DIGEST_5.update(buffer, 0, numRead);
		}
		fis.close();
		return bufferToHex(MESSAGE_DIGEST_5.digest());
	}

	public static String getFileMD5String(InputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int numRead;
		while ((numRead = in.read(buffer)) > 0) {
			MESSAGE_DIGEST_5.update(buffer, 0, numRead);
		}
		in.close();
		return bufferToHex(MESSAGE_DIGEST_5.digest());
	}
	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}
	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}
	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = HEX_DIGITS[(bt & 0xf0) >> 4];
		char c1 = HEX_DIGITS[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	public static boolean compareFiles(File one, File two) throws IOException {

		if (one.getAbsolutePath().equals(two.getAbsolutePath())) {
			// 是同一个文件
			return true;
		}
		String md5_1 = getFileMD5String(one);
		String md5_2 = getFileMD5String(two);
		return TextUtils.equals(md5_1, md5_2);
	}

}
