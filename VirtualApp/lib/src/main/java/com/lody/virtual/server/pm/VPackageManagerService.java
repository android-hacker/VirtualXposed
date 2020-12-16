package com.lody.virtual.server.pm;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
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
import android.text.TextUtils;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.fixer.ComponentFixer;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.compat.ObjectsCompat;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.VParceledListSlice;
import com.lody.virtual.server.IPackageInstaller;
import com.lody.virtual.server.IPackageManager;
import com.lody.virtual.server.pm.installer.VPackageInstallerService;
import com.lody.virtual.server.pm.parser.PackageParserEx;
import com.lody.virtual.server.pm.parser.VPackage;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_UNAWARE;

/**
 * @author Lody
 */
public class VPackageManagerService extends IPackageManager.Stub {

    static final String TAG = "PackageManager";
    static final Comparator<ResolveInfo> sResolvePrioritySorter = new Comparator<ResolveInfo>() {
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
    private static final AtomicReference<VPackageManagerService> gService = new AtomicReference<>();
    private static final Comparator<ProviderInfo> sProviderInitOrderSorter = new Comparator<ProviderInfo>() {
        public int compare(ProviderInfo p1, ProviderInfo p2) {
            final int v1 = p1.initOrder;
            final int v2 = p2.initOrder;
            return (v1 > v2) ? -1 : ((v1 < v2) ? 1 : 0);
        }
    };

    private final ResolveInfo mResolveInfo;

    private final ActivityIntentResolver mActivities = new ActivityIntentResolver();
    private final ServiceIntentResolver mServices = new ServiceIntentResolver();
    private final ActivityIntentResolver mReceivers = new ActivityIntentResolver();
    private final ProviderIntentResolver mProviders = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? new ProviderIntentResolver() : null;

    private final HashMap<ComponentName, VPackage.ProviderComponent> mProvidersByComponent = new HashMap<>();

    private final HashMap<String, VPackage.PermissionComponent> mPermissions = new HashMap<>();
    private final HashMap<String, VPackage.PermissionGroupComponent> mPermissionGroups = new HashMap<>();
    private final HashMap<String, VPackage.ProviderComponent> mProvidersByAuthority = new HashMap<>();

    private final Map<String, VPackage> mPackages = PackageCacheManager.PACKAGE_CACHE;


    public VPackageManagerService() {
        Intent intent = new Intent();
        intent.setClassName(VirtualCore.get().getHostPkg(), VASettings.RESOLVER_ACTIVITY);
        mResolveInfo = VirtualCore.get().getUnHookPackageManager().resolveActivity(intent, 0);
    }

    public static void systemReady() {
        VPackageManagerService instance = new VPackageManagerService();
        new VUserManagerService(VirtualCore.get().getContext(), instance, new char[0], instance.mPackages);
        gService.set(instance);
    }

    public static VPackageManagerService get() {
        return gService.get();
    }


    void analyzePackageLocked(VPackage pkg) {
        int N = pkg.activities.size();
        for (int i = 0; i < N; i++) {
            VPackage.ActivityComponent a = pkg.activities.get(i);
            if (a.info.processName == null) {
                a.info.processName = a.info.packageName;
            }
            mActivities.addActivity(a, "activity");
        }
        N = pkg.services.size();
        for (int i = 0; i < N; i++) {
            VPackage.ServiceComponent a = pkg.services.get(i);
            if (a.info.processName == null) {
                a.info.processName = a.info.packageName;
            }
            mServices.addService(a);
        }
        N = pkg.receivers.size();
        for (int i = 0; i < N; i++) {
            VPackage.ActivityComponent a = pkg.receivers.get(i);
            if (a.info.processName == null) {
                a.info.processName = a.info.packageName;
            }
            mReceivers.addActivity(a, "receiver");
        }

        N = pkg.providers.size();
        for (int i = 0; i < N; i++) {
            VPackage.ProviderComponent p = pkg.providers.get(i);
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
            VPackage.PermissionComponent permission = pkg.permissions.get(i);
            mPermissions.put(permission.className, permission);
        }
        N = pkg.permissionGroups.size();
        for (int i = 0; i < N; i++) {
            VPackage.PermissionGroupComponent group = pkg.permissionGroups.get(i);
            mPermissionGroups.put(group.className, group);
        }
    }

    void deletePackageLocked(String packageName) {
        VPackage pkg = mPackages.get(packageName);
        if (pkg == null) {
            return;
        }
        int N = pkg.activities.size();
        for (int i = 0; i < N; i++) {
            VPackage.ActivityComponent a = pkg.activities.get(i);
            mActivities.removeActivity(a, "activity");
        }
        N = pkg.services.size();
        for (int i = 0; i < N; i++) {
            VPackage.ServiceComponent a = pkg.services.get(i);
            mServices.removeService(a);
        }
        N = pkg.receivers.size();
        for (int i = 0; i < N; i++) {
            VPackage.ActivityComponent a = pkg.receivers.get(i);
            mReceivers.removeActivity(a, "receiver");
        }

        N = pkg.providers.size();
        for (int i = 0; i < N; i++) {
            VPackage.ProviderComponent p = pkg.providers.get(i);
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
            VPackage.PermissionComponent permission = pkg.permissions.get(i);
            mPermissions.remove(permission.className);
        }
        N = pkg.permissionGroups.size();
        for (int i = 0; i < N; i++) {
            VPackage.PermissionGroupComponent group = pkg.permissionGroups.get(i);
            mPermissionGroups.remove(group.className);
        }
    }

    @Override
    public List<String> getSharedLibraries(String packageName) {
        synchronized (mPackages) {
            VPackage p = mPackages.get(packageName);
            if (p != null) {
                ArrayList<String> list = new ArrayList<>();
                if (p.usesLibraries != null) {
                    list.addAll(p.usesLibraries);
                }
                if (p.usesOptionalLibraries != null) {
                    list.addAll(p.usesOptionalLibraries);
                }
                return list;
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
        checkUserId(userId);
        synchronized (mPackages) {
            VPackage p = mPackages.get(packageName);
            if (p != null) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                return generatePackageInfo(p, ps, flags, userId);
            }
        }
        return null;
    }

    private PackageInfo generatePackageInfo(VPackage p, PackageSetting ps, int flags, int userId) {
        flags = updateFlagsNought(flags);
        PackageInfo packageInfo = PackageParserEx.generatePackageInfo(p, flags,
                ps.firstInstallTime, ps.lastUpdateTime, ps.readUserState(userId), userId);
        if (packageInfo != null) {
            return packageInfo;
        }
        return null;
    }

    private int updateFlagsNought(int flags) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return flags;
        }
        if ((flags & (PackageManager.MATCH_DIRECT_BOOT_UNAWARE
                | PackageManager.MATCH_DIRECT_BOOT_AWARE)) != 0) {
            // Caller expressed an explicit opinion about what encryption
            // aware/unaware components they want to see, so fall through and
            // give them what they want
        } else {
            // Caller expressed no opinion, so match based on user state
            flags |= PackageManager.MATCH_DIRECT_BOOT_AWARE | MATCH_DIRECT_BOOT_UNAWARE;
        }
        return flags;
    }

