package com.lody.virtual.server.pm;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.lody.virtual.helper.compat.ObjectsCompat;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.server.pm.parser.PackageParserEx;
import com.lody.virtual.server.pm.parser.VPackage;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.lody.virtual.server.pm.VPackageManagerService.TAG;

final class ProviderIntentResolver extends IntentResolver<VPackage.ProviderIntentInfo, ResolveInfo> {
    private final HashMap<ComponentName, VPackage.ProviderComponent> mProviders = new HashMap<>();
    private int mFlags;

    public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
        mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
        return super.queryIntent(intent, resolvedType, defaultOnly, userId);
    }

    public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags, int userId) {
        mFlags = flags;
        return super.queryIntent(intent, resolvedType, (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0, userId);
    }

    public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags,
                                                   ArrayList<VPackage.ProviderComponent> packageProviders, int userId) {
        if (packageProviders == null) {
            return null;
        }
        mFlags = flags;
        final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
        final int N = packageProviders.size();
        ArrayList<VPackage.ProviderIntentInfo[]> listCut = new ArrayList<>(N);

        ArrayList<VPackage.ProviderIntentInfo> intentFilters;
        for (int i = 0; i < N; ++i) {
            intentFilters = packageProviders.get(i).intents;
            if (intentFilters != null && intentFilters.size() > 0) {
                VPackage.ProviderIntentInfo[] array = new VPackage.ProviderIntentInfo[intentFilters
                        .size()];
                intentFilters.toArray(array);
                listCut.add(array);
            }
        }
        return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
    }

    public final void addProvider(VPackage.ProviderComponent p) {
        if (mProviders.containsKey(p.getComponentName())) {
            VLog.w(TAG, "Provider " + p.getComponentName() + " already defined; ignoring");
            return;
        }

        mProviders.put(p.getComponentName(), p);
        final int NI = p.intents.size();
        int j;
        for (j = 0; j < NI; j++) {
            VPackage.ProviderIntentInfo intent = p.intents.get(j);
            addFilter(intent);
        }
    }

    public final void removeProvider(VPackage.ProviderComponent p) {
        mProviders.remove(p.getComponentName());
        final int NI = p.intents.size();
        int j;
        for (j = 0; j < NI; j++) {
            VPackage.ProviderIntentInfo intent = p.intents.get(j);
            removeFilter(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean allowFilterResult(VPackage.ProviderIntentInfo filter, List<ResolveInfo> dest) {
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
    protected VPackage.ProviderIntentInfo[] newArray(int size) {
        return new VPackage.ProviderIntentInfo[size];
    }

    @Override
    protected boolean isFilterStopped(VPackage.ProviderIntentInfo filter) {
        return false;
    }

    @Override
    protected boolean isPackageForFilter(String packageName, VPackage.ProviderIntentInfo info) {
        return packageName.equals(info.provider.owner.packageName);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected ResolveInfo newResult(VPackage.ProviderIntentInfo filter, int match, int userId) {
        final VPackage.ProviderComponent provider = filter.provider;
        PackageSetting ps = (PackageSetting) provider.owner.mExtras;
        ProviderInfo pi = PackageParserEx.generateProviderInfo(provider, mFlags, ps.readUserState(userId), userId);
        if (pi == null) {
            return null;
        }
        final ResolveInfo res = new ResolveInfo();
        res.providerInfo = pi;
        if ((mFlags & PackageManager.GET_RESOLVED_FILTER) != 0) {
            res.filter = filter.filter;
        }
        res.priority = filter.filter.getPriority();
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
        Collections.sort(results, VPackageManagerService.sResolvePrioritySorter);
    }

    @Override
    protected void dumpFilter(PrintWriter out, String prefix, VPackage.ProviderIntentInfo filter) {

    }

    @Override
    protected Object filterToLabel(VPackage.ProviderIntentInfo filter) {
        return filter.provider;
    }

    protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {

    }
}