package com.lody.virtual.helper.bundle;

import java.util.HashSet;

import com.lody.virtual.helper.utils.Reflect;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;

/**
 * @author Lody
 *
 */
/* package */ class PackageParserV16 extends PackageParserV15 {

	private boolean mStopped = false;
	private int mEnabledState = 0;

	@Override
	public ActivityInfo generateActivityInfo(PackageParser.Activity activity, int flags) {

		return Reflect.on(PackageParser.class)
				.call("generateActivityInfo", activity, flags, mStopped, mEnabledState, mUid).get();
	}

	@Override
	public ServiceInfo generateServiceInfo(PackageParser.Service service, int flags) {

		return Reflect.on(PackageParser.class)
				.call("generateServiceInfo", service, flags, mStopped, mEnabledState, mUid).get();
	}

	@Override
	public ProviderInfo generateProviderInfo(PackageParser.Provider provider, int flags) {

		return Reflect.on(PackageParser.class)
				.call("generateProviderInfo", provider, flags, mStopped, mEnabledState, mUid).get();
	}

	@Override
	public ApplicationInfo generateApplicationInfo(int flags) {
		return Reflect.on(PackageParser.class)
				.call("generateApplicationInfo", mPackage, flags, mStopped, mEnabledState, mUid).get();
	}

	@Override
	public PackageInfo generatePackageInfo(int[] gids, int flags, long firstInstallTime, long lastUpdateTime,
			HashSet<String> grantedPermissions) {

		return Reflect.on(PackageParser.class).call("generatePackageInfo", mPackage, gids, flags, firstInstallTime,
				lastUpdateTime, grantedPermissions, mStopped, mEnabledState, mUid).get();
	}
}
