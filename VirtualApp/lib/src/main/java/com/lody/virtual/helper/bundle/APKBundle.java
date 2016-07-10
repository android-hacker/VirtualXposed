package com.lody.virtual.helper.bundle;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.utils.FileIO;
import com.lody.virtual.service.AppFileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Lody
 *
 */
public class APKBundle {

	private final PackageInfo mHostPkgInfo = VirtualCore.getCore().getHostPkgInfo();
	private final String mPackageName;
	private PackageParserCompat mParser;
	private File mApkFile;

	private AppInfo mAppInfo;

	//////////////
	////////////// Cache
	//////////////
	private Map<ComponentName, PackageParser.Activity> mActivityObjCache = new TreeMap<ComponentName, PackageParser.Activity>();
	private Map<ComponentName, PackageParser.Service> mServiceObjCache = new TreeMap<ComponentName, PackageParser.Service>();
	private Map<ComponentName, PackageParser.Provider> mProviderObjCache = new TreeMap<ComponentName, PackageParser.Provider>();
	private Map<ComponentName, PackageParser.Activity> mReceiversObjCache = new TreeMap<ComponentName, PackageParser.Activity>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, PackageParser.Permission> mPermissionsObjCache = new TreeMap<ComponentName, PackageParser.Permission>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, PackageParser.PermissionGroup> mPermissionGroupObjCache = new TreeMap<ComponentName, PackageParser.PermissionGroup>(
			ComponentNameComparator.sComparator);
	private HashSet<String> mRequestedPermissionsCache = new HashSet<String>();

	private Map<ComponentName, List<IntentFilter>> mActivityIntentFilterCache = new TreeMap<ComponentName, List<IntentFilter>>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, List<IntentFilter>> mServiceIntentFilterCache = new TreeMap<ComponentName, List<IntentFilter>>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, List<IntentFilter>> mProviderIntentFilterCache = new TreeMap<ComponentName, List<IntentFilter>>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, List<IntentFilter>> mReceiverIntentFilterCache = new TreeMap<ComponentName, List<IntentFilter>>(
			ComponentNameComparator.sComparator);

	private Map<ComponentName, ActivityInfo> mActivityInfoCache = new TreeMap<ComponentName, ActivityInfo>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, ServiceInfo> mServiceInfoCache = new TreeMap<ComponentName, ServiceInfo>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, ProviderInfo> mProviderInfoCache = new TreeMap<ComponentName, ProviderInfo>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, ActivityInfo> mReceiversInfoCache = new TreeMap<ComponentName, ActivityInfo>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, InstrumentationInfo> mInstrumentationInfoCache = new TreeMap<ComponentName, InstrumentationInfo>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, PermissionGroupInfo> mPermissionGroupInfoCache = new TreeMap<ComponentName, PermissionGroupInfo>(
			ComponentNameComparator.sComparator);
	private Map<ComponentName, PermissionInfo> mPermissionsInfoCache = new TreeMap<ComponentName, PermissionInfo>(
			ComponentNameComparator.sComparator);
	/////////////////////////
	/////////////////////////
	/////////////////////////

    public APKBundle(File apkFile) throws Exception {
        this(apkFile, false);
    }

