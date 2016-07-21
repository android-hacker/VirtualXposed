package com.lody.virtual.helper.bundle;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import com.lody.virtual.helper.utils.Reflect;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;

/**
 * @author Lody
 *
 */
/* package */ class PackageParserV15 extends PackageParserCompat {

	protected PackageParser mParser;

	protected PackageParser.Package mPackage;

	@Override
	public void parsePackage(File file, int flags) throws Exception {
		mParser = new PackageParser();
		mPackage = mParser.parsePackage(file, flags);
	}

	@Override
	public String getPackageName() throws Exception {
		return mPackage.packageName;
	}

	@Override
	public void collectCertificates(int flags) throws Exception {
		Reflect.on(mParser).call("collectCertificates", mPackage, flags);
	}

	@Override
	public ActivityInfo generateActivityInfo(PackageParser.Activity activity, int flags) throws Exception {
		return Reflect.on(PackageParser.class).call("generateActivityInfo", activity, flags).get();
	}

	@Override
	public ServiceInfo generateServiceInfo(PackageParser.Service service, int flags) throws Exception {
		return Reflect.on(PackageParser.class).call("generateServiceInfo", service, flags).get();
	}

	@Override
	public ProviderInfo generateProviderInfo(PackageParser.Provider provider, int flags) throws Exception {
		return Reflect.on(PackageParser.class).call("generateProviderInfo", provider, flags).get();
	}

	@Override
	public ActivityInfo generateReceiverInfo(PackageParser.Activity receiver, int flags) throws Exception {
		return generateActivityInfo(receiver, flags);
	}

	@Override
	public InstrumentationInfo generateInstrumentationInfo(PackageParser.Instrumentation instrumentation, int flags)
			throws Exception {
		return PackageParser.generateInstrumentationInfo(instrumentation, flags);
	}

	@Override
	public ApplicationInfo generateApplicationInfo(int flags) throws Exception {
		return Reflect.on(PackageParser.class).call("generateApplicationInfo", mPackage, flags).get();
	}

	@Override
	public PermissionGroupInfo generatePermissionGroupInfo(PackageParser.PermissionGroup permissionGroup, int flags)
			throws Exception {
		return PackageParser.generatePermissionGroupInfo(permissionGroup, flags);
	}

	@Override
	public PermissionInfo generatePermissionInfo(PackageParser.Permission permission, int flags) throws Exception {
		return PackageParser.generatePermissionInfo(permission, flags);
	}

	@Override
	public PackageInfo generatePackageInfo(int[] gids, int flags, long firstInstallTime, long lastUpdateTime,
			HashSet<String> grantedPermissions) throws Exception {
		return Reflect.on(PackageParser.class)
				.call("generatePackageInfo", mPackage, gids, flags, firstInstallTime, lastUpdateTime).get();
	}

	@Override
	public List<PackageParser.Activity> getActivities() throws Exception {
		return mPackage.activities;
	}

	@Override
	public List<PackageParser.Service> getServices() throws Exception {
		return mPackage.services;
	}

	@Override
	public List<PackageParser.Provider> getProviders() throws Exception {
		return mPackage.providers;
	}

	@Override
	public List<PackageParser.Permission> getPermissions() throws Exception {
		return mPackage.permissions;
	}

	@Override
	public List<PackageParser.PermissionGroup> getPermissionGroups() throws Exception {
		return mPackage.permissionGroups;
	}

	@Override
	public List<String> getRequestedPermissions() throws Exception {
		return mPackage.requestedPermissions;
	}

	@Override
	public List<PackageParser.Activity> getReceivers() throws Exception {
		return mPackage.receivers;
	}

	@Override
	public List<PackageParser.Instrumentation> getInstrumentations() throws Exception {
		return mPackage.instrumentation;
	}

	@Override
	public void collectManifestDigest() throws Exception {
		mParser.collectManifestDigest(mPackage);
	}

}
