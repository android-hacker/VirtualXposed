package com.lody.virtual.client.env;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 *
 *
 *         存放一些插件组件黑名单
 */
public final class BlackList {

	private static final List<String> ACTION_BLACK_LIST = new ArrayList<String>(1);
	private static final List<String> PKG_BLACK_LIST = new ArrayList<String>(2);


	static {
		ACTION_BLACK_LIST.add("android.appwidget.action.APPWIDGET_UPDATE");
	}

	/**
	 * 是否为黑名单Action
	 * 
	 * @param action
	 *            Action
	 */
	public static boolean isActionInBlackList(String action) {
		return ACTION_BLACK_LIST.contains(action);
	}

	/**
	 * 添加一个黑名单 Action
	 * 
	 * @param action
	 *            action
	 */
	public static void addBlackAction(String action) {
		ACTION_BLACK_LIST.add(action);
	}

	public static boolean isBlackPkg(String pkgName) {
		return PKG_BLACK_LIST.contains(pkgName);
	}
}
