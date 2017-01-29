package com.lody.virtual.server.pm;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.LogPrinter;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.fixer.ComponentFixer;
import com.lody.virtual.helper.compat.ObjectsCompat;
import com.lody.virtual.helper.compat.PackageParserCompat;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.proto.ReceiverInfo;
import com.lody.virtual.helper.proto.VParceledListSlice;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.IPackageManager;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lody
 *
 */
public class VPackageManagerService extends IPackageManager.Stub {

	static final String TAG = "PackageManager";

	private static final boolean DEBUG_SHOW_INFO = false;
	private static final AtomicReference<VPackageManagerService> gService = new AtomicReference<>();
	static final Comparator<ResolveInfo> mResolvePrioritySorter = new Comparator<ResolveInfo>() {
		public int compare(ResolveInfo r1, ResolveInfo r2) {
			int v1 = r1.priority;
			int v2 = r2.priority;
			if (v1 != v2) {
				return (v1 > v2) ? -1 : 1;
			}
			v1 = r1.preferredOrder;
			v2 = r2.preferredOrder;
			if (v1 != v2) {
				return (v1 > v2) ? -1 : 1;
			}
			if (r1.isDefault != r2.isDefault) {
				return r1.isDefault ? -1 : 1;
			}
			v1 = r1.match;
			v2 = r2.match;
			if (v1 != v2) {
				return (v1 > v2) ? -1 : 1;
			}
			return 0;
		}
	};
	private static final Comparator<ProviderInfo> mProviderInitOrderSorter = new Comparator<ProviderInfo>() {
		public int compare(ProviderInfo p1, ProviderInfo p2) {
			final int v1 = p1.initOrder;
			final int v2 = p2.initOrder;
			return (v1 > v2) ? -1 : ((v1 < v2) ? 1 : 0);
		}
	};

	final ActivityIntentResolver mActivities = new ActivityIntentResolver();
	final ServiceIntentResolver mServices = new ServiceIntentResolver();
	final ActivityIntentResolver mReceivers = new ActivityIntentResolver();
	final ProviderIntentResolver mProviders = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?  new ProviderIntentResolver() : null;

	final HashMap<ComponentName, PackageParser.Provider> mProvidersByComponent = new HashMap<>();

	final HashMap<String, PackageParser.Permission> mPermissions = new HashMap<>();
	final HashMap<String, PackageParser.PermissionGroup> mPermissionGroups = new HashMap<>();
	final HashMap<String, PackageParser.Provider> mProvidersByAuthority = new HashMap<>();

	private final Map<String, PackageParser.Package> mPackages = PackageCache.sPackageCaches;



	public static void systemReady() {
		VPackageManagerService instance = new VPackageManagerService();
		new VUserManagerService(VirtualCore.get().getContext(), instance, new char[0], instance.mPackages);
		gService.set(instance);
	}

	public static VPackageManagerService get() {
		return gService.get();
	}

