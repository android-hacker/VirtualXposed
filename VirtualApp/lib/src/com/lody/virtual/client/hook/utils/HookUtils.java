package com.lody.virtual.client.hook.utils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.ArrayIndex;

/**
 * @author Lody
 *
 */
public class HookUtils {

	public static void replaceFirstAppPkg(Object[] args) {
		int index = ArrayIndex.indexOfFirst(args, String.class);
		if (index != -1) {
			String pkg = (String) args[index];
			if (VirtualCore.getCore().isAppInstalled(pkg)) {
				args[index] = VirtualCore.getCore().getHostPkg();
			}
		}
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
		int index = ArrayIndex.indexOfLast(args, String.class);
		if (index != -1) {
			String pkg = (String) args[index];
			if (VirtualCore.getCore().isAppInstalled(pkg)) {
				args[index] = VirtualCore.getCore().getHostPkg();
			}
		}
	}

	public static void replaceSequenceAppPkg(Object[] args, int sequence) {
		int index = ArrayIndex.indexOf(args, String.class, sequence);
		if (index != -1) {
			String pkg = (String) args[index];
			if (VirtualCore.getCore().isAppInstalled(pkg)) {
				args[index] = VirtualCore.getCore().getHostPkg();
			}
		}
	}

}
