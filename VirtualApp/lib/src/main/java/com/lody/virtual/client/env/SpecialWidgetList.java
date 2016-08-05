package com.lody.virtual.client.env;

import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lody
 *
 * 特殊组件名单
 *
 */
public final class SpecialWidgetList {

	private static final List<String> ACTION_BLACK_LIST = new ArrayList<String>(1);

	private static final Map<String, String> MODIFY_ACTION_MAP = new HashMap<>();

	static {
		ACTION_BLACK_LIST.add("android.appwidget.action.APPWIDGET_UPDATE");
		MODIFY_ACTION_MAP.put(Intent.ACTION_PACKAGE_ADDED,  Constants.VIRTUAL_ACTION_PACKAGE_ADDED);
		MODIFY_ACTION_MAP.put(Intent.ACTION_PACKAGE_REMOVED,  Constants.VIRTUAL_ACTION_PACKAGE_REMOVED);
		MODIFY_ACTION_MAP.put(Intent.ACTION_PACKAGE_CHANGED,  Constants.VIRTUAL_ACTION_PACKAGE_CHANGED);
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

	public static String modifyAction(String originAction) {
		return MODIFY_ACTION_MAP.get(originAction);
	}
}