    private void checkUserId(int userId) {
        if (!VUserManagerService.get().exists(userId)) {
            throw new SecurityException("Invalid userId " + userId);
        }
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
        synchronized (mPackages) {
            VPackage p = mPackages.get(component.getPackageName());
            if (p != null) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                VPackage.ActivityComponent a = mActivities.mActivities.get(component);
                if (a != null) {
                    ActivityInfo activityInfo = PackageParserEx.generateActivityInfo(a, flags, ps.readUserState(userId), userId);
                    ComponentFixer.fixComponentInfo(ps, activityInfo, userId);
                    return activityInfo;
                }
            }
        }
        return null;
    }

    @Override
    public boolean activitySupportsIntent(ComponentName component, Intent intent, String resolvedType) {
        synchronized (mPackages) {
            VPackage.ActivityComponent a = mActivities.mActivities.get(component);
            if (a == null) {
                return false;
            }
            for (int i = 0; i < a.intents.size(); i++) {
                if (a.intents.get(i).filter.match(intent.getAction(), resolvedType, intent.getScheme(), intent.getData(),
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
        flags = updateFlagsNought(flags);
        synchronized (mPackages) {
            VPackage p = mPackages.get(component.getPackageName());
            if (p != null) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                VPackage.ActivityComponent a = mReceivers.mActivities.get(component);
                if (a != null) {
                    ActivityInfo receiverInfo = PackageParserEx.generateActivityInfo(a, flags, ps.readUserState(userId), userId);
                    ComponentFixer.fixComponentInfo(ps, receiverInfo, userId);
                    return receiverInfo;
                }
            }
        }
        return null;
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName component, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
        synchronized (mPackages) {
            VPackage p = mPackages.get(component.getPackageName());
            if (p != null) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                VPackage.ServiceComponent s = mServices.mServices.get(component);
                if (s != null) {
                    ServiceInfo serviceInfo = PackageParserEx.generateServiceInfo(s, flags, ps.readUserState(userId), userId);
                    ComponentFixer.fixComponentInfo(ps, serviceInfo, userId);
                    return serviceInfo;
                }
            }
        }
        return null;
    }

    @Override
    public ProviderInfo getProviderInfo(ComponentName component, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
        synchronized (mPackages) {
            VPackage p = mPackages.get(component.getPackageName());
            if (p != null) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                VPackage.ProviderComponent provider = mProvidersByComponent.get(component);
                if (provider != null) {
                    ProviderInfo providerInfo = PackageParserEx.generateProviderInfo(provider, flags, ps.readUserState(userId), userId);
                    ComponentFixer.fixComponentInfo(ps, providerInfo, userId);
                    return providerInfo;
                }
            }
        }
        return null;
    }

    @Override
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
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
                // If we have saved a preference for a preferred activity for
                // this Intent, use that.

                ResolveInfo ri = findPreferredActivity(intent, resolvedType,
                        flags, query, r0.priority);
                //noinspection ConstantConditions
                if (ri != null) {
                    return ri;
                }
                return query.get(0);
            }
        }
        return null;
    }

    private ResolveInfo findPreferredActivity(Intent intent, String resolvedType, int flags, List<ResolveInfo> query, int priority) {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
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
                return mActivities.queryIntent(intent, resolvedType, flags, userId);
            }
            final VPackage pkg = mPackages.get(pkgName);
            if (pkg != null) {
                return mActivities.queryIntentForPackage(intent, resolvedType, flags, pkg.activities, userId);
            }
            return Collections.emptyList();
        }
    }

    @Override
    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
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
                return mReceivers.queryIntent(intent, resolvedType, flags, userId);
            }
            final VPackage pkg = mPackages.get(pkgName);
            if (pkg != null) {
                return mReceivers.queryIntentForPackage(intent, resolvedType, flags, pkg.receivers, userId);
            }
            return Collections.emptyList();
        }
    }

    @Override
    public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
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
        flags = updateFlagsNought(flags);
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
                return mServices.queryIntent(intent, resolvedType, flags, userId);
            }
            final VPackage pkg = mPackages.get(pkgName);
            if (pkg != null) {
                return mServices.queryIntentForPackage(intent, resolvedType, flags, pkg.services, userId);
            }
            return Collections.emptyList();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
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
                return mProviders.queryIntent(intent, resolvedType, flags, userId);
            }
            final VPackage pkg = mPackages.get(pkgName);
            if (pkg != null) {
                return mProviders.queryIntentForPackage(intent, resolvedType, flags, pkg.providers, userId);
            }
            return Collections.emptyList();
        }
    }

    @Override
    public VParceledListSlice<ProviderInfo> queryContentProviders(String processName, int vuid, int flags) {
        int userId = VUserHandle.getUserId(vuid);
        checkUserId(userId);
        flags = updateFlagsNought(flags);
        ArrayList<ProviderInfo> finalList = new ArrayList<>(3);
        // reader
        synchronized (mPackages) {
            for (VPackage.ProviderComponent p : mProvidersByComponent.values()) {
                PackageSetting ps = (PackageSetting) p.owner.mExtras;
                if (processName == null || ps.appId == VUserHandle.getAppId(vuid) && p.info.processName.equals(processName)) {
                    ProviderInfo providerInfo = PackageParserEx.generateProviderInfo(p, flags, ps.readUserState(userId), userId);
                    finalList.add(providerInfo);
                }
            }
        }
        if (!finalList.isEmpty()) {
            Collections.sort(finalList, sProviderInitOrderSorter);
        }
        return new VParceledListSlice<>(finalList);
    }

    @Override
    public VParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) {
        checkUserId(userId);
        ArrayList<PackageInfo> pkgList = new ArrayList<>(mPackages.size());
        synchronized (mPackages) {
            for (VPackage p : mPackages.values()) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                PackageInfo info = generatePackageInfo(p, ps, flags, userId);
                if (info != null) {
                    pkgList.add(info);
                }
            }
        }
        return new VParceledListSlice<>(pkgList);
    }

    @Override
    public VParceledListSlice<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
        ArrayList<ApplicationInfo> list = new ArrayList<>(mPackages.size());
        synchronized (mPackages) {
            for (VPackage p : mPackages.values()) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                ApplicationInfo info = PackageParserEx.generateApplicationInfo(p, flags, ps.readUserState(userId), userId);
                list.add(info);
            }
        }
        return new VParceledListSlice<>(list);
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) {
        synchronized (mPackages) {
            VPackage.PermissionComponent p = mPermissions.get(name);
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
            VPackage.PermissionGroupComponent p = mPermissionGroups.get(name);
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
            for (VPackage.PermissionGroupComponent pg : mPermissionGroups.values()) {
                out.add(new PermissionGroupInfo(pg.info));
            }
            return out;
        }
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
        synchronized (mPackages) {
            final VPackage.ProviderComponent provider = mProvidersByAuthority.get(name);
            if (provider != null) {
                PackageSetting ps = (PackageSetting) provider.owner.mExtras;
                ProviderInfo providerInfo = PackageParserEx.generateProviderInfo(provider, flags, ps.readUserState(userId), userId);
                if (providerInfo != null) {
                    VPackage p = mPackages.get(providerInfo.packageName);
                    PackageSetting settings = (PackageSetting) p.mExtras;
                    ComponentFixer.fixComponentInfo(settings, providerInfo, userId);
                    return providerInfo;
                }
            }
        }
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        checkUserId(userId);
        flags = updateFlagsNought(flags);
        synchronized (mPackages) {
            VPackage p = mPackages.get(packageName);
            if (p != null) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                return PackageParserEx.generateApplicationInfo(p, flags, ps.readUserState(userId), userId);
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
            for (VPackage p : mPackages.values()) {
                PackageSetting settings = (PackageSetting) p.mExtras;
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
            VPackage p = mPackages.get(packageName);
            if (p != null) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                return VUserHandle.getUid(userId, ps.appId);
            }
            return -1;
        }
    }

    @Override
    public String getNameForUid(int uid) {
        int appId = VUserHandle.getAppId(uid);
        synchronized (mPackages) {
            for (VPackage p : mPackages.values()) {
                PackageSetting ps = (PackageSetting) p.mExtras;
                if (ps.appId == appId) {
                    return ps.packageName;
                }
            }
            return null;
        }
    }


    @Override
    public List<String> querySharedPackages(String packageName) {
        synchronized (mPackages) {
            VPackage p = mPackages.get(packageName);
            if (p == null || p.mSharedUserId == null) {
                // noinspection unchecked
                return Collections.EMPTY_LIST;
            }
            ArrayList<String> list = new ArrayList<>();
            for (VPackage one : mPackages.values()) {
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

    @Override
    public IPackageInstaller getPackageInstaller() {
        return VPackageInstallerService.get();
    }

    void createNewUser(int userId, File userPath) {
        for (VPackage p : mPackages.values()) {
            PackageSetting setting = (PackageSetting) p.mExtras;
            setting.modifyUserState(userId);
        }
    }

    void cleanUpUser(int userId) {
        for (VPackage p : mPackages.values()) {
            PackageSetting ps = (PackageSetting) p.mExtras;
            ps.removeUser(userId);
        }
    }

    private final class ActivityIntentResolver extends IntentResolver<VPackage.ActivityIntentInfo, ResolveInfo> {
        // Keys are String (activity class name), values are Activity.
        private final HashMap<ComponentName, VPackage.ActivityComponent> mActivities = new HashMap<>();
        private int mFlags;

        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
            mFlags = defaultOnly ? PackageManager.MATCH_DEFAULT_ONLY : 0;
            return super.queryIntent(intent, resolvedType, defaultOnly, userId);
        }

        List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags, int userId) {
            mFlags = flags;
            return super.queryIntent(intent, resolvedType, (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0, userId);
        }

        List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags,
                                                ArrayList<VPackage.ActivityComponent> packageActivities, int userId) {
            if (packageActivities == null) {
                return null;
            }
            mFlags = flags;
            final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
            final int N = packageActivities.size();
            ArrayList<VPackage.ActivityIntentInfo[]> listCut = new ArrayList<VPackage.ActivityIntentInfo[]>(
                    N);

            ArrayList<VPackage.ActivityIntentInfo> intentFilters;
            for (int i = 0; i < N; ++i) {
                intentFilters = packageActivities.get(i).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    VPackage.ActivityIntentInfo[] array = new VPackage.ActivityIntentInfo[intentFilters
                            .size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        public final void addActivity(VPackage.ActivityComponent a, String type) {
            mActivities.put(a.getComponentName(), a);
            final int NI = a.intents.size();
            for (int j = 0; j < NI; j++) {
                VPackage.ActivityIntentInfo intent = a.intents.get(j);
                if (intent.filter.getPriority() > 0 && "activity".equals(type)) {
                    intent.filter.setPriority(0);
                    Log.w(TAG, "Package " + a.info.applicationInfo.packageName + " has activity " + a.className
                            + " with priority > 0, forcing to 0");
                }
                addFilter(intent);
            }
        }

        public final void removeActivity(VPackage.ActivityComponent a, String type) {
            mActivities.remove(a.getComponentName());
            final int NI = a.intents.size();
            for (int j = 0; j < NI; j++) {
                VPackage.ActivityIntentInfo intent = a.intents.get(j);
                removeFilter(intent);
            }
        }

        @Override
        protected boolean allowFilterResult(VPackage.ActivityIntentInfo filter, List<ResolveInfo> dest) {
            ActivityInfo filterAi = filter.activity.info;
            for (int i = dest.size() - 1; i >= 0; i--) {
                ActivityInfo destAi = dest.get(i).activityInfo;
                if (ObjectsCompat.equals(destAi.name, filterAi.name) && ObjectsCompat.equals(destAi.packageName, filterAi.packageName)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected VPackage.ActivityIntentInfo[] newArray(int size) {
            return new VPackage.ActivityIntentInfo[size];
        }

        @Override
        protected boolean isFilterStopped(VPackage.ActivityIntentInfo filter) {
            return false;
        }

        @Override
        protected boolean isPackageForFilter(String packageName, VPackage.ActivityIntentInfo info) {
            return packageName.equals(info.activity.owner.packageName);
        }

        @Override
        protected ResolveInfo newResult(VPackage.ActivityIntentInfo info, int match, int userId) {
            final VPackage.ActivityComponent activity = info.activity;
            PackageSetting ps = (PackageSetting) activity.owner.mExtras;
            ActivityInfo ai = PackageParserEx.generateActivityInfo(activity, mFlags, ps.readUserState(userId), userId);
            if (ai == null) {
                return null;
            }
            final ResolveInfo res = new ResolveInfo();
            res.activityInfo = ai;
            if ((mFlags & PackageManager.GET_RESOLVED_FILTER) != 0) {
                res.filter = info.filter;
            }
            res.priority = info.filter.getPriority();
            res.preferredOrder = activity.owner.mPreferredOrder;
            res.match = match;
            res.isDefault = info.hasDefault;
            res.labelRes = info.labelRes;
            res.nonLocalizedLabel = info.nonLocalizedLabel;
            res.icon = info.icon;
            return res;
        }

        @Override
        protected void sortResults(List<ResolveInfo> results) {
            Collections.sort(results, sResolvePrioritySorter);
        }

        @Override
        protected void dumpFilter(PrintWriter out, String prefix, VPackage.ActivityIntentInfo filter) {

        }

        @Override
        protected Object filterToLabel(VPackage.ActivityIntentInfo filter) {
            return filter.activity;
        }

        protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {

        }
    }

    private final class ServiceIntentResolver extends IntentResolver<VPackage.ServiceIntentInfo, ResolveInfo> {
        // Keys are String (activity class name), values are Activity.
        private final HashMap<ComponentName, VPackage.ServiceComponent> mServices = new HashMap<>();
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
                                                       ArrayList<VPackage.ServiceComponent> packageServices, int userId) {
            if (packageServices == null) {
                return null;
            }
            mFlags = flags;
            final boolean defaultOnly = (flags & PackageManager.MATCH_DEFAULT_ONLY) != 0;
            final int N = packageServices.size();
            ArrayList<VPackage.ServiceIntentInfo[]> listCut = new ArrayList<VPackage.ServiceIntentInfo[]>(N);

            ArrayList<VPackage.ServiceIntentInfo> intentFilters;
            for (int i = 0; i < N; ++i) {
                intentFilters = packageServices.get(i).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    VPackage.ServiceIntentInfo[] array = new VPackage.ServiceIntentInfo[intentFilters.size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        public final void addService(VPackage.ServiceComponent s) {
            mServices.put(s.getComponentName(), s);
            final int NI = s.intents.size();
            int j;
            for (j = 0; j < NI; j++) {
                VPackage.ServiceIntentInfo intent = s.intents.get(j);
                addFilter(intent);
            }
        }

        public final void removeService(VPackage.ServiceComponent s) {
            mServices.remove(s.getComponentName());
            final int NI = s.intents.size();
            int j;
            for (j = 0; j < NI; j++) {
                VPackage.ServiceIntentInfo intent = s.intents.get(j);
                removeFilter(intent);
            }
        }

        @Override
        protected boolean allowFilterResult(VPackage.ServiceIntentInfo filter, List<ResolveInfo> dest) {
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
        protected VPackage.ServiceIntentInfo[] newArray(int size) {
            return new VPackage.ServiceIntentInfo[size];
        }

        @Override
        protected boolean isFilterStopped(VPackage.ServiceIntentInfo filter) {
            return false;
        }

        @Override
        protected boolean isPackageForFilter(String packageName, VPackage.ServiceIntentInfo info) {
            return packageName.equals(info.service.owner.packageName);
        }

        @Override
        protected ResolveInfo newResult(VPackage.ServiceIntentInfo filter, int match, int userId) {
            final VPackage.ServiceComponent service = filter.service;
            PackageSetting ps = (PackageSetting) service.owner.mExtras;
            ServiceInfo si = PackageParserEx.generateServiceInfo(service, mFlags, ps.readUserState(userId), userId);
            if (si == null) {
                return null;
            }
            final ResolveInfo res = new ResolveInfo();
            res.serviceInfo = si;
            if ((mFlags & PackageManager.GET_RESOLVED_FILTER) != 0) {
                res.filter = filter.filter;
            }
            res.priority = filter.filter.getPriority();
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
            Collections.sort(results, sResolvePrioritySorter);
        }

        @Override
        protected void dumpFilter(PrintWriter out, String prefix, VPackage.ServiceIntentInfo filter) {

        }

        @Override
        protected Object filterToLabel(VPackage.ServiceIntentInfo filter) {
            return filter.service;
        }

        protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {

        }
    }

}
