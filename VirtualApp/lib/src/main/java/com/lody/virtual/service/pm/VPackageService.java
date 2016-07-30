package com.lody.virtual.service.pm;

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
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.util.LogPrinter;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.fixer.ComponentFixer;
import com.lody.virtual.helper.compat.ObjectsCompat;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.proto.VParceledListSlice;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.IPackageManager;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lody
 *
 */
public class VPackageService extends IPackageManager.Stub {

	static final String TAG = "PackageManager";

	private static final boolean DEBUG_SHOW_INFO = false;
	private static final VPackageService gService = new VPackageService();
	private static final Comparator<ResolveInfo> mResolvePrioritySorter = new Comparator<ResolveInfo>() {
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
			if (r1.system != r2.system) {
				return r1.system ? -1 : 1;
			}
			return 0;
		}
	};
	final ActivityIntentResolver mActivities = new ActivityIntentResolver();
	final ServiceIntentResolver mServices = new ServiceIntentResolver();
	final ActivityIntentResolver mReceivers = new ActivityIntentResolver();
	final ProviderIntentResolver mProviders = new ProviderIntentResolver();

	final HashMap<ComponentName, PackageParser.Provider> mProvidersByComponent = new HashMap<>();

	final HashMap<String, PackageParser.Permission> mPermissions = new HashMap<>();
	final HashMap<String, PackageParser.PermissionGroup> mPermissionGroups = new HashMap<>();
	final HashMap<String, PackageParser.Provider> mProvidersByAuthority = new HashMap<>();

	private final Map<String, PackageParser.Package> mPackages = PackageCache.sPackageCaches;
	private final Map<String, AppInfo> mAppInfos = PackageCache.sAppInfos;

	private int[] mGids;

	public static VPackageService getService() {
		return gService;
	}


	private static boolean isSystemApp(ApplicationInfo info) {
		return info != null && (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
	}

	private Context getContext() {
		return VirtualCore.getCore().getContext();
	}

	private VAppService getPMS() {
		return VAppService.getService();
	}

	public void onCreate(Context context) {
		this.mGids = VirtualCore.getCore().getGids();
	}

	public void analyzePackageLocked(AppInfo appInfo, PackageParser.Package pkg) {
		int N = pkg.activities.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Activity a = pkg.activities.get(i);
			ComponentFixer.fixComponentInfo(appInfo, a.info);
			mActivities.addActivity(a, "activity");
		}
		N = pkg.services.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Service a = pkg.services.get(i);
			ComponentFixer.fixComponentInfo(appInfo, a.info);
			mServices.addService(a);
		}
		N = pkg.receivers.size();
		for (int i = 0; i < N; i++) {
			PackageParser.Activity a = pkg.receivers.get(i);
			ComponentFixer.fixComponentInfo(appInfo, a.info);
			mReceivers.addActivity(a, "receiver");
		}

		N = pkg.providers.size();
		for (int i=0; i<N; i++) {
			PackageParser.Provider p = pkg.providers.get(i);
			ComponentFixer.fixComponentInfo(appInfo, p.info);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mProviders.addProvider(p);
			}
			String names[] = p.info.authority.split(";");
			for (String name : names) {
				if (!mProvidersByAuthority.containsKey(name)) {
					mProvidersByAuthority.put(name, p);
				}
			}
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
			mActivities.addActivity(a, "activity");
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
		for (int i=0; i<N; i++) {
			PackageParser.Provider p = pkg.providers.get(i);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mProviders.removeProvider(p);
			}
			String names[] = p.info.authority.split(";");
			for (String name : names) {
				if (!mProvidersByAuthority.containsKey(name)) {
					mProvidersByAuthority.remove(name);
				}
			}
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
	public int checkPermission(String permName, String pkgName) {
		return PackageManager.PERMISSION_GRANTED;
	}

	@Override
	public PackageInfo getPackageInfo(String packageName, int flags) {
		synchronized (mPackages) {
			PackageParser.Package pkg = mPackages.get(packageName);
			if (pkg != null) {
				if ((flags & PackageManager.GET_SIGNATURES) != 0 && pkg.mSignatures == null) {
					PackageParser.collectCertificates(pkg, PackageParser.PARSE_IS_SYSTEM);
				}
				PackageInfo packageInfo = PackageParser.generatePackageInfo(pkg, mGids, flags,
						getFirstInstallTime(packageName), getLastInstallTime(packageName));
				if (packageInfo != null) {
					ComponentFixer.fixApplicationInfo(mAppInfos.get(packageName), packageInfo.applicationInfo);
					return packageInfo;
				}
			}
		}
		return null;
	}

	private long getLastInstallTime(String packageName) {
		return System.currentTimeMillis();
	}

	private long getFirstInstallTime(String packageName) {
		return System.currentTimeMillis();
	}

	@Override
	public ActivityInfo getActivityInfo(ComponentName component, int flags) {
		synchronized (mPackages) {
			PackageParser.Activity a = mActivities.mActivities.get(component);
			if (a != null) {
				ActivityInfo activityInfo = PackageParser.generateActivityInfo(a, flags);
				ComponentFixer.fixComponentInfo(mAppInfos.get(activityInfo.packageName), activityInfo);
				return activityInfo;
			}
		}
		return null;
	}

	@Override
	public boolean activitySupportsIntent(ComponentName component, Intent intent,
										  String resolvedType) {
		synchronized (mPackages) {
			PackageParser.Activity a = mActivities.mActivities.get(component);
			if (a == null) {
				return false;
			}
			for (int i=0; i<a.intents.size(); i++) {
				if (a.intents.get(i).match(intent.getAction(), resolvedType, intent.getScheme(),
						intent.getData(), intent.getCategories(), TAG) >= 0) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public ActivityInfo getReceiverInfo(ComponentName component, int flags) {
		synchronized (mPackages) {
			PackageParser.Activity a = mReceivers.mActivities.get(component);
			if (a != null) {
				ActivityInfo receiverInfo = PackageParser.generateActivityInfo(a, flags);
				ComponentFixer.fixComponentInfo(mAppInfos.get(receiverInfo.packageName), receiverInfo);
				return receiverInfo;
			}
		}
		return null;
	}

	@Override
	public ServiceInfo getServiceInfo(ComponentName component, int flags) {
		synchronized (mPackages) {
			PackageParser.Service s = mServices.mServices.get(component);
			if (s != null) {
				ServiceInfo serviceInfo = PackageParser.generateServiceInfo(s, flags);
				ComponentFixer.fixComponentInfo(mAppInfos.get(serviceInfo.packageName), serviceInfo);
				return serviceInfo;
			}
		}
		return null;
	}

	@Override
	public ProviderInfo getProviderInfo(ComponentName component, int flags) {
		synchronized (mPackages) {
			PackageParser.Provider p = mProvidersByComponent.get(component);
			if (p != null) {
				ProviderInfo providerInfo = PackageParser.generateProviderInfo(p, flags);
				ComponentFixer.fixComponentInfo(mAppInfos.get(providerInfo.packageName), providerInfo);
				return providerInfo;
			}
		}
		return null;
	}

	@Override
	public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags) {
		List<ResolveInfo> query = queryIntentActivities(intent, resolvedType, flags);
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
	public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags) {
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
			final ActivityInfo ai = getActivityInfo(comp, flags);
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
	public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) {
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
			ActivityInfo ai = getReceiverInfo(comp, flags);
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
	public ResolveInfo resolveService(Intent intent, String resolvedType, int flags) {
		List<ResolveInfo> query = queryIntentServices(intent, resolvedType, flags);
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
	public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) {
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
			final ServiceInfo si = getServiceInfo(comp, flags);
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
	public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) {
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
			final ProviderInfo pi = getProviderInfo(comp, flags);
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
				return mProviders.queryIntentForPackage(
						intent, resolvedType, flags, pkg.providers);
			}
			return null;
		}
	}

	@Override
	public VParceledListSlice<PackageInfo> getInstalledPackages(int flags) {
		ArrayList<PackageInfo> pkgList = new ArrayList<>(mPackages.size());
		synchronized (mPackages) {
			for (PackageParser.Package pkg : mPackages.values()) {
				String packageName = pkg.packageName;
				pkgList.add(getPackageInfo(packageName, flags));
			}
		}
		return new VParceledListSlice<>(pkgList);
	}

	@Override
	public VParceledListSlice<ApplicationInfo> getInstalledApplications(int flags) {
		ArrayList<ApplicationInfo> list = new ArrayList<>(mPackages.size());
		synchronized (mPackages) {
			for (PackageParser.Package pkg : mPackages.values()) {
				list.add(getApplicationInfo(pkg.packageName, flags));
			}
		}
		return new VParceledListSlice<>(list);
	}

	@Override
	public PermissionInfo getPermissionInfo(String name, int flags) {
		synchronized (mPackages) {
			PackageParser.Permission p = mPermissions.get(name);
			if (p != null) {
				return PackageParser.generatePermissionInfo(p, flags);
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
				return PackageParser.generatePermissionGroupInfo(p, flags);
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
				out.add(PackageParser.generatePermissionGroupInfo(pg, flags));
			}
			return out;
		}
	}

	@Override
	public ProviderInfo resolveContentProvider(String name, int flags) {
		synchronized (mPackages) {
			final PackageParser.Provider provider = mProvidersByAuthority.get(name);
			if (provider != null) {
				ProviderInfo providerInfo = PackageParser.generateProviderInfo(provider, flags);
				ComponentFixer.fixComponentInfo(mAppInfos.get(providerInfo.packageName), providerInfo);
				return providerInfo;
			}
		}
		return null;
	}

	@Override
	public ApplicationInfo getApplicationInfo(String packageName, int flags) {
		synchronized (mPackages) {
			PackageParser.Package pkg = mPackages.get(packageName);
			if (pkg != null) {
				ApplicationInfo applicationInfo = PackageParser.generateApplicationInfo(pkg, flags);
				ComponentFixer.fixApplicationInfo(mAppInfos.get(applicationInfo.packageName), applicationInfo);
				return applicationInfo;
			}
		}
		return null;
	}

	@Override
	public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) {
		synchronized (mPackages) {
			ComponentName component = new ComponentName(info.packageName, info.name);
			PackageParser.Activity receiver = mReceivers.mActivities.get(component);
			if (receiver != null) {
				List<IntentFilter> filters = new ArrayList<>(receiver.intents.size());
				for (PackageParser.ActivityIntentInfo filter : receiver.intents) {
					filters.add(filter);
				}
				return filters;
			}
		}
		return null;
	}

	final class ActivityIntentResolver
			extends IntentResolver<PackageParser.ActivityIntentInfo, ResolveInfo> {
		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType,
											 boolean defaultOnly) {
			mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
			return super.queryIntent(intent, resolvedType, defaultOnly);
		}

		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags) {
			mFlags = flags;
			return super.queryIntent(intent, resolvedType,
					(flags & PackageManager.MATCH_DEFAULT_ONLY) != 0);
		}

		public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType,
													   int flags, ArrayList<PackageParser.Activity> packageActivities) {
			if (packageActivities == null) {
				return null;
			}
			mFlags = flags;
			final boolean defaultOnly = (flags&PackageManager.MATCH_DEFAULT_ONLY) != 0;
			final int N = packageActivities.size();
			ArrayList<PackageParser.ActivityIntentInfo[]> listCut =
					new ArrayList<PackageParser.ActivityIntentInfo[]>(N);

			ArrayList<PackageParser.ActivityIntentInfo> intentFilters;
			for (int i = 0; i < N; ++i) {
				intentFilters = packageActivities.get(i).intents;
				if (intentFilters != null && intentFilters.size() > 0) {
					PackageParser.ActivityIntentInfo[] array =
							new PackageParser.ActivityIntentInfo[intentFilters.size()];
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
				Log.v(
						TAG, "  " + type + " " +
								(a.info.nonLocalizedLabel != null ? a.info.nonLocalizedLabel : a.info.name) + ":");
			if (DEBUG_SHOW_INFO)
				Log.v(TAG, "    Class=" + a.info.name);
			final int NI = a.intents.size();
			for (int j=0; j<NI; j++) {
				PackageParser.ActivityIntentInfo intent = a.intents.get(j);
				if (!systemApp && intent.getPriority() > 0 && "activity".equals(type)) {
					intent.setPriority(0);
					Log.w(TAG, "Package " + a.info.applicationInfo.packageName + " has activity "
							+ a.className + " with priority > 0, forcing to 0");
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
						+ (a.info.nonLocalizedLabel != null ? a.info.nonLocalizedLabel
						: a.info.name) + ":");
				Log.v(TAG, "    Class=" + a.info.name);
			}
			final int NI = a.intents.size();
			for (int j=0; j<NI; j++) {
				PackageParser.ActivityIntentInfo intent = a.intents.get(j);
				if (DEBUG_SHOW_INFO) {
					Log.v(TAG, "    IntentFilter:");
					intent.dump(new LogPrinter(Log.VERBOSE, TAG), "      ");
				}
				removeFilter(intent);
			}
		}

		@Override
		protected boolean allowFilterResult(
				PackageParser.ActivityIntentInfo filter, List<ResolveInfo> dest) {
			ActivityInfo filterAi = filter.activity.info;
			for (int i=dest.size()-1; i>=0; i--) {
				ActivityInfo destAi = dest.get(i).activityInfo;
				if (destAi.name == filterAi.name
						&& destAi.packageName == filterAi.packageName) {
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
		protected boolean isPackageForFilter(String packageName,
											 PackageParser.ActivityIntentInfo info) {
			return packageName.equals(info.activity.owner.packageName);
		}

		@Override
		protected ResolveInfo newResult(PackageParser.ActivityIntentInfo info,
										int match) {
			final PackageParser.Activity activity = info.activity;
			ActivityInfo ai = PackageParser.generateActivityInfo(activity, mFlags);
			if (ai == null) {
				return null;
			}
			final ResolveInfo res = new ResolveInfo();
			res.activityInfo = ai;
			if ((mFlags&PackageManager.GET_RESOLVED_FILTER) != 0) {
				res.filter = info;
			}
			res.priority = info.getPriority();
			res.preferredOrder = activity.owner.mPreferredOrder;
			//System.out.println("Result: " + res.activityInfo.className +
			//                   " = " + res.priority);
			res.match = match;
			res.isDefault = info.hasDefault;
			res.labelRes = info.labelRes;
			res.nonLocalizedLabel = info.nonLocalizedLabel;
			res.icon = info.icon;
			res.system = isSystemApp(res.activityInfo.applicationInfo);
			return res;
		}

		@Override
		protected void sortResults(List<ResolveInfo> results) {
			Collections.sort(results, mResolvePrioritySorter);
		}

		@Override
		protected void dumpFilter(PrintWriter out, String prefix,
								  PackageParser.ActivityIntentInfo filter) {
			out.print(prefix); out.print(
					Integer.toHexString(System.identityHashCode(filter.activity)));
			out.print(' ');
			filter.activity.printComponentShortName(out);
			out.print(" filter ");
			out.println(Integer.toHexString(System.identityHashCode(filter)));
		}

		@Override
		protected Object filterToLabel(PackageParser.ActivityIntentInfo filter) {
			return filter.activity;
		}

		protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
			PackageParser.Activity activity = (PackageParser.Activity)label;
			out.print(prefix); out.print(
					Integer.toHexString(System.identityHashCode(activity)));
			out.print(' ');
			activity.printComponentShortName(out);
			if (count > 1) {
				out.print(" ("); out.print(count); out.print(" filters)");
			}
			out.println();
		}

		// Keys are String (activity class name), values are Activity.
		private final HashMap<ComponentName, PackageParser.Activity> mActivities
				= new HashMap<>();
		private int mFlags;
	}


	private final class ServiceIntentResolver
			extends IntentResolver<PackageParser.ServiceIntentInfo, ResolveInfo> {
		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType,
											 boolean defaultOnly) {
			mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
			return super.queryIntent(intent, resolvedType, defaultOnly);
		}

		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags) {
			mFlags = flags;
			return super.queryIntent(intent, resolvedType,
					(flags & PackageManager.MATCH_DEFAULT_ONLY) != 0);
		}

		public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType,
													   int flags, ArrayList<PackageParser.Service> packageServices) {
			if (packageServices == null) {
				return null;
			}
			mFlags = flags;
			final boolean defaultOnly = (flags&PackageManager.MATCH_DEFAULT_ONLY) != 0;
			final int N = packageServices.size();
			ArrayList<PackageParser.ServiceIntentInfo[]> listCut =
					new ArrayList<PackageParser.ServiceIntentInfo[]>(N);

			ArrayList<PackageParser.ServiceIntentInfo> intentFilters;
			for (int i = 0; i < N; ++i) {
				intentFilters = packageServices.get(i).intents;
				if (intentFilters != null && intentFilters.size() > 0) {
					PackageParser.ServiceIntentInfo[] array =
							new PackageParser.ServiceIntentInfo[intentFilters.size()];
					intentFilters.toArray(array);
					listCut.add(array);
				}
			}
			return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut);
		}

		public final void addService(PackageParser.Service s) {
			mServices.put(s.getComponentName(), s);
			if (DEBUG_SHOW_INFO) {
				Log.v(TAG, "  "
						+ (s.info.nonLocalizedLabel != null
						? s.info.nonLocalizedLabel : s.info.name) + ":");
				Log.v(TAG, "    Class=" + s.info.name);
			}
			final int NI = s.intents.size();
			int j;
			for (j=0; j<NI; j++) {
				PackageParser.ServiceIntentInfo intent = s.intents.get(j);
				if (DEBUG_SHOW_INFO) {
					Log.v(TAG, "    IntentFilter:");
					intent.dump(new LogPrinter(Log.VERBOSE, TAG), "      ");
				}
				addFilter(intent);
			}
		}

		public final void removeService(PackageParser.Service s) {
			mServices.remove(s.getComponentName());
			if (DEBUG_SHOW_INFO) {
				Log.v(TAG, "  " + (s.info.nonLocalizedLabel != null
						? s.info.nonLocalizedLabel : s.info.name) + ":");
				Log.v(TAG, "    Class=" + s.info.name);
			}
			final int NI = s.intents.size();
			int j;
			for (j=0; j<NI; j++) {
				PackageParser.ServiceIntentInfo intent = s.intents.get(j);
				if (DEBUG_SHOW_INFO) {
					Log.v(TAG, "    IntentFilter:");
					intent.dump(new LogPrinter(Log.VERBOSE, TAG), "      ");
				}
				removeFilter(intent);
			}
		}

		@Override
		protected boolean allowFilterResult(
				PackageParser.ServiceIntentInfo filter, List<ResolveInfo> dest) {
			ServiceInfo filterSi = filter.service.info;
			for (int i=dest.size()-1; i>=0; i--) {
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
		protected boolean isPackageForFilter(String packageName,
											 PackageParser.ServiceIntentInfo info) {
			return packageName.equals(info.service.owner.packageName);
		}

		@Override
		protected ResolveInfo newResult(PackageParser.ServiceIntentInfo filter,
										int match) {
			final PackageParser.Service service = filter.service;
			ServiceInfo si = PackageParser.generateServiceInfo(service, mFlags);
			if (si == null) {
				return null;
			}
			final ResolveInfo res = new ResolveInfo();
			res.serviceInfo = si;
			if ((mFlags&PackageManager.GET_RESOLVED_FILTER) != 0) {
				res.filter = filter;
			}
			res.priority = filter.getPriority();
			res.preferredOrder = service.owner.mPreferredOrder;
			res.match = match;
			res.isDefault = filter.hasDefault;
			res.labelRes = filter.labelRes;
			res.nonLocalizedLabel = filter.nonLocalizedLabel;
			res.icon = filter.icon;
			res.system = isSystemApp(res.serviceInfo.applicationInfo);
			return res;
		}

		@Override
		protected void sortResults(List<ResolveInfo> results) {
			Collections.sort(results, mResolvePrioritySorter);
		}

		@Override
		protected void dumpFilter(PrintWriter out, String prefix,
								  PackageParser.ServiceIntentInfo filter) {
			out.print(prefix); out.print(
					Integer.toHexString(System.identityHashCode(filter.service)));
			out.print(' ');
			filter.service.printComponentShortName(out);
			out.print(" filter ");
			out.println(Integer.toHexString(System.identityHashCode(filter)));
		}

		@Override
		protected Object filterToLabel(PackageParser.ServiceIntentInfo filter) {
			return filter.service;
		}

		protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
			PackageParser.Service service = (PackageParser.Service)label;
			out.print(prefix); out.print(
					Integer.toHexString(System.identityHashCode(service)));
			out.print(' ');
			service.printComponentShortName(out);
			if (count > 1) {
				out.print(" ("); out.print(count); out.print(" filters)");
			}
			out.println();
		}

		// Keys are String (activity class name), values are Activity.
		private final HashMap<ComponentName, PackageParser.Service> mServices
				= new HashMap<>();
		private int mFlags;
	}



	private final class ProviderIntentResolver
			extends IntentResolver<PackageParser.ProviderIntentInfo, ResolveInfo> {
		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType,
											 boolean defaultOnly) {
			mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
			return super.queryIntent(intent, resolvedType, defaultOnly);
		}

		public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags) {
			mFlags = flags;
			return super.queryIntent(intent, resolvedType,
					(flags & PackageManager.MATCH_DEFAULT_ONLY) != 0);
		}

		public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType,
													   int flags, ArrayList<PackageParser.Provider> packageProviders) {
			if (packageProviders == null) {
				return null;
			}
			mFlags = flags;
			final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
			final int N = packageProviders.size();
			ArrayList<PackageParser.ProviderIntentInfo[]> listCut =
					new ArrayList<>(N);

			ArrayList<PackageParser.ProviderIntentInfo> intentFilters;
			for (int i = 0; i < N; ++i) {
				intentFilters = packageProviders.get(i).intents;
				if (intentFilters != null && intentFilters.size() > 0) {
					PackageParser.ProviderIntentInfo[] array =
							new PackageParser.ProviderIntentInfo[intentFilters.size()];
					intentFilters.toArray(array);
					listCut.add(array);
				}
			}
			return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut);
		}

		public final void addProvider(PackageParser.Provider p) {
			if (mProviders.containsKey(p.getComponentName())) {
				VLog.w(TAG, "Provider " + p.getComponentName() + " already defined; ignoring");
				return;
			}

			mProviders.put(p.getComponentName(), p);
			if (DEBUG_SHOW_INFO) {
				Log.v(TAG, "  "
						+ (p.info.nonLocalizedLabel != null
						? p.info.nonLocalizedLabel : p.info.name) + ":");
				Log.v(TAG, "    Class=" + p.info.name);
			}
			final int NI = p.intents.size();
			int j;
			for (j = 0; j < NI; j++) {
				PackageParser.ProviderIntentInfo intent = p.intents.get(j);
				if (DEBUG_SHOW_INFO) {
					Log.v(TAG, "    IntentFilter:");
					intent.dump(new LogPrinter(Log.VERBOSE, TAG), "      ");
				}
				addFilter(intent);
			}
		}

		public final void removeProvider(PackageParser.Provider p) {
			mProviders.remove(p.getComponentName());
			if (DEBUG_SHOW_INFO) {
				Log.v(TAG, "  " + (p.info.nonLocalizedLabel != null
						? p.info.nonLocalizedLabel : p.info.name) + ":");
				Log.v(TAG, "    Class=" + p.info.name);
			}
			final int NI = p.intents.size();
			int j;
			for (j = 0; j < NI; j++) {
				PackageParser.ProviderIntentInfo intent = p.intents.get(j);
				if (DEBUG_SHOW_INFO) {
					Log.v(TAG, "    IntentFilter:");
					intent.dump(new LogPrinter(Log.VERBOSE, TAG), "      ");
				}
				removeFilter(intent);
			}
		}

		@TargetApi(Build.VERSION_CODES.KITKAT)
		@Override
		protected boolean allowFilterResult(
				PackageParser.ProviderIntentInfo filter, List<ResolveInfo> dest) {
			ProviderInfo filterPi = filter.provider.info;
			for (int i = dest.size() - 1; i >= 0; i--) {
				ProviderInfo destPi = dest.get(i).providerInfo;
				if (ObjectsCompat.equals(destPi.name, filterPi.name)
						&& ObjectsCompat.equals(destPi.packageName, filterPi.packageName)) {
					return false;
				}
			}
			return true;
		}

		@Override
		protected PackageParser.ProviderIntentInfo[] newArray(int size) {
			return new PackageParser.ProviderIntentInfo[size];
		}

		@Override
		protected boolean isFilterStopped(PackageParser.ProviderIntentInfo filter) {
			return false;
		}

		@Override
		protected boolean isPackageForFilter(String packageName,
											 PackageParser.ProviderIntentInfo info) {
			return packageName.equals(info.provider.owner.packageName);
		}

		@TargetApi(Build.VERSION_CODES.KITKAT)
		@Override
		protected ResolveInfo newResult(PackageParser.ProviderIntentInfo filter,
										int match) {
			final PackageParser.Provider provider = filter.provider;
			ProviderInfo pi = PackageParser.generateProviderInfo(provider, mFlags);
			if (pi == null) {
				return null;
			}
			final ResolveInfo res = new ResolveInfo();
			res.providerInfo = pi;
			if ((mFlags & PackageManager.GET_RESOLVED_FILTER) != 0) {
				res.filter = filter;
			}
			res.priority = filter.getPriority();
			res.preferredOrder = provider.owner.mPreferredOrder;
			res.match = match;
			res.isDefault = filter.hasDefault;
			res.labelRes = filter.labelRes;
			res.nonLocalizedLabel = filter.nonLocalizedLabel;
			res.icon = filter.icon;
			res.system = isSystemApp(res.providerInfo.applicationInfo);
			return res;
		}

		@Override
		protected void sortResults(List<ResolveInfo> results) {
			Collections.sort(results, mResolvePrioritySorter);
		}

		@Override
		protected void dumpFilter(PrintWriter out, String prefix,
								  PackageParser.ProviderIntentInfo filter) {
			out.print(prefix);
			out.print(
					Integer.toHexString(System.identityHashCode(filter.provider)));
			out.print(' ');
			filter.provider.printComponentShortName(out);
			out.print(" filter ");
			out.println(Integer.toHexString(System.identityHashCode(filter)));
		}

		@Override
		protected Object filterToLabel(PackageParser.ProviderIntentInfo filter) {
			return filter.provider;
		}

		protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
			PackageParser.Provider provider = (PackageParser.Provider)label;
			out.print(prefix); out.print(
					Integer.toHexString(System.identityHashCode(provider)));
			out.print(' ');
			provider.printComponentShortName(out);
			if (count > 1) {
				out.print(" ("); out.print(count); out.print(" filters)");
			}
			out.println();
		}

		private final HashMap<ComponentName, PackageParser.Provider> mProviders
				= new HashMap<>();
		private int mFlags;
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
}