	private static boolean isSystemApp(ApplicationInfo info) {
		return info != null && (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
	}

	public void analyzePackageLocked(PackageParser.Package pkg) {
		int N = pkg.activities.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Activity a = pkg.activities.get(i);
			if (a.info.processName == null) {
				a.info.processName = a.info.packageName;
			}
			mActivities.addActivity(a, "activity");
		}
		N = pkg.services.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Service a = pkg.services.get(i);
			if (a.info.processName == null) {
				a.info.processName = a.info.packageName;
			}
			mServices.addService(a);
		}
		N = pkg.receivers.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Activity a = pkg.receivers.get(i);
			if (a.info.processName == null) {
				a.info.processName = a.info.packageName;
			}
			mReceivers.addActivity(a, "receiver");
		}

		N = pkg.providers.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Provider p = pkg.providers.get(i);
			if (p.info.processName == null) {
				p.info.processName = p.info.packageName;
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mProviders.addProvider(p);
			}
			String names[] = p.info.authority.split(";");
			for (String name : names) {
				if (!mProvidersByAuthority.containsKey(name)) {
					mProvidersByAuthority.put(name, p);
				}
			}
			mProvidersByComponent.put(p.getComponentName(), p);
		}

		N = pkg.permissions.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Permission permission = pkg.permissions.get(i);
			mPermissions.put(permission.className, permission);
		}
		N = pkg.permissionGroups.size();
		for (int i = 0; i < N; i++) {
			PackageParser.PermissionGroup group = pkg.permissionGroups.get(i);
			mPermissionGroups.put(group.className, group);
		}
	}

	public void deletePackageLocked(String packageName) {
		PackageParser.Package pkg = mPackages.get(packageName);
		if (pkg == null) {
			return;
		}
		int N = pkg.activities.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Activity a = pkg.activities.get(i);
			mActivities.removeActivity(a, "activity");
		}
		N = pkg.services.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Service a = pkg.services.get(i);
			mServices.removeService(a);
		}
		N = pkg.receivers.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Activity a = pkg.receivers.get(i);
			mReceivers.removeActivity(a, "receiver");
		}

		N = pkg.providers.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Provider p = pkg.providers.get(i);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mProviders.removeProvider(p);
			}
			String names[] = p.info.authority.split(";");
			for (String name : names) {
				mProvidersByAuthority.remove(name);
			}
			mProvidersByComponent.remove(p.getComponentName());
		}

		N = pkg.permissions.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Permission permission = pkg.permissions.get(i);
			mPermissions.remove(permission.className);
		}
		N = pkg.permissionGroups.size();
		for (int i = 0; i < N; i++) {
			PackageParser.PermissionGroup group = pkg.permissionGroups.get(i);
			mPermissionGroups.remove(group.className);
		}
	}

	@Override
	public List<String> getSharedLibraries(String pkgName) {
		synchronized (mPackages) {
			PackageParser.Package p = mPackages.get(pkgName);
			if (p != null) {
				return p.usesLibraries;
			}
			return null;
		}
	}

	@Override
	public int checkPermission(String permName, String pkgName, int userId) {
		if ("android.permission.INTERACT_ACROSS_USERS".equals(permName)
				|| "android.permission.INTERACT_ACROSS_USERS_FULL".equals(permName)) {
			return PackageManager.PERMISSION_DENIED;
		}
		return VirtualCore.get().getPackageManager().checkPermission(permName, VirtualCore.get().getHostPkg());
	}

	@Override
	public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
		synchronized (mPackages) {
			PackageParser.Package pkg = mPackages.get(packageName);
			if (pkg != null) {
				AppSetting setting = (AppSetting) pkg.mExtras;
				if ((flags & PackageManager.GET_SIGNATURES) != 0 && pkg.mSignatures == null) {
					if (pkg.mAppMetaData != null && pkg.mAppMetaData.containsKey(Constants.FEATURE_FAKE_SIGNATURE)) {
						String sig = pkg.mAppMetaData.getString("fake-signature");
						if (sig != null) {
							pkg.mSignatures = new Signature[] {new Signature(sig)};
						}
					} else {
						PackageParserCompat.collectCertificates(setting.parser, pkg, PackageParser.PARSE_IS_SYSTEM);
					}
				}
				PackageInfo packageInfo = PackageParserCompat.generatePackageInfo(pkg, flags,
						getFirstInstallTime(pkg), getLastInstallTime(pkg));
				if (packageInfo != null) {
					ComponentFixer.fixApplicationInfo(setting, packageInfo.applicationInfo, userId);
					return packageInfo;
				}
			}
		}
		return null;
	}

	private long getLastInstallTime(PackageParser.Package p) {
		AppSetting setting = (AppSetting) p.mExtras;
		return new File(setting.apkPath).lastModified();
	}

	private long getFirstInstallTime(PackageParser.Package p) {
		AppSetting setting = (AppSetting) p.mExtras;
		return new File(setting.apkPath).lastModified();
	}

	public void checkUserId(int userId) {
		if (!VUserManagerService.get().exists(userId)) {
			throw new SecurityException("The userId: " + userId + " is not exist.");
		}
	}

	@Override
	public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
		checkUserId(userId);
		synchronized (mPackages) {
			PackageParser.Activity a = mActivities.mActivities.get(component);
			if (a != null) {
				ActivityInfo activityInfo = PackageParserCompat.generateActivityInfo(a, flags);
				PackageParser.Package p = mPackages.get(activityInfo.packageName);
				AppSetting settings = (AppSetting) p.mExtras;
				ComponentFixer.fixComponentInfo(settings, activityInfo, userId);
				return activityInfo;
			}
		}
		return null;
	}

	@Override
	public boolean activitySupportsIntent(ComponentName component, Intent intent, String resolvedType) {
		synchronized (mPackages) {
			PackageParser.Activity a = mActivities.mActivities.get(component);
			if (a == null) {
				return false;
			}
			for (int i = 0; i < a.intents.size(); i++) {
				if (a.intents.get(i).match(intent.getAction(), resolvedType, intent.getScheme(), intent.getData(),
						intent.getCategories(), TAG) >= 0) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public ActivityInfo getReceiverInfo(ComponentName component, int flags, int userId) {
		checkUserId(userId);
		synchronized (mPackages) {
			PackageParser.Activity a = mReceivers.mActivities.get(component);
			if (a != null) {
				ActivityInfo receiverInfo = PackageParserCompat.generateActivityInfo(a, flags);
				PackageParser.Package p = mPackages.get(receiverInfo.packageName);
				AppSetting settings = (AppSetting) p.mExtras;
				ComponentFixer.fixComponentInfo(settings, receiverInfo, userId);
				return receiverInfo;
			}
		}
		return null;
	}

	@Override
	public ServiceInfo getServiceInfo(ComponentName component, int flags, int userId) {
		checkUserId(userId);
		synchronized (mPackages) {
			PackageParser.Service s = mServices.mServices.get(component);
			if (s != null) {
				ServiceInfo serviceInfo = PackageParserCompat.generateServiceInfo(s, flags);
				PackageParser.Package p = mPackages.get(serviceInfo.packageName);
				AppSetting settings = (AppSetting) p.mExtras;
				ComponentFixer.fixComponentInfo(settings, serviceInfo, userId);
				return serviceInfo;
			}
		}
		return null;
	}

	@Override
	public ProviderInfo getProviderInfo(ComponentName component, int flags, int userId) {
		checkUserId(userId);
		synchronized (mPackages) {
			PackageParser.Provider p = mProvidersByComponent.get(component);
			if (p != null) {
				ProviderInfo providerInfo = PackageParserCompat.generateProviderInfo(p, flags);
				PackageParser.Package pkg = mPackages.get(providerInfo.packageName);
				AppSetting settings = (AppSetting) pkg.mExtras;
				ComponentFixer.fixComponentInfo(settings, providerInfo, userId);
				return providerInfo;
			}
		}
		return null;
	}

	@Override
	public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
		checkUserId(userId);
		List<ResolveInfo> query = queryIntentActivities(intent, resolvedType, flags, 0);
		return chooseBestActivity(intent, resolvedType, flags, query);
	}

	private ResolveInfo chooseBestActivity(Intent intent, String resolvedType, int flags, List<ResolveInfo> query) {
		if (query != null) {
			final int N = query.size();
			if (N == 1) {
				return query.get(0);
			} else if (N > 1) {
				// If there is more than one activity with the same priority,
				// then let the user decide between them.
				ResolveInfo r0 = query.get(0);
				ResolveInfo r1 = query.get(1);
				// If the first activity has a higher priority, or a different
				// default, then it is always desireable to pick it.
				if (r0.priority != r1.priority || r0.preferredOrder != r1.preferredOrder
						|| r0.isDefault != r1.isDefault) {
					return query.get(0);
				}
				return query.get(0);
				// If we have saved a preference for a preferred activity for
				// this Intent, use that.

				// TODO
				// ResolveInfo ri = findPreferredActivity(intent, resolvedType,
				// flags, query, r0.priority);
				// if (ri != null) {
				// return ri;
				// }
				// return mResolveInfo;
			}
		}
		return null;
	}

	@Override
	public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
		checkUserId(userId);
		ComponentName comp = intent.getComponent();
		if (comp == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				if (intent.getSelector() != null) {
					intent = intent.getSelector();
					comp = intent.getComponent();
				}
			}
		}
		if (comp != null) {
			final List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
			final ActivityInfo ai = getActivityInfo(comp, flags, userId);
			if (ai != null) {
				final ResolveInfo ri = new ResolveInfo();
				ri.activityInfo = ai;
				list.add(ri);
			}
			return list;
		}

		// reader
		synchronized (mPackages) {
			final String pkgName = intent.getPackage();
			if (pkgName == null) {
				return mActivities.queryIntent(intent, resolvedType, flags);
			}
			final PackageParser.Package pkg = mPackages.get(pkgName);
			if (pkg != null) {
				return mActivities.queryIntentForPackage(intent, resolvedType, flags, pkg.activities);
			}
			return new ArrayList<ResolveInfo>();
		}
	}

	@Override
	public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) {
		ComponentName comp = intent.getComponent();
		if (comp == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				if (intent.getSelector() != null) {
					intent = intent.getSelector();
					comp = intent.getComponent();
				}
			}
		}
		if (comp != null) {
			List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
			ActivityInfo ai = getReceiverInfo(comp, flags, userId);
			if (ai != null) {
				ResolveInfo ri = new ResolveInfo();
				ri.activityInfo = ai;
				list.add(ri);
			}
			return list;
		}

		// reader
		synchronized (mPackages) {
			String pkgName = intent.getPackage();
			if (pkgName == null) {
				return mReceivers.queryIntent(intent, resolvedType, flags);
			}
			final PackageParser.Package pkg = mPackages.get(pkgName);
			if (pkg != null) {
				return mReceivers.queryIntentForPackage(intent, resolvedType, flags, pkg.receivers);
			}
			return null;
		}
	}

	@Override
	public List<ReceiverInfo> queryReceivers(String processName, int uid, int flags) {
		int userId = VUserHandle.getUserId(uid);
		checkUserId(userId);
		ArrayList<ReceiverInfo> finalList = new ArrayList<>(3);
		synchronized (mPackages) {
			for (PackageParser.Activity a : mReceivers.mActivities.values()) {
				if (a.info.processName.equals(processName)) {
					ActivityInfo receiverInfo = PackageParserCompat.generateActivityInfo(a, flags);
					if (receiverInfo != null) {
						AppSetting settings = (AppSetting) mPackages.get(receiverInfo.packageName).mExtras;
						ComponentFixer.fixComponentInfo(settings, receiverInfo, userId);
						ComponentName component = ComponentUtils.toComponentName(receiverInfo);
						IntentFilter[] filters = null;
						if (a.intents != null) {
							filters = a.intents.toArray(new IntentFilter[a.intents.size()]);
						}
						finalList.add(new ReceiverInfo(component, filters, receiverInfo.permission));
					}
				}
			}
		}
		return finalList;
	}

	@Override
	public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) {
		List<ResolveInfo> query = queryIntentServices(intent, resolvedType, flags, userId);
		if (query != null) {
			if (query.size() >= 1) {
				// If there is more than one service with the same priority,
				// just arbitrarily pick the first one.
				return query.get(0);
			}
		}
		return null;
	}

	@Override
	public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags, int userId) {
		checkUserId(userId);
		ComponentName comp = intent.getComponent();
		if (comp == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				if (intent.getSelector() != null) {
					intent = intent.getSelector();
					comp = intent.getComponent();
				}
			}
		}
		if (comp != null) {
			final List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
			final ServiceInfo si = getServiceInfo(comp, flags, userId);
			if (si != null) {
				final ResolveInfo ri = new ResolveInfo();
				ri.serviceInfo = si;
				list.add(ri);
			}
			return list;
		}

		// reader
		synchronized (mPackages) {
			String pkgName = intent.getPackage();
			if (pkgName == null) {
				return mServices.queryIntent(intent, resolvedType, flags);
			}
			final PackageParser.Package pkg = mPackages.get(pkgName);
			if (pkg != null) {
				return mServices.queryIntentForPackage(intent, resolvedType, flags, pkg.services);
			}
			return null;
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) {
		checkUserId(userId);
		ComponentName comp = intent.getComponent();
		if (comp == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				if (intent.getSelector() != null) {
					intent = intent.getSelector();
					comp = intent.getComponent();
				}
			}
		}
		if (comp != null) {
			final List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
			final ProviderInfo pi = getProviderInfo(comp, flags, userId);
			if (pi != null) {
				final ResolveInfo ri = new ResolveInfo();
				ri.providerInfo = pi;
				list.add(ri);
			}
			return list;
		}
		// reader
		synchronized (mPackages) {
			String pkgName = intent.getPackage();
			if (pkgName == null) {
				return mProviders.queryIntent(intent, resolvedType, flags);
			}
			final PackageParser.Package pkg = mPackages.get(pkgName);
			if (pkg != null) {
				return mProviders.queryIntentForPackage(intent, resolvedType, flags, pkg.providers);
			}
			return null;
		}
	}

	@Override
	public VParceledListSlice<ProviderInfo> queryContentProviders(String processName, int vuid, int flags) {
		int userId = VUserHandle.getUserId(vuid);
		checkUserId(userId);
		ArrayList<ProviderInfo> finalList = new ArrayList<>(3);
		// reader
		synchronized (mPackages) {
			for (PackageParser.Provider p : mProvidersByComponent.values()) {
				AppSetting setting = (AppSetting) p.owner.mExtras;
				if (processName == null || setting.appId == VUserHandle.getAppId(vuid) && p.info.processName.equals(processName)) {
					ProviderInfo providerInfo = PackageParserCompat.generateProviderInfo(p, flags);
					ComponentFixer.fixApplicationInfo(setting, providerInfo.applicationInfo, userId);
					finalList.add(providerInfo);
				}
			}
		}
		if (!finalList.isEmpty()) {
			Collections.sort(finalList, mProviderInitOrderSorter);
		}
		return new VParceledListSlice<>(finalList);
	}

	@Override
	public VParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) {
		checkUserId(userId);
		ArrayList<PackageInfo> pkgList = new ArrayList<>(mPackages.size());
		synchronized (mPackages) {
			for (PackageParser.Package pkg : mPackages.values()) {
				String packageName = pkg.packageName;
				pkgList.add(getPackageInfo(packageName, flags, userId));
			}
		}
		return new VParceledListSlice<>(pkgList);
	}

	@Override
	public VParceledListSlice<ApplicationInfo> getInstalledApplications(int flags, int userId) {
		checkUserId(userId);
		ArrayList<ApplicationInfo> list = new ArrayList<>(mPackages.size());
		synchronized (mPackages) {
			for (PackageParser.Package pkg : mPackages.values()) {
				list.add(getApplicationInfo(pkg.packageName, flags, userId));
			}
		}
		return new VParceledListSlice<>(list);
	}

	@Override
	public PermissionInfo getPermissionInfo(String name, int flags) {
		synchronized (mPackages) {
			PackageParser.Permission p = mPermissions.get(name);
			if (p != null) {
				return new PermissionInfo(p.info);
			}
		}
		return null;
	}

	@Override
	public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) {
		synchronized (mPackages) {
			return null;
		}
	}

	@Override
	public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) {
		synchronized (mPackages) {
			PackageParser.PermissionGroup p = mPermissionGroups.get(name);
			if (p != null) {
				return new PermissionGroupInfo(p.info);
			}
		}
		return null;
	}

	@Override
	public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
		synchronized (mPackages) {
			final int N = mPermissionGroups.size();
			ArrayList<PermissionGroupInfo> out = new ArrayList<>(N);
			for (PackageParser.PermissionGroup pg : mPermissionGroups.values()) {
				out.add(new PermissionGroupInfo(pg.info));
			}
			return out;
		}
	}

	@Override
	public ProviderInfo resolveContentProvider(String name, int flags, int userId) {
		checkUserId(userId);
		synchronized (mPackages) {
			final PackageParser.Provider provider = mProvidersByAuthority.get(name);
			if (provider != null) {
				ProviderInfo providerInfo = PackageParserCompat.generateProviderInfo(provider, flags);
				PackageParser.Package p = mPackages.get(providerInfo.packageName);
				AppSetting settings = (AppSetting) p.mExtras;
				ComponentFixer.fixComponentInfo(settings, providerInfo, userId);
				return providerInfo;
			}
		}
		return null;
	}

	@Override
	public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
		checkUserId(userId);
		synchronized (mPackages) {
			PackageParser.Package pkg = mPackages.get(packageName);
			if (pkg != null) {
				ApplicationInfo applicationInfo = PackageParserCompat.generateApplicationInfo(pkg, flags);
				AppSetting settings = (AppSetting) pkg.mExtras;
				ComponentFixer.fixApplicationInfo(settings, applicationInfo, userId);
				return applicationInfo;
			}
		}
		return null;
	}

	@Override
	public String[] getPackagesForUid(int uid) {
		int userId = VUserHandle.getUserId(uid);
		checkUserId(userId);
		synchronized (this) {
			List<String> pkgList = new ArrayList<>(2);
			for (PackageParser.Package p : mPackages.values()) {
				AppSetting settings = (AppSetting) p.mExtras;
				if (VUserHandle.getUid(userId, settings.appId) == uid) {
					pkgList.add(p.packageName);
				}
			}
			return pkgList.toArray(new String[pkgList.size()]);
		}
	}

	@Override
	public int getPackageUid(String packageName, int userId) {
		checkUserId(userId);
		synchronized (mPackages) {
			PackageParser.Package p = mPackages.get(packageName);
			if (p != null) {
				AppSetting settings = (AppSetting) p.mExtras;
				return VUserHandle.getUid(userId, settings.appId);
			}
			return -1;
		}
	}


	@Override
	public List<String> querySharedPackages(String packageName) {
		synchronized (mPackages) {
			PackageParser.Package p = mPackages.get(packageName);
			if (p == null || p.mSharedUserId == null) {
				// noinspection unchecked
				return Collections.EMPTY_LIST;
			}
			ArrayList<String> list = new ArrayList<>();
			for (PackageParser.Package one : mPackages.values()) {
				if (TextUtils.equals(one.mSharedUserId, p.mSharedUserId)) {
					list.add(one.packageName);
				}
			}
			return list;
		}
	}
	@Override
	public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
		try {
			return super.onTransact(code, data, reply, flags);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void createNewUserLILPw(int userId, File userPath) {

	}

	public void cleanUpUserLILPw(int userHandle) {

	}

	final class ActivityIntentResolver extends IntentResolver<PackageParser.ActivityIntentInfo, ResolveInfo> {
		// Keys are String (activity class name), values are Activity.
		private final HashMap<ComponentName, PackageParser.Activity> mActivities = new HashMap<>();
		private int mFlags;

		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly) {
			mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
			return super.queryIntent(intent, resolvedType, defaultOnly);
		}

		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags) {
			mFlags = flags;
			return super.queryIntent(intent, resolvedType, (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0);
		}

		public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags,
				ArrayList<PackageParser.Activity> packageActivities) {
			if (packageActivities == null) {
				return null;
			}
			mFlags = flags;
			final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
			final int N = packageActivities.size();
			ArrayList<PackageParser.ActivityIntentInfo[]> listCut = new ArrayList<PackageParser.ActivityIntentInfo[]>(
					N);

			ArrayList<PackageParser.ActivityIntentInfo> intentFilters;
			for (int i = 0; i < N; ++i) {
				intentFilters = packageActivities.get(i).intents;
				if (intentFilters != null && intentFilters.size() > 0) {
					PackageParser.ActivityIntentInfo[] array = new PackageParser.ActivityIntentInfo[intentFilters
							.size()];
					intentFilters.toArray(array);
					listCut.add(array);
				}
			}
			return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut);
		}

		public final void addActivity(PackageParser.Activity a, String type) {
			final boolean systemApp = isSystemApp(a.info.applicationInfo);
			mActivities.put(a.getComponentName(), a);
			if (DEBUG_SHOW_INFO)
				Log.v(TAG, "  " + type + " "
						+ (a.info.nonLocalizedLabel != null ? a.info.nonLocalizedLabel : a.info.name) + ":");
			if (DEBUG_SHOW_INFO)
				Log.v(TAG, "    Class=" + a.info.name);
			final int NI = a.intents.size();
			for (int j = 0; j < NI; j++) {
				PackageParser.ActivityIntentInfo intent = a.intents.get(j);
				if (!systemApp && intent.getPriority() > 0 && "activity".equals(type)) {
					intent.setPriority(0);
					Log.w(TAG, "Package " + a.info.applicationInfo.packageName + " has activity " + a.className
							+ " with priority > 0, forcing to 0");
				}
				if (DEBUG_SHOW_INFO) {
					Log.v(TAG, "    IntentFilter:");
					intent.dump(new LogPrinter(Log.VERBOSE, TAG), "      ");
				}
				addFilter(intent);
			}
		}

		public final void removeActivity(PackageParser.Activity a, String type) {
			mActivities.remove(a.getComponentName());
			if (DEBUG_SHOW_INFO) {
				Log.v(TAG, "  " + type + " "
						+ (a.info.nonLocalizedLabel != null ? a.info.nonLocalizedLabel : a.info.name) + ":");
				Log.v(TAG, "    Class=" + a.info.name);
			}
			final int NI = a.intents.size();
			for (int j = 0; j < NI; j++) {
				PackageParser.ActivityIntentInfo intent = a.intents.get(j);
				if (DEBUG_SHOW_INFO) {
					Log.v(TAG, "    IntentFilter:");
					intent.dump(new LogPrinter(Log.VERBOSE, TAG), "      ");
				}
				removeFilter(intent);
			}
		}

		@Override
		protected boolean allowFilterResult(PackageParser.ActivityIntentInfo filter, List<ResolveInfo> dest) {
			ActivityInfo filterAi = filter.activity.info;
			for (int i = dest.size() - 1; i >= 0; i--) {
				ActivityInfo destAi = dest.get(i).activityInfo;
				if (destAi.name == filterAi.name && destAi.packageName == filterAi.packageName) {
					return false;
				}
			}
			return true;
		}

		@Override
		protected PackageParser.ActivityIntentInfo[] newArray(int size) {
			return new PackageParser.ActivityIntentInfo[size];
		}

		@Override
		protected boolean isFilterStopped(PackageParser.ActivityIntentInfo filter) {
			return false;
		}

		@Override
		protected boolean isPackageForFilter(String packageName, PackageParser.ActivityIntentInfo info) {
			return packageName.equals(info.activity.owner.packageName);
		}

		@Override
		protected ResolveInfo newResult(PackageParser.ActivityIntentInfo info, int match) {
			final PackageParser.Activity activity = info.activity;
			ActivityInfo ai = PackageParserCompat.generateActivityInfo(activity, mFlags);
			if (ai == null) {
				return null;
			}
			final ResolveInfo res = new ResolveInfo();
			res.activityInfo = ai;
			if ((mFlags & PackageManager.GET_RESOLVED_FILTER) != 0) {
				res.filter = info;
			}
			res.priority = info.getPriority();
			res.preferredOrder = activity.owner.mPreferredOrder;
			// System.out.println("Result: " + res.activityInfo.className +
			// " = " + res.priority);
			res.match = match;
			res.isDefault = info.hasDefault;
			res.labelRes = info.labelRes;
			res.nonLocalizedLabel = info.nonLocalizedLabel;
			res.icon = info.icon;
			return res;
		}

		@Override
		protected void sortResults(List<ResolveInfo> results) {
			Collections.sort(results, mResolvePrioritySorter);
		}

		@Override
		protected void dumpFilter(PrintWriter out, String prefix, PackageParser.ActivityIntentInfo filter) {

		}

		@Override
		protected Object filterToLabel(PackageParser.ActivityIntentInfo filter) {
			return filter.activity;
		}

		protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {

		}
	}

	private final class ServiceIntentResolver extends IntentResolver<PackageParser.ServiceIntentInfo, ResolveInfo> {
		// Keys are String (activity class name), values are Activity.
		private final HashMap<ComponentName, PackageParser.Service> mServices = new HashMap<>();
		private int mFlags;

		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly) {
			mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
			return super.queryIntent(intent, resolvedType, defaultOnly);
		}

		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags) {
			mFlags = flags;
			return super.queryIntent(intent, resolvedType, (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0);
		}

		public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags,
				ArrayList<PackageParser.Service> packageServices) {
			if (packageServices == null) {
				return null;
			}
			mFlags = flags;
			final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
			final int N = packageServices.size();
			ArrayList<PackageParser.ServiceIntentInfo[]> listCut = new ArrayList<PackageParser.ServiceIntentInfo[]>(N);

			ArrayList<PackageParser.ServiceIntentInfo> intentFilters;
			for (int i = 0; i < N; ++i) {
				intentFilters = packageServices.get(i).intents;
				if (intentFilters != null && intentFilters.size() > 0) {
					PackageParser.ServiceIntentInfo[] array = new PackageParser.ServiceIntentInfo[intentFilters.size()];
					intentFilters.toArray(array);
					listCut.add(array);
				}
			}
			return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut);
		}

		public final void addService(PackageParser.Service s) {
			mServices.put(s.getComponentName(), s);
			if (DEBUG_SHOW_INFO) {
				Log.v(TAG, "  " + (s.info.nonLocalizedLabel != null ? s.info.nonLocalizedLabel : s.info.name) + ":");
				Log.v(TAG, "    Class=" + s.info.name);
			}
			final int NI = s.intents.size();
			int j;
			for (j = 0; j < NI; j++) {
				PackageParser.ServiceIntentInfo intent = s.intents.get(j);
				addFilter(intent);
			}
		}

		public final void removeService(PackageParser.Service s) {
			mServices.remove(s.getComponentName());
			if (DEBUG_SHOW_INFO) {
				Log.v(TAG, "  " + (s.info.nonLocalizedLabel != null ? s.info.nonLocalizedLabel : s.info.name) + ":");
				Log.v(TAG, "    Class=" + s.info.name);
			}
			final int NI = s.intents.size();
			int j;
			for (j = 0; j < NI; j++) {
				PackageParser.ServiceIntentInfo intent = s.intents.get(j);
				removeFilter(intent);
			}
		}

		@Override
		protected boolean allowFilterResult(PackageParser.ServiceIntentInfo filter, List<ResolveInfo> dest) {
			ServiceInfo filterSi = filter.service.info;
			for (int i = dest.size() - 1; i >= 0; i--) {
				ServiceInfo destAi = dest.get(i).serviceInfo;
				if (ObjectsCompat.equals(destAi.name, filterSi.name)
						&& ObjectsCompat.equals(destAi.packageName, filterSi.packageName)) {
					return false;
				}
			}
			return true;
		}

		@Override
		protected PackageParser.ServiceIntentInfo[] newArray(int size) {
			return new PackageParser.ServiceIntentInfo[size];
		}

		@Override
		protected boolean isFilterStopped(PackageParser.ServiceIntentInfo filter) {
			return false;
		}

		@Override
		protected boolean isPackageForFilter(String packageName, PackageParser.ServiceIntentInfo info) {
			return packageName.equals(info.service.owner.packageName);
		}

		@Override
		protected ResolveInfo newResult(PackageParser.ServiceIntentInfo filter, int match) {
			final PackageParser.Service service = filter.service;
			ServiceInfo si = PackageParserCompat.generateServiceInfo(service, mFlags);
			if (si == null) {
				return null;
			}
			final ResolveInfo res = new ResolveInfo();
			res.serviceInfo = si;
			if ((mFlags & PackageManager.GET_RESOLVED_FILTER) != 0) {
				res.filter = filter;
			}
			res.priority = filter.getPriority();
			res.preferredOrder = service.owner.mPreferredOrder;
			res.match = match;
			res.isDefault = filter.hasDefault;
			res.labelRes = filter.labelRes;
			res.nonLocalizedLabel = filter.nonLocalizedLabel;
			res.icon = filter.icon;
			return res;
		}

		@Override
		protected void sortResults(List<ResolveInfo> results) {
			Collections.sort(results, mResolvePrioritySorter);
		}

		@Override
		protected void dumpFilter(PrintWriter out, String prefix, PackageParser.ServiceIntentInfo filter) {

		}

		@Override
		protected Object filterToLabel(PackageParser.ServiceIntentInfo filter) {
			return filter.service;
		}

		protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {

		}
	}

}
