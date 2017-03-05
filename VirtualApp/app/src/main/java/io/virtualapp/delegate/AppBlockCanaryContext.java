package io.virtualapp.delegate;

import java.io.File;

import com.github.moduth.blockcanary.BlockCanaryContext;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import io.virtualapp.BuildConfig;
import io.virtualapp.VApp;

/**
 * @author Lody
 *
 */
public class AppBlockCanaryContext extends BlockCanaryContext {
	@Override
	public String getQualifier() {
		String qualifier = "";
		try {
			PackageInfo info = VApp.getApp().getPackageManager().getPackageInfo(VApp.getApp().getPackageName(), 0);
			qualifier += info.versionCode + "_" + info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			// Ignore
		}
		return qualifier;
	}

	@Override
	public String getUid() {
		return "87224330";
	}

	@Override
	public String getNetworkType() {
		return "4G";
	}

	@Override
	public int getConfigDuration() {
		return 9999;
	}

	@Override
	public int getConfigBlockThreshold() {
		return 500;
	}

	@Override
	public boolean isNeedDisplay() {
		return BuildConfig.DEBUG;
	}

	@Override
	public String getLogPath() {
		return "/blockcanary/performance";
	}

	@Override
	public boolean zipLogFile(File[] src, File dest) {
		return false;
	}

	@Override
	public void uploadLogFile(File zippedFile) {

	}
}