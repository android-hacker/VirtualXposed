package com.lody.virtual.client.stub;

import java.net.URLDecoder;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * @author Lody
 *
 * The base class for cover the original APK installation UI.
 *
 * TODO: Implemention the UI.
 *
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
		if (Constants.ACTION_INSTALL_PACKAGE.equals(action)) {
			try {
				final String apkPath = URLDecoder.decode(data.substring(installScheme.length()), "utf-8");
				Toast.makeText(this, "Installing " + apkPath, Toast.LENGTH_SHORT).show();
				installVirtualApp(apkPath);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (Constants.ACTION_UNINSTALL_PACKAGE.equals(action)) {
			final String packageName = intent.getDataString().substring(uninstallScheme.length());
			Toast.makeText(this, "Uninstalling " + packageName, Toast.LENGTH_SHORT).show();
			unInstallVirtualApp(packageName);
		}

	}

	public void installVirtualApp(String path) throws Throwable {
		int flags = InstallStrategy.UPDATE_IF_EXIST | InstallStrategy.DEPEND_SYSTEM_IF_EXIST;
		VirtualCore.get().installApp(path, flags);
		Toast.makeText(this, "Install finish!", Toast.LENGTH_SHORT).show();
		finish();
	}

	public void unInstallVirtualApp(String packageName) {
		VirtualCore.get().uninstallApp(packageName);
		Toast.makeText(this, "Uninstall finish!", Toast.LENGTH_SHORT).show();
		finish();
	}
}
