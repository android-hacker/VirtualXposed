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
		try {
			mPackage = mParser.parsePackage(file, flags);
		} catch (PackageParser.PackageParserException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getPackageName() {
		return mPackage.packageName;
	}

	@Override
	public void collectCertificates(int flags) {
		Reflect.on(mParser).call("collectCertificates", mPackage, flags);
	}

	@Override
	public ActivityInfo generateActivityInfo(PackageParser.Activity activity, int flags) {
		return Reflect.on(PackageParser.class).call("generateActivityInfo", activity, flags).get();
	}

	@Override
	public ServiceInfo generateServiceInfo(PackageParser.Service service, int flags) {
		return Reflect.on(PackageParser.class).call("generateServiceInfo", service, flags).get();
	}

	@Override
	public ProviderInfo generateProviderInfo(PackageParser.Provider provider, int flags) {
		return Reflect.on(PackageParser.class).call("generateProviderInfo", provider, flags).get();
	}

	@Override
	public ActivityInfo generateReceiverInfo(PackageParser.Activity receiver, int flags) {
		return generateActivityInfo(receiver, flags);
	}

	@Override
	public InstrumentationInfo generateInstrumentationInfo(PackageParser.Instrumentation instrumentation, int flags) {
		return PackageParser.generateInstrumentationInfo(instrumentation, flags);
	}

	@Override
	public ApplicationInfo generateApplicationInfo(int flags) {
		return Reflect.on(PackageParser.class).call("generateApplicationInfo", mPackage, flags).get();
	}

	@Override
	public PermissionGroupInfo generatePermissionGroupInfo(PackageParser.PermissionGroup permissionGroup, int flags) {
		return PackageParser.generatePermissionGroupInfo(permissionGroup, flags);
	}

	@Override
	public PermissionInfo generatePermissionInfo(PackageParser.Permission permission, int flags) {
		return PackageParser.generatePermissionInfo(permission, flags);
	}

	@Override
	public PackageInfo generatePackageInfo(int[] gids, int flags, long firstInstallTime, long lastUpdateTime,
			HashSet<String> grantedPermissions) {
		return Reflect.on(PackageParser.class)
				.call("generatePackageInfo", mPackage, gids, flags, firstInstallTime, lastUpdateTime).get();
	}

	@Override
	public List<PackageParser.Activity> getActivities() {
		return mPackage.activities;
	}

	@Override
	public List<PackageParser.Service> getServices() {
		return mPackage.services;
	}

	@Override
	public List<PackageParser.Provider> getProviders() {
		return mPackage.providers;
	}

	@Override
	public List<PackageParser.Permission> getPermissions() {
		return mPackage.permissions;
	}

	@Override
	public List<PackageParser.PermissionGroup> getPermissionGroups() {
		return mPackage.permissionGroups;
	}

	@Override
	public List<String> getRequestedPermissions() {
		return mPackage.requestedPermissions;
	}

	@Override
	public List<PackageParser.Activity> getReceivers() {
		return mPackage.receivers;
	}

	@Override
	public List<PackageParser.Instrumentation> getInstrumentations() {
		return mPackage.instrumentation;
	}

	@Override
	public void collectManifestDigest() {
		try {
			mParser.collectManifestDigest(mPackage);
		} catch (PackageParser.PackageParserException e) {
			e.printStackTrace();
		}
	}

}
