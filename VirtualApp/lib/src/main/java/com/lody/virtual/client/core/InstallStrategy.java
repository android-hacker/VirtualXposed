package com.lody.virtual.client.core;

/**
 * @author Lody
 *
 *
 *         APK安装策略
 */
public interface InstallStrategy {
	int TERMINATE_IF_EXIST = 10;
	int UPDATE_IF_EXIST = 11;
	int COMPARE_VERSION = 12;
	int IGNORE_NEW_VERSION = 13;
}
