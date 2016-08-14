package io.virtualapp;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;

import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
public class VApp extends Application {

	private static final String[] GMS_PKG = {
			"com.android.vending",

			"com.google.android.gsf",
			"com.google.android.gsf.login",
			"com.google.android.gms",

			"com.google.android.backuptransport",
			"com.google.android.backup",
			"com.google.android.configupdater",
			"com.google.android.syncadapters.contacts",
			"com.google.android.feedback",
			"com.google.android.onetimeinitializer",
			"com.google.android.partnersetup",
			"com.google.android.setupwizard",
			"com.google.android.syncadapters.calendar",};
	private static VApp gDefault;

	public static VApp getApp() {
		return gDefault;
	}

	@Override
	protected void attachBaseContext(Context base) {
		try {
			VirtualCore.getCore().startup(base);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
		super.attachBaseContext(base);
	}

	@Override
	public void onCreate() {
		gDefault = this;
		super.onCreate();
		if (VirtualCore.getCore().isMainProcess()) {
			Once.initialise(this);
			// Install the Google mobile service
			installGms();
		}
	}

	private void installGms() {
		PackageManager pm = VirtualCore.getCore().getUnHookPackageManager();
		for (String pkg : GMS_PKG) {
			try {
				ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
				String apkPath = appInfo.sourceDir;
				VirtualCore.getCore().installApp(apkPath,
						InstallStrategy.DEPEND_SYSTEM_IF_EXIST | InstallStrategy.TERMINATE_IF_EXIST);
			} catch (Throwable e) {
				// Ignore
			}
		}
	}

}
