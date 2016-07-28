package com.lody.virtual.helper.compat;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @author Lody
 *
 */
public class BundleCompat {
	public static IBinder getBinder(Bundle bundle, String key) {
		if (Build.VERSION.SDK_INT >= 18) {
			return bundle.getBinder(key);
		} else {
			return bundle.getIBinder(key);
		}
	}

	public static void putBinder(Bundle bundle, String key, IBinder value) {
		if (Build.VERSION.SDK_INT >= 18) {
			bundle.putBinder(key, value);
		} else {
			bundle.putIBinder(key, value);
		}
	}


}
