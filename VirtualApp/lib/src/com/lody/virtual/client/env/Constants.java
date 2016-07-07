package com.lody.virtual.client.env;

import com.lody.virtual.client.stub.ShortcutHandleActivity;

/**
 * @author Lody
 *
 */
public class Constants {

	public static final String X_META_KEY_IDENTITY = "X-Identity";

	public static final String X_META_VALUE_STUB = "Stub-User";

	/**
	 * 服务端进程名
	 */
	public static final String SERVER_PROCESS_NAME = ":x";
	/**
	 * PendingIntent代理广播的Action
	 */
	public static final String ACTION_DELEGATE_PENDING_INTENT = "com.lody.virtual.action.DELEGATE_PENDING_INTENT";
	/**
	 * 安装Shortcut的广播
	 */
	public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
	/**
	 * 卸载Shortcut的广播
	 */
	public static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";
	public static String SHORTCUT_PROXY_ACTIVITY_NAME = ShortcutHandleActivity.class.getName();

	public static boolean USE_DIY_SERVICE_ENV = true;
}
