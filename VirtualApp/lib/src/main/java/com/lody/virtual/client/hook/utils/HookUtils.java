package com.lody.virtual.client.hook.utils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.ArrayUtils;

/**
 * @author Lody
 *
 */
public class HookUtils {

	public static String replaceFirstAppPkg(Object[] args) {
		if (args == null) {
			return null;
		}
		int index = ArrayUtils.indexOfFirst(args, String.class);
		if (index != -1) {
			String pkg = (String) args[index];
			args[index] = VirtualCore.get().getHostPkg();
			return pkg;
		}
		return null;
	}

	public static String replaceLastAppPkg(Object[] args) {
		int index = ArrayUtils.indexOfLast(args, String.class);
		if (index != -1) {
			String pkg = (String) args[index];
			args[index] = VirtualCore.get().getHostPkg();
			return pkg;
		}
		return null;
	}

	public static String replaceSequenceAppPkg(Object[] args, int sequence) {
		int index = ArrayUtils.indexOf(args, String.class, sequence);
		if (index != -1) {
			String pkg = (String) args[index];
			args[index] = VirtualCore.get().getHostPkg();
			return pkg;
		}
		return null;
	}

}
