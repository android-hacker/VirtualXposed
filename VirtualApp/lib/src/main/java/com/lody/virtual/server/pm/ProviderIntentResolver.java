package com.lody.virtual.server.pm;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.lody.virtual.helper.compat.ObjectsCompat;
import com.lody.virtual.helper.compat.PackageParserCompat;
import com.lody.virtual.helper.utils.VLog;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.lody.virtual.server.pm.VPackageManagerService.TAG;

final class ProviderIntentResolver extends IntentResolver<PackageParser.ProviderIntentInfo, ResolveInfo> {
	private final HashMap<ComponentName, PackageParser.Provider> mProviders = new HashMap<>();
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
				ArrayList<PackageParser.Provider> packageProviders) {
			if (packageProviders == null) {
				return null;
			}
			mFlags = flags;
			final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
			final int N = packageProviders.size();
			ArrayList<PackageParser.ProviderIntentInfo[]> listCut = new ArrayList<>(N);

			ArrayList<PackageParser.ProviderIntentInfo> intentFilters;
			for (int i = 0; i < N; ++i) {
				intentFilters = packageProviders.get(i).intents;
				if (intentFilters != null && intentFilters.size() > 0) {
					PackageParser.ProviderIntentInfo[] array = new PackageParser.ProviderIntentInfo[intentFilters
							.size()];
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
			final int NI = p.intents.size();
			int j;
			for (j = 0; j < NI; j++) {
				PackageParser.ProviderIntentInfo intent = p.intents.get(j);
				addFilter(intent);
			}
		}

		public final void removeProvider(PackageParser.Provider p) {
			mProviders.remove(p.getComponentName());
			final int NI = p.intents.size();
			int j;
			for (j = 0; j < NI; j++) {
				PackageParser.ProviderIntentInfo intent = p.intents.get(j);
				removeFilter(intent);
			}
		}

		@TargetApi(Build.VERSION_CODES.KITKAT)
		@Override
		protected boolean allowFilterResult(PackageParser.ProviderIntentInfo filter, List<ResolveInfo> dest) {
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
		protected boolean isPackageForFilter(String packageName, PackageParser.ProviderIntentInfo info) {
			return packageName.equals(info.provider.owner.packageName);
		}

		@TargetApi(Build.VERSION_CODES.KITKAT)
		@Override
		protected ResolveInfo newResult(PackageParser.ProviderIntentInfo filter, int match) {
			final PackageParser.Provider provider = filter.provider;
			ProviderInfo pi = PackageParserCompat.generateProviderInfo(provider, mFlags);
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
			return res;
		}

		@Override
		protected void sortResults(List<ResolveInfo> results) {
			Collections.sort(results, VPackageManagerService.mResolvePrioritySorter);
		}

		@Override
		protected void dumpFilter(PrintWriter out, String prefix, PackageParser.ProviderIntentInfo filter) {

		}

		@Override
		protected Object filterToLabel(PackageParser.ProviderIntentInfo filter) {
			return filter.provider;
		}

		protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {

		}
	}