	public APKBundle(File apkFile, boolean local) throws Exception {
		this.mApkFile = apkFile;
		mParser = PackageParserCompat.newParser();
		mParser.parsePackage(apkFile, 0);
		mPackageName = mParser.getPackageName();

		if (!local && !VirtualCore.getCore().isOutsideInstalled(mPackageName)) {
			File storeFile = AppFileSystem.getDefault().getAppApkFile(mPackageName);
			File parentFolder = storeFile.getParentFile();
			if (!parentFolder.exists()) {
				parentFolder.mkdirs();
			}
			if (!storeFile.exists()) {
				FileIO.copyFile(apkFile, storeFile);
			}
			this.mApkFile = storeFile;
		}
		mAppInfo = createAppInfo(this);
		List<PackageParser.Activity> activities = mParser.getActivities();
		for (PackageParser.Activity activity : activities) {
			ComponentName componentName = new ComponentName(mPackageName, activity.className);
			mActivityObjCache.put(componentName, activity);
			ActivityInfo activityInfo = mParser.generateActivityInfo(activity, 0);
			initApplicationInfo(activityInfo.applicationInfo);
			initProcessName(activityInfo);
			mActivityInfoCache.put(componentName, activityInfo);
			List<PackageParser.ActivityIntentInfo> filters = activity.intents;
			mActivityIntentFilterCache.remove(componentName);
			mActivityIntentFilterCache.put(componentName, new ArrayList<IntentFilter>(filters));
		}
		List<PackageParser.Service> services = mParser.getServices();
		for (PackageParser.Service service : services) {
			ComponentName componentName = new ComponentName(mPackageName, service.className);
			mServiceObjCache.put(componentName, service);
			ServiceInfo serviceInfo = mParser.generateServiceInfo(service, 0);
			initApplicationInfo(serviceInfo.applicationInfo);
			initProcessName(serviceInfo);
			mServiceInfoCache.put(componentName, serviceInfo);
			List<PackageParser.ServiceIntentInfo> filters = service.intents;
			mServiceIntentFilterCache.remove(componentName);
			mServiceIntentFilterCache.put(componentName, new ArrayList<IntentFilter>(filters));
		}
		List<PackageParser.Provider> providers = mParser.getProviders();
		for (PackageParser.Provider provider : providers) {
			ComponentName componentName = new ComponentName(mPackageName, provider.className);
			mProviderObjCache.put(componentName, provider);
			ProviderInfo providerInfo = mParser.generateProviderInfo(provider, 0);
			initApplicationInfo(providerInfo.applicationInfo);
			initProcessName(providerInfo);
			mProviderInfoCache.put(componentName, providerInfo);
			List<PackageParser.ProviderIntentInfo> filters = provider.intents;
			mProviderIntentFilterCache.remove(componentName);
			mProviderIntentFilterCache.put(componentName, new ArrayList<IntentFilter>(filters));
		}
		List<PackageParser.Activity> receivers = mParser.getReceivers();
		for (PackageParser.Activity receiver : receivers) {
			ComponentName componentName = new ComponentName(mPackageName, receiver.className);
			mReceiversObjCache.put(componentName, receiver);
			ActivityInfo receiverInfo = mParser.generateActivityInfo(receiver, 0);
			initApplicationInfo(receiverInfo.applicationInfo);
			initProcessName(receiverInfo);
			mReceiversInfoCache.put(componentName, receiverInfo);
			List<PackageParser.ActivityIntentInfo> filters = receiver.intents;
			mReceiverIntentFilterCache.put(componentName, new ArrayList<IntentFilter>(filters));
		}
		List<PackageParser.Permission> permissions = mParser.getPermissions();
		for (PackageParser.Permission permission : permissions) {
			ComponentName componentName = new ComponentName(mPackageName, permission.info.name);
			mPermissionsObjCache.put(componentName, permission);
			PermissionInfo permissionInfo = mParser.generatePermissionInfo(permission, 0);
			mPermissionsInfoCache.put(componentName, permissionInfo);
		}

		List<PackageParser.PermissionGroup> permissionGroups = mParser.getPermissionGroups();
		for (PackageParser.PermissionGroup permissionGroup : permissionGroups) {
			ComponentName componentName = new ComponentName(mPackageName, permissionGroup.info.name);
			mPermissionGroupObjCache.put(componentName, permissionGroup);
			PermissionGroupInfo permissionGroupInfo = mParser.generatePermissionGroupInfo(permissionGroup, 0);
			mPermissionGroupInfoCache.put(componentName, permissionGroupInfo);
		}
		List<String> requestedPermissions = mParser.getRequestedPermissions();
		if (requestedPermissions != null && requestedPermissions.size() > 0) {
			mRequestedPermissionsCache.addAll(requestedPermissions);
		}
		mAppInfo.applicationInfo = getApplicationInfo(0);
	}

	private static void initProcessName(ComponentInfo componentInfo) {
		if (TextUtils.isEmpty(componentInfo.processName)) {
			componentInfo.processName = componentInfo.packageName;
		}
	}

	private static AppInfo createAppInfo(APKBundle bundle) throws Exception {
		AppInfo appInfo = new AppInfo();
		appInfo.apkPath = bundle.mApkFile.getPath();
		String pkg = bundle.getPackageName();
		appInfo.packageName = pkg;
		AppFileSystem fileSystem = AppFileSystem.getDefault();
		File dataFolder = fileSystem.getAppPackageFolder(pkg);
		File libFolder = fileSystem.getAppLibFolder(pkg);
		File dvmCacheFolder = fileSystem.getAppDVMCacheFolder(pkg);
		File cacheFolder = fileSystem.getAppCacheFolder(pkg);

		ensureFoldersCreated(dataFolder, libFolder, dvmCacheFolder, cacheFolder);
		appInfo.dataDir = dataFolder.getPath();
		appInfo.libDir = libFolder.getPath();
		appInfo.cacheDir = cacheFolder.getPath();
		appInfo.odexDir = dvmCacheFolder.getPath();

		return appInfo;
	}

