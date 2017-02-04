package com.lody.virtual.client.fixer;

import android.os.StrictMode;

import com.lody.virtual.client.core.VirtualCore;

import java.lang.reflect.Field;

/**
 * @author CodeHz
 */
public final class StrictModeFixer {

	private StrictModeFixer() {
	}

	public static void fixStrictMode() {
		if (VirtualCore.get().isVAppProcess())
			try {
				Field sVmPolicyMask = StrictMode.class.getDeclaredField("sVmPolicyMask");
				sVmPolicyMask.setAccessible(true);
				sVmPolicyMask.setInt(null, 0);
			} catch (Exception ignored) {
			}
	}
}
