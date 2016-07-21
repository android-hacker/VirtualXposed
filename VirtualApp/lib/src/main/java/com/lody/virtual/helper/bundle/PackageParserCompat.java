package com.lody.virtual.helper.bundle;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.UserHandle;

/**
 * @author Lody
 *
 * @see PackageParser
 */
public abstract class PackageParserCompat {

	final int mUid;
	{
		if (Build.VERSION.SDK_INT >= 17) {
			mUid = UserHandle.getCallingUserId();
		} else {
			mUid = 0;
		}
	}

	/* package */ PackageParserCompat() {
	}

	public static PackageParserCompat newParser() {
		if (Build.VERSION.SDK_INT >= 21) {
			return new PackageParserV21();
		} else if (Build.VERSION.SDK_INT >= 17) {
			return new PackageParserV17();
		} else if (Build.VERSION.SDK_INT == 16) {
			return new PackageParserV16();
		} else {
			return new PackageParserV15();
		}
	}

	public abstract void parsePackage(File file, int flags) throws Exception;

	public abstract String getPackageName();

	public abstract void collectCertificates(int flags);

	public abstract ActivityInfo generateActivityInfo(PackageParser.Activity activity, int flags);

	public abstract ServiceInfo generateServiceInfo(PackageParser.Service service, int flags);

	public abstract ProviderInfo generateProviderInfo(PackageParser.Provider provider, int flags);

	public abstract ActivityInfo generateReceiverInfo(PackageParser.Activity receiver, int flags);

	public abstract InstrumentationInfo generateInstrumentationInfo(PackageParser.Instrumentation instrumentation,
			int flags);

	public abstract ApplicationInfo generateApplicationInfo(int flags);

	public abstract PermissionGroupInfo generatePermissionGroupInfo(PackageParser.PermissionGroup permissionGroup,
			int flags);

	public abstract PermissionInfo generatePermissionInfo(PackageParser.Permission permission, int flags);

	public abstract PackageInfo generatePackageInfo(int gids[], int flags, long firstInstallTime, long lastUpdateTime,
			HashSet<String> grantedPermissions);

	public abstract List<PackageParser.Activity> getActivities();

	public abstract List<PackageParser.Service> getServices();

	public abstract List<PackageParser.Provider> getProviders();

	public abstract List<PackageParser.Permission> getPermissions();

	public abstract List<PackageParser.PermissionGroup> getPermissionGroups();

	public abstract List<String> getRequestedPermissions();

	public abstract List<PackageParser.Activity> getReceivers();

	public abstract List<PackageParser.Instrumentation> getInstrumentations();

	public abstract void collectManifestDigest();
}
