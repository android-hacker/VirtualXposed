package com.lody.virtual.helper.compat;

import com.lody.virtual.helper.utils.Reflect;

/**
 * @author Lody
 *
 */
public class VMRuntimeCompat {

	static Reflect THE_ONE_MIRROR;

	static {
		try {
			THE_ONE_MIRROR = Reflect.on("dalvik.system.VMRuntime").call("getRuntime");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void setTargetSdkVersion(int targetSdkVersion) {
		if (THE_ONE_MIRROR != null) {
			try {
				THE_ONE_MIRROR.call("setTargetSdkVersion", targetSdkVersion);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean is64Bit() {
		if (THE_ONE_MIRROR != null) {
			try {
				return THE_ONE_MIRROR.call("is64Bit").get();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