	private static void ensureFoldersCreated(File... folders) {
		for (File folder : folders) {
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
	}

	public String getPackageName() {
		return mPackageName;
	}

	public File getAPKFile() {
		return mApkFile;
	}

	public void collectCertificates(int flags) throws Exception {
		mParser.collectCertificates(flags);
	}

	public List<IntentFilter> getActivityIntentFilter(ComponentName className) {
		return mActivityIntentFilterCache.get(className);
	}

	public List<IntentFilter> getServiceIntentFilter(ComponentName className) {
		return mServiceIntentFilterCache.get(className);
	}

	public List<IntentFilter> getProviderIntentFilter(ComponentName className) {
		return mProviderIntentFilterCache.get(className);
	}

	public ActivityInfo getActivityInfo(ComponentName className, int flags) throws Exception {
		PackageParser.Activity activity = mActivityObjCache.get(className);
		if (activity != null) {
			ActivityInfo activityInfo = mParser.generateActivityInfo(activity, flags);
			initApplicationInfo(activityInfo.applicationInfo);
			initProcessName(activityInfo);
			return activityInfo;
		}
		return null;
	}

	public ServiceInfo getServiceInfo(ComponentName className, int flags) throws Exception {
		PackageParser.Service service = mServiceObjCache.get(className);;
		if (service != null) {
			ServiceInfo serviceInfo = mParser.generateServiceInfo(service, flags);
			initApplicationInfo(serviceInfo.applicationInfo);
			initProcessName(serviceInfo);
			return serviceInfo;
		}
		return null;
	}

	public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws Exception {
		PackageParser.Activity receiver = mReceiversObjCache.get(className);
		if (receiver != null) {
			ActivityInfo receiverInfo = mParser.generateReceiverInfo(receiver, flags);
			initApplicationInfo(receiverInfo.applicationInfo);
			initProcessName(receiverInfo);
			return receiverInfo;
		}
		return null;
	}

	public ProviderInfo getProviderInfo(ComponentName className, int flags) throws Exception {
		PackageParser.Provider provider = mProviderObjCache.get(className);
		if (provider != null) {
			ProviderInfo providerInfo = mParser.generateProviderInfo(provider, flags);
			initApplicationInfo(providerInfo.applicationInfo);
			initProcessName(providerInfo);
			return providerInfo;
		}
		return null;
	}

	public ApplicationInfo getApplicationInfo(int flags) throws Exception {
		ApplicationInfo applicationInfo = mParser.generateApplicationInfo(flags);
		initApplicationInfo(applicationInfo);
		if (TextUtils.isEmpty(applicationInfo.processName)) {
			applicationInfo.processName = applicationInfo.packageName;
		}
		return applicationInfo;
	}

	public PermissionGroupInfo getPermissionGroupInfo(ComponentName className, int flags) throws Exception {
		PackageParser.PermissionGroup permissionGroup = mPermissionGroupObjCache.get(className);
		if (permissionGroup != null) {
			return mParser.generatePermissionGroupInfo(permissionGroup, flags);
		}
		return null;
	}

	public PermissionInfo getPermissionInfo(ComponentName className, int flags) throws Exception {
		PackageParser.Permission data = mPermissionsObjCache.get(className);
		if (data != null) {
			return mParser.generatePermissionInfo(data, flags);
		}
		return null;
	}

	public PackageInfo getPackageInfo(int flags) throws Exception {
		if ((flags & PackageManager.GET_SIGNATURES) != 0) {
			try {
				mParser.collectManifestDigest();
			} catch (Throwable e) {
				// Ignore
			}
			mParser.collectCertificates(0);
		}
		PackageInfo packageInfo = mParser.generatePackageInfo(mHostPkgInfo.gids, flags, mApkFile.lastModified(),
				System.currentTimeMillis(), null);
		initPackageInfo(packageInfo);
		return packageInfo;
	}

	public HashSet<String> getRequestedPermissions() throws Exception {
		return new HashSet<String>(mRequestedPermissionsCache);
	}

	public Map<ActivityInfo, List<IntentFilter>> getReceiverIntentFilter() {
		Map<ActivityInfo, List<IntentFilter>> map = new HashMap<ActivityInfo, List<IntentFilter>>();
		for (ComponentName componentName : mReceiverIntentFilterCache.keySet()) {
			map.put(mReceiversInfoCache.get(componentName), mReceiverIntentFilterCache.get(componentName));
		}
		return map;
	}

	public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) {
		for (ComponentName componentName : mReceiverIntentFilterCache.keySet()) {
			if (TextUtils.equals(info.name, mReceiversInfoCache.get(componentName).name)) {
				return mReceiverIntentFilterCache.get(componentName);
			}
		}
		return null;
	}

