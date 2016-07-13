package com.lody.virtual.service;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.bundle.APKBundle;
import com.lody.virtual.helper.bundle.IntentResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lody
 *
 */
public class VPackageServiceImpl extends IPackageManager.Stub {

	private static final VPackageServiceImpl gService = new VPackageServiceImpl();
	private PackageManager mPM;

	public static VPackageServiceImpl getService() {
		return gService;
	}

	private Context getContext() {
		return VirtualCore.getCore().getContext();
	}

	private VAppServiceImpl getPMS() {
		return VAppServiceImpl.getService();
	}

	public void onCreate(Context context) {
		this.mPM = VirtualCore.getCore().getUnHookPackageManager();
	}

	public PackageInfo getPackageInfo(String packageName, int flags) {
		try {
			APKBundle bundle = getPMS().getAPKBundle(packageName);
			if (bundle != null) {
				return bundle.getPackageInfo(flags);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public ActivityInfo getActivityInfo(ComponentName componentName, int flags) {
		try {
			String pkgName = componentName.getPackageName();
			if (pkgName != null) {
				APKBundle bundle = getPMS().getAPKBundle(pkgName);
				if (bundle != null) {
					return bundle.getActivityInfo(componentName, flags);
				}
			}
			try {
				return mPM.getActivityInfo(componentName, flags);
			} catch (PackageManager.NameNotFoundException e) {
				// Ignore
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public ActivityInfo getReceiverInfo(ComponentName componentName, int flags) {
		try {
			String pkgName = componentName.getPackageName();
			if (pkgName != null) {
				APKBundle bundle = getPMS().getAPKBundle(pkgName);
				if (bundle != null) {
					return bundle.getReceiverInfo(componentName, flags);
				}
			}
			try {
				return mPM.getReceiverInfo(componentName, flags);
			} catch (PackageManager.NameNotFoundException e) {
				// Ignore
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public ServiceInfo getServiceInfo(ComponentName componentName, int flags) {
		try {
			String pkgName = componentName.getPackageName();
			String className = componentName.getClassName();
			if (pkgName != null && className != null) {
				APKBundle bundle = getPMS().getAPKBundle(pkgName);
				if (bundle != null) {
					return bundle.getServiceInfo(componentName, flags);
				}
			}
			try {
				return mPM.getServiceInfo(componentName, flags);
			} catch (PackageManager.NameNotFoundException e) {
				// Ignore
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public ProviderInfo resolveContentProvider(String auth, int flags) {

		Map<String, APKBundle> bundleMap = VAppServiceImpl.getService().getAllAPKBundles();
		for (APKBundle bundle : bundleMap.values()) {
			try {
				PackageInfo packageInfo = bundle.getPackageInfo(PackageManager.GET_PROVIDERS | flags);
				if (packageInfo.providers != null) {
					for (ProviderInfo providerInfo : packageInfo.providers) {
						if (TextUtils.equals(providerInfo.authority, auth)) {
							return providerInfo;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	public ProviderInfo getProviderInfo(ComponentName componentName, int flags) {
		try {
			String pkg = componentName.getPackageName();
			if (pkg != null) {
				APKBundle bundle = getPMS().getAPKBundle(pkg);
				if (bundle != null) {
					return bundle.getProviderInfo(componentName, flags);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags) {
		try {
			Intent backupIntent = new Intent(intent);
			Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();
			List<ResolveInfo> infos = IntentResolver.resolveIntent(getContext(), bundleMap, backupIntent, resolvedType,
					flags);
			if (infos.size() > 0) {
				return IntentResolver.findBest(infos);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags) {
		Intent backupIntent = new Intent(intent);
		Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();
		try {
			return IntentResolver.resolveActivityIntent(getContext(), bundleMap, backupIntent, resolvedType, flags);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ResolveInfo>(0);
	}

	public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) {
		Intent backupIntent = new Intent(intent);
		Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();
		try {
			return IntentResolver.resolveReceiverIntent(getContext(), bundleMap, backupIntent, resolvedType, flags);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ResolveInfo>(0);
	}

	public ResolveInfo resolveService(Intent intent, String resolvedType, int flags) {
		List<ResolveInfo> list = queryIntentServices(intent, resolvedType, flags);
		if (list.size() > 0) {
			return IntentResolver.findBest(list);
		}
		return null;
	}

	public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) {
		Intent backupIntent = new Intent(intent);
		Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();
		try {
			return IntentResolver.resolveServiceIntent(getContext(), bundleMap, backupIntent, resolvedType, flags);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>(0);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) {
		Intent backupIntent = new Intent(intent);
		Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();
		try {
			return IntentResolver.resolveProviderIntent(getContext(), bundleMap, backupIntent, resolvedType, flags);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ResolveInfo>(0);
	}

	public List<PackageInfo> getInstalledPackages(int flags) {
		List<PackageInfo> installedPkgs = new ArrayList<PackageInfo>(getPMS().getAppCount());
		for (APKBundle bundle : getPMS().getAllAPKBundles().values()) {
			try {
				installedPkgs.add(bundle.getPackageInfo(flags));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return installedPkgs;
	}

	public List<ApplicationInfo> getInstalledApplications(int flags) {
		List<ApplicationInfo> installedApps = new ArrayList<ApplicationInfo>(getPMS().getAppCount());
		Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();
		for (APKBundle bundle : bundleMap.values()) {
			try {
				installedApps.add(bundle.getApplicationInfo(flags));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return installedApps;
	}

	public PermissionInfo getPermissionInfo(String name, int flags) {
		Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();
		for (APKBundle bundle : bundleMap.values()) {
			try {
				ComponentName componentName = new ComponentName(bundle.getPackageName(), name);
				PermissionInfo permissionInfo = bundle.getPermissionInfo(componentName, flags);
				if (permissionInfo != null) {
					return permissionInfo;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			return mPM.getPermissionInfo(name, flags);
		} catch (PackageManager.NameNotFoundException e) {
			// Ignore
		}
		return null;
	}

	public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) {
		Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();

		List<PermissionInfo> list = new ArrayList<PermissionInfo>();

		for (APKBundle bundle : bundleMap.values()) {
			try {
				List<PermissionInfo> permissionInfos = bundle.getPermissions();
				if (permissionInfos == null) {
					continue;
				}
				for (PermissionInfo permissionInfo : permissionInfos) {
					if (TextUtils.equals(permissionInfo.group, group)) {
						list.add(permissionInfo);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (list.isEmpty()) {
			try {
				List<PermissionInfo> permissionInfos = mPM.queryPermissionsByGroup(group, flags);
				if (!permissionInfos.isEmpty()) {
					list.addAll(permissionInfos);
				}
			} catch (PackageManager.NameNotFoundException e) {
				// Ignore
			}
		}
		return list;
	}

	public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) {

		Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();

		for (APKBundle bundle : bundleMap.values()) {
			try {
				ComponentName componentName = new ComponentName(bundle.getPackageName(), name);
				PermissionGroupInfo permissionGroupInfo = bundle.getPermissionGroupInfo(componentName, flags);
				if (permissionGroupInfo != null) {
					return permissionGroupInfo;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			return mPM.getPermissionGroupInfo(name, flags);
		} catch (PackageManager.NameNotFoundException e) {
			// Ignore
		}
		return null;
	}

	public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
		List<PermissionGroupInfo> list = new ArrayList<PermissionGroupInfo>();
		Map<String, APKBundle> bundleMap = getPMS().getAllAPKBundles();
		for (APKBundle bundle : bundleMap.values()) {
			try {
				List<PermissionGroupInfo> permissionGroupInfos = bundle.getPermissionGroups();
				if (permissionGroupInfos != null) {
					list.addAll(permissionGroupInfos);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		List<PermissionGroupInfo> hostGroupInfos = mPM.getAllPermissionGroups(flags);
		list.addAll(hostGroupInfos);
		return list;
	}

	public ApplicationInfo getApplicationInfo(String packageName, int flags) {
		APKBundle bundle = getPMS().getAPKBundle(packageName);
		if (bundle != null) {
			try {
				return bundle.getApplicationInfo(flags);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			return mPM.getApplicationInfo(packageName, flags);
		} catch (PackageManager.NameNotFoundException e) {
			// Ignore
		}
		return null;
	}

	public List<ActivityInfo> getReceivers(String packageName, int flags) {
		APKBundle bundle = getPMS().getAPKBundle(packageName);
		if (bundle != null) {
			try {
				return bundle.getReceivers();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public int checkPermission(String permName, String pkg) {
		String hostPkg = VirtualCore.getCore().getHostPkg();
		return mPM.checkPermission(permName, hostPkg);
	}

	public String[] getPackagesForPid(int pid) {
		return VProcessServiceImpl.getService().findRunningAppPkgByPid(pid);
	}

	public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) {
		if (info != null) {
			APKBundle bundle = getPMS().getAPKBundle(info.packageName);
			if (bundle != null) {
				List<IntentFilter> filters = bundle.getReceiverIntentFilter(info);
				if (filters != null && filters.size() > 0) {
					return filters;
				}
			}
		}
		return null;
	}
}
