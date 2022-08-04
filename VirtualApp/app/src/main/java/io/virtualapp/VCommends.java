package io.virtualapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Lody
 */
public class VCommends {

	public static final String TAG_NEW_VERSION = "First launch new Version";
	public static final String TAG_SHOW_ADD_APP_GUIDE = "Should show add app guide";

	public static final int REQUEST_SELECT_APP = 5;

	public static final String EXTRA_APP_INFO_LIST = "va.extra.APP_INFO_LIST";

	public static final String TAG_ASK_INSTALL_GMS = "va.extra.ASK_INSTALL_GMS";

	static String getSig(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
			Signature[] signs = packageInfo.signatures;
			Signature sign = signs[0];
			String signStr = md(sign.toByteArray());
			return signStr;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	static String md(byte[] byteStr) {
		MessageDigest messageDigest = null;
		StringBuffer md5StrBuff = new StringBuffer();
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(byteStr);
			byte[] byteArray = messageDigest.digest();
			for (int i = 0; i < byteArray.length; i++) {
				if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
					md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
				} else {
					md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md5StrBuff.toString();
	}

	/**
	 * 君子坦荡荡，小人常戚戚
	 * @param context the context
	 */
	public static void c(Context context) {
		String sig = getSig(context);
		if (!"99A244F52F40581B48E4BA61E3435B6C".equalsIgnoreCase(sig)) {
			System.exit(10);
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
}
