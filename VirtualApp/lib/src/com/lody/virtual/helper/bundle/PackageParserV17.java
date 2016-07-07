package com.lody.virtual.helper.bundle;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;

import com.lody.virtual.helper.utils.Reflect;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.util.ArraySet;
import android.util.DisplayMetrics;

/**
 * @author Lody
 *
 */
/* package */ class PackageParserV17 extends PackageParserV15 {

	private static final PackageUserState sUserState = new PackageUserState();

	@Override
	public void parsePackage(File file, int flags) throws Exception {
		DisplayMetrics metrics = new DisplayMetrics();
		metrics.setToDefaults();
		String destCodePath = file.getPath();
		mParser = Reflect.on(PackageParser.class).create(destCodePath).get();
		mPackage = Reflect.on(mParser).call("parsePackage", file, destCodePath, metrics, flags).get();
	}

	@Override
	public ActivityInfo generateActivityInfo(PackageParser.Activity activity, int flags) throws Exception {
		return PackageParser.generateActivityInfo(activity, flags, sUserState, mUid);
	}

	@Override
	public ServiceInfo generateServiceInfo(PackageParser.Service service, int flags) throws Exception {
		return PackageParser.generateServiceInfo(service, flags, sUserState, mUid);
	}

	@Override
	public ActivityInfo generateReceiverInfo(PackageParser.Activity receiver, int flags) throws Exception {
		return PackageParser.generateActivityInfo(receiver, flags, sUserState, mUid);
	}

	@Override
	public ProviderInfo generateProviderInfo(PackageParser.Provider provider, int flags) throws Exception {
		return PackageParser.generateProviderInfo(provider, flags, sUserState, mUid);
	}

	@Override
	public ApplicationInfo generateApplicationInfo(int flags) throws Exception {
		return PackageParser.generateApplicationInfo(mPackage, flags, sUserState, mUid);
	}

	@Override
	public PackageInfo generatePackageInfo(int[] gids, int flags, long firstInstallTime, long lastUpdateTime,
			HashSet<String> grantedPermissions) throws Exception {

		try {
			return PackageParser.generatePackageInfo(mPackage, gids, flags, firstInstallTime, lastUpdateTime,
					grantedPermissions, sUserState, mUid);
		} catch (NoSuchMethodError e) {

			try {
				Method m_generatePackageInfo = PackageParser.class.getDeclaredMethod("generatePackageInfo",
						PackageParser.Package.class, int[].class, int.class, long.class, long.class, HashSet.class,
						PackageUserState.class, int.class);
				return (PackageInfo) m_generatePackageInfo.invoke(null, mPackage, gids, flags, firstInstallTime,
						lastUpdateTime, grantedPermissions, sUserState, mUid);
			} catch (NoSuchMethodException noError) {
				ArraySet<String> grantedPermissionsArray = new ArraySet<String>(grantedPermissions);
				return Reflect.on(PackageParser.class).call("generatePackageInfo", mPackage, gids, flags,
						firstInstallTime, lastUpdateTime, grantedPermissionsArray, sUserState, mUid).get();

			}

		}
	}
}
