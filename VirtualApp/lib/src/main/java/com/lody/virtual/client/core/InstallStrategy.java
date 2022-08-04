package com.lody.virtual.client.core;

/**
 * @author Lody
 *
 *
 */
public interface InstallStrategy {
	int TERMINATE_IF_EXIST = 0x01 << 1;
	int UPDATE_IF_EXIST = 0x01 << 2;
	int COMPARE_VERSION = 0X01 << 3;
	int IGNORE_NEW_VERSION = 0x01 << 4;
	int DEPEND_SYSTEM_IF_EXIST = 0x01 << 5;
	int SKIP_DEX_OPT = 0x01 << 6;
}
