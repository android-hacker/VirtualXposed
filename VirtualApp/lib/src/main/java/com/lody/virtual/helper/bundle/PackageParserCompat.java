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
import android.os.Process;
import android.os.UserHandle;

/**
 * @author Lody
 *
 * @see PackageParser
 */
public abstract class PackageParserCompat {

	protected final int mUid;
	{
		if (Build.VERSION.SDK_INT >= 17) {
			mUid = UserHandle.getCallingUserId();
		} else {
			mUid = Process.myUid();
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

	public abstract String getPackageName() throws Exception;

	public abstract void collectCertificates(int flags) throws Exception;

	public abstract ActivityInfo generateActivityInfo(PackageParser.Activity activity, int flags) throws Exception;

	public abstract ServiceInfo generateServiceInfo(PackageParser.Service service, int flags) throws Exception;

	public abstract ProviderInfo generateProviderInfo(PackageParser.Provider provider, int flags) throws Exception;

	public abstract ActivityInfo generateReceiverInfo(PackageParser.Activity receiver, int flags) throws Exception;

	public abstract InstrumentationInfo generateInstrumentationInfo(PackageParser.Instrumentation instrumentation,
			int flags) throws Exception;

	public abstract ApplicationInfo generateApplicationInfo(int flags) throws Exception;

	public abstract PermissionGroupInfo generatePermissionGroupInfo(PackageParser.PermissionGroup permissionGroup,
			int flags) throws Exception;

	public abstract PermissionInfo generatePermissionInfo(PackageParser.Permission permission, int flags)
			throws Exception;

	public abstract PackageInfo generatePackageInfo(int gids[], int flags, long firstInstallTime, long lastUpdateTime,
			HashSet<String> grantedPermissions) throws Exception;

	public abstract List<PackageParser.Activity> getActivities() throws Exception;

	public abstract List<PackageParser.Service> getServices() throws Exception;

	public abstract List<PackageParser.Provider> getProviders() throws Exception;

	public abstract List<PackageParser.Permission> getPermissions() throws Exception;

	public abstract List<PackageParser.PermissionGroup> getPermissionGroups() throws Exception;

	public abstract List<String> getRequestedPermissions() throws Exception;

	public abstract List<PackageParser.Activity> getReceivers() throws Exception;

	public abstract List<PackageParser.Instrumentation> getInstrumentations() throws Exception;

	public abstract void collectManifestDigest() throws Exception;
}
