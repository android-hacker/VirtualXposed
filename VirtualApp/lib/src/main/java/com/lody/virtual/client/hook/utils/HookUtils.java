package com.lody.virtual.client.hook.utils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.ArrayUtils;

/**
 * @author Lody
 *
 */
public class HookUtils {

	public static String replaceFirstAppPkg(Object[] args) {
		int index = ArrayUtils.indexOfFirst(args, String.class);
		if (index != -1) {
			String pkg = (String) args[index];
			if (VirtualCore.getCore().isAppInstalled(pkg)) {
				args[index] = VirtualCore.getCore().getHostPkg();
			}
            return pkg;
		}
        return null;
	}

	public static void replaceAppPkg(Object[] args) {
		for (int N = 0; N < args.length; N++) {
			if (args[N] instanceof String && VirtualCore.getCore().isAppInstalled((String) args[N])) {
				args[N] = VirtualCore.getCore().getHostPkg();
				break;
			}
		}
	}

	public static void replaceLastAppPkg(Object[] args) {
		int index = ArrayUtils.indexOfLast(args, String.class);
		if (index != -1) {
			String pkg = (String) args[index];
			if (VirtualCore.getCore().isAppInstalled(pkg)) {
				args[index] = VirtualCore.getCore().getHostPkg();
			}
		}
	}

	public static void replaceSequenceAppPkg(Object[] args, int sequence) {
		int index = ArrayUtils.indexOf(args, String.class, sequence);
		if (index != -1) {
			String pkg = (String) args[index];
			if (VirtualCore.getCore().isAppInstalled(pkg)) {
				args[index] = VirtualCore.getCore().getHostPkg();
			}
		}
	}

}
