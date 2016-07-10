package com.lody.virtual.helper.loaders;

import com.lody.virtual.helper.proto.AppInfo;

import dalvik.system.DexClassLoader;

/**
 * @author Lody
 *
 */
public class DexAppClassLoader extends DexClassLoader {

	public DexAppClassLoader(AppInfo appInfo) {
		super(appInfo.apkPath, appInfo.odexDir, appInfo.libDir, ClassLoader.getSystemClassLoader().getParent());
	}
}
