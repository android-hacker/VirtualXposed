package com.lody.virtual.client.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Class: Created by andy on 16-8-3. TODO:
 */
public class InstallerActivity extends Activity {
	public static String installScheme = "file://";
	public static String uninstallScheme = "package:";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent == null || intent.getAction() == null || intent.getDataString() == null) {
			finish();
			return;
		}

		String action = intent.getAction();
		String data = intent.getDataString();
		if (ServiceManagerNative.ACTION_INSTALL_PACKAGE.equals(action)) {
			try {
				final String apkPath = URLDecoder.decode(data.substring(installScheme.length()), "utf-8");
				installVirtualApp(apkPath);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (ServiceManagerNative.ACTION_UNINSTALL_PACKAGE.equals(action)) {
			final String packageName = intent.getDataString().substring(uninstallScheme.length());
			unInstallVirtualApp(packageName);
		}

		finish();
	}

	public void installVirtualApp(String path) throws Throwable {
		int flags = InstallStrategy.UPDATE_IF_EXIST;
		VirtualCore.getCore().installApp(path, flags);
	}

	public void unInstallVirtualApp(String packageName) {
		VirtualCore.getCore().uninstallApp(packageName);
	}
}
