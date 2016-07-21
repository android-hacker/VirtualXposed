package com.lody.virtual.service.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.util.LogPrinter;

import com.lody.virtual.helper.bundle.PackageParserCompat;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

final class ActivityIntentResolver extends IntentResolver<PackageParser.ActivityIntentInfo, ResolveInfo> {

    private static final boolean DEBUG_SHOW_INFO = true;
    private static final String TAG = ActivityIntentResolver.class.getSimpleName();
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
		ArrayList<ArrayList<PackageParser.ActivityIntentInfo>> listCut = new ArrayList<>(
				N);

		ArrayList<PackageParser.ActivityIntentInfo> intentFilters;
		for (int i = 0; i < N; ++i) {
			intentFilters = packageActivities.get(i).intents;
			if (intentFilters != null && intentFilters.size() > 0) {
				listCut.add(intentFilters);
			}
		}
		return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut);
	}

	public final void addActivity(PackageParser.Activity a, String type) {
		final boolean systemApp = isSystemApp(a.info.applicationInfo);
		mActivities.put(a.getComponentName(), a);
		if (DEBUG_SHOW_INFO)
			Log.v(TAG, "  " + type + " " + (a.info.nonLocalizedLabel != null ? a.info.nonLocalizedLabel : a.info.name)
					+ ":");
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
			if (!intent.debugCheck()) {
				Log.w(TAG, "==> For Activity " + a.info.name);
			}
			addFilter(intent);
		}
	}

	public final void removeActivity(PackageParser.Activity a, String type) {
		mActivities.remove(a.getComponentName());
		if (DEBUG_SHOW_INFO) {
			Log.v(TAG, "  " + type + " " + (a.info.nonLocalizedLabel != null ? a.info.nonLocalizedLabel : a.info.name)
					+ ":");
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
	protected boolean isFilterStopped(PackageParser.ActivityIntentInfo filter) {
		return false;
	}

	@Override
	protected String packageForFilter(PackageParser.ActivityIntentInfo info) {
		return info.activity.owner.packageName;
	}

	@Override
	protected ResolveInfo newResult(PackageParser.ActivityIntentInfo info, int match) {
		final PackageParser.Activity activity = info.activity;
		final ResolveInfo res = new ResolveInfo();
		res.activityInfo = PackageParserCompat.newParser().generateActivityInfo(activity, mFlags);
		if ((mFlags & PackageManager.GET_RESOLVED_FILTER) != 0) {
			res.filter = info;
		}
		res.priority = info.getPriority();
		res.preferredOrder = activity.owner.mPreferredOrder;
		res.match = match;
		res.isDefault = info.hasDefault;
		res.labelRes = info.labelRes;
		res.nonLocalizedLabel = info.nonLocalizedLabel;
		res.icon = info.icon;
		res.system = isSystemApp(res.activityInfo.applicationInfo);
		return res;
	}

    private static boolean isSystemApp(ApplicationInfo info) {
        return (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }


	@Override
	protected void sortResults(List<ResolveInfo> results) {
		Collections.sort(results, mResolvePrioritySorter);
	}

	@Override
	protected void dumpFilter(PrintWriter out, String prefix, PackageParser.ActivityIntentInfo filter) {
		out.print(prefix);
		out.print(Integer.toHexString(System.identityHashCode(filter.activity)));
		out.print(' ');
		out.print(filter.activity.info.toString());
		out.print(" filter ");
		out.println(Integer.toHexString(System.identityHashCode(filter)));
	}

    private static final Comparator<ResolveInfo> mResolvePrioritySorter =
            new Comparator<ResolveInfo>() {
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
}