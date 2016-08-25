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
			return mirror.android.os.Bundle.getIBinder.call(bundle, key);
		}
	}

	public static void putBinder(Bundle bundle, String key, IBinder value) {
		if (Build.VERSION.SDK_INT >= 18) {
			bundle.putBinder(key, value);
		} else {
			mirror.android.os.Bundle.putIBinder.call(bundle, key, value);
		}
	}

}