	public List<ActivityInfo> getActivities() throws Exception {
		return new ArrayList<ActivityInfo>(mActivityInfoCache.values());
	}

	public List<ServiceInfo> getServices() throws Exception {
		return new ArrayList<ServiceInfo>(mServiceInfoCache.values());
	}

	public List<ProviderInfo> getProviders() throws Exception {
		return new ArrayList<ProviderInfo>(mProviderInfoCache.values());
	}

	public List<ActivityInfo> getReceivers() throws Exception {
		return new ArrayList<ActivityInfo>(mReceiversInfoCache.values());
	}

	public List<PermissionInfo> getPermissions() throws Exception {
		return new ArrayList<PermissionInfo>(mPermissionsInfoCache.values());
	}

	public List<PermissionGroupInfo> getPermissionGroups() throws Exception {
		return new ArrayList<PermissionGroupInfo>(mPermissionGroupInfoCache.values());
	}

	public List<InstrumentationInfo> getInstrumentationInfos() {
		return new ArrayList<InstrumentationInfo>(mInstrumentationInfoCache.values());
	}

	private void initPackageInfo(PackageInfo packageInfo) {
		packageInfo.gids = mHostPkgInfo.gids;
		initApplicationInfo(packageInfo.applicationInfo);
		if (packageInfo.activities != null) {
			for (ActivityInfo activityInfo : packageInfo.activities) {
				initApplicationInfo(activityInfo.applicationInfo);
			}
		}
		if (packageInfo.services != null) {
			for (ServiceInfo serviceInfo : packageInfo.services) {
				initApplicationInfo(serviceInfo.applicationInfo);
			}
		}
		if (packageInfo.receivers != null) {
			for (ActivityInfo receiverInfo : packageInfo.receivers) {
				initApplicationInfo(receiverInfo.applicationInfo);
			}
		}
		if (packageInfo.providers != null) {
			for (ProviderInfo providerInfo : packageInfo.providers) {
				initApplicationInfo(providerInfo.applicationInfo);
			}
		}
	}

	private String fixComponentClassName(String className) {
		if (className != null) {
			if (className.charAt(0) == '.') {
				return getPackageName() + className;
			}
			return className;
		}
		return null;
	}

	private void initApplicationInfo(ApplicationInfo applicationInfo) {
		if (TextUtils.isEmpty(applicationInfo.processName)) {
			applicationInfo.processName = applicationInfo.packageName;
		}
		applicationInfo.name = fixComponentClassName(applicationInfo.name);
		applicationInfo.publicSourceDir = mApkFile.getPath();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
            applicationInfo.flags &= -ApplicationInfo.FLAG_STOPPED;
        }
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				applicationInfo.splitSourceDirs = new String[]{mApkFile.getPath()};
				applicationInfo.splitPublicSourceDirs = applicationInfo.splitSourceDirs;
			}
		} catch (Throwable e) {
			// ignore
		}
		try {
			if (applicationInfo.scanSourceDir == null) {
				applicationInfo.scanSourceDir = applicationInfo.dataDir;
			}
			if (applicationInfo.scanPublicSourceDir == null) {
				applicationInfo.scanPublicSourceDir = applicationInfo.dataDir;
			}
		} catch (Throwable e) {
			// Ignore
		}
		applicationInfo.sourceDir = mApkFile.getPath();
		applicationInfo.dataDir = mAppInfo.dataDir;
		applicationInfo.enabled = true;
		applicationInfo.nativeLibraryDir = mAppInfo.libDir;
		applicationInfo.uid = Process.myUid();
	}

	public AppInfo getAppInfo() {
		return mAppInfo;
	}
}
