package com.lody.virtual.server.pm.parser;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Parcel;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.fixer.ComponentFixer;
import com.lody.virtual.helper.collection.ArrayMap;
import com.lody.virtual.helper.compat.PackageParserCompat;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.server.pm.PackageSetting;
import com.lody.virtual.server.pm.PackageUserState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mirror.android.content.pm.ApplicationInfoL;
import mirror.android.content.pm.ApplicationInfoN;

/**
 * @author Lody
 */

public class PackageParserEx {

    private static final String TAG = PackageParserEx.class.getSimpleName();

    private static final ArrayMap<String, String[]> sSharedLibCache = new ArrayMap<>();

    private static final String FAKE_SIG = "308203553082023da0030201020204378edaaa300d06092a864886f70d01010b0500305a310d300b0603550406130466616b65310d300b0603550408130466616b65310d300b0603550407130466616b65310d300b060355040a130466616b65310d300b060355040b130466616b65310d300b0603550403130466616b653020170d3138303533303034343434385a180f32313237313230353034343434385a305a310d300b0603550406130466616b65310d300b0603550408130466616b65310d300b0603550407130466616b65310d300b060355040a130466616b65310d300b060355040b130466616b65310d300b0603550403130466616b6530820122300d06092a864886f70d01010105000382010f003082010a0282010100b766ff6afd8a53edd4cee4985bc90e0c515157b5e9f731818961f7250d0d1ac7c7fb80eb5aeb8c28478732e8ff38cff574bfa0eba8039f73af1532f939c4ef9684719efbaba2dd3c583a20907c1c55248a63098c6da23dcfc877763d5fe6061dddd399cf2f49e3250e23f9e687a4d182bcd0662179ba4c9983448e34b4c83e5abbf4f87e87add9157c75fd40de3416744507a3517915f35b6fcad78766e8e1879df8ab823a6ffa335e4790f6e29c87393732025b63ce3a38e42cb0d48cdceb902f191d7d45823db9a0678895e8bfc59b2af7526ca4c2dc3dbe7e70c7c840e666b9629d36e5ddf1d9a80c37f1ab1bc1fb30432914008fbde95d5d3db7853565510203010001a321301f301d0603551d0e04160414d8513e1ae21c64e9ebeee3507e24ea375eef958e300d06092a864886f70d01010b0500038201010088bf20b36428558359536dddcfff16fe233656a92364cb544d8acc43b0859f880a8da339dd430616085edf035e4e6e6dd2281ceb14adde2f05e9ac58d547a09083eece0c6d405289cb7918f85754ee545eefe35e30c103cad617905e94eb4fb68e6920a60d30577855f9feb6e3a664856f74aa9f824aa7d4a3adf85e162c67b9a4261e3185f038ead96112ae3e574d280425e90567352fb82bc9173302122025eaecfabd94d0f9be69a85c415f7cf7759c9651734300952027b316c37aaa1b2418865a3fc7b6bd1072c92ccaacdaa1cf9586d9b8310ceee066ce68859107dfc45ccce729ad9e75b53b584fa37dcd64da8673b1279c6c5861ed3792deac156c8a";

    public static VPackage parsePackage(File packageFile) throws Throwable {
        PackageParser parser = PackageParserCompat.createParser(packageFile);
        PackageParser.Package p = PackageParserCompat.parsePackage(parser, packageFile, 0);
        if (p.requestedPermissions.contains("android.permission.FAKE_PACKAGE_SIGNATURE")
                && p.mAppMetaData != null
                && p.mAppMetaData.containsKey("fake-signature")) {
            String sig = p.mAppMetaData.getString("fake-signature");
            buildSignature(p, new Signature[]{new Signature(sig)});
            VLog.d(TAG, "Using fake-signature feature on : " + p.packageName);
        } else {
            try {
                PackageParserCompat.collectCertificates(parser, p, PackageParser.PARSE_IS_SYSTEM);
            } catch (Throwable e) {
                VLog.e(TAG, "collectCertificates failed", e);
                if (VirtualCore.get().getContext().getFileStreamPath(Constants.FAKE_SIGNATURE_FLAG).exists()) {
                    VLog.w(TAG, "Using fake signature: " + p.packageName);
                    buildSignature(p, new Signature[]{new Signature(FAKE_SIG)});
                } else {
                    throw e;
                }
            }
        }
        return buildPackageCache(p);
    }

    private static void buildSignature(PackageParser.Package p, Signature[] signatures) {
        if (Build.VERSION.SDK_INT < 28) {
            p.mSignatures = signatures;
        } else {
            Object signingDetails = mirror.android.content.pm.PackageParser.Package.mSigningDetails.get(p);
            mirror.android.content.pm.PackageParser.SigningDetails.pastSigningCertificates.set(signingDetails, signatures);
            mirror.android.content.pm.PackageParser.SigningDetails.signatures.set(signingDetails, signatures);
        }
    }

    public static VPackage readPackageCache(String packageName) {
        Parcel p = Parcel.obtain();
        try {
            File cacheFile = VEnvironment.getPackageCacheFile(packageName);
            FileInputStream is = new FileInputStream(cacheFile);
            byte[] bytes = FileUtils.toByteArray(is);
            is.close();
            p.unmarshall(bytes, 0, bytes.length);
            p.setDataPosition(0);
            if (p.readInt() != 4) {
                throw new IllegalStateException("Invalid version.");
            }
            VPackage pkg = new VPackage(p);
            addOwner(pkg);
            return pkg;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            p.recycle();
        }
        return null;
    }

    public static void readSignature(VPackage pkg) {
        File signatureFile = VEnvironment.getSignatureFile(pkg.packageName);
        if (!signatureFile.exists()) {
            return;
        }
        Parcel p = Parcel.obtain();
        try {
            FileInputStream fis = new FileInputStream(signatureFile);
            byte[] bytes = FileUtils.toByteArray(fis);
            fis.close();
            p.unmarshall(bytes, 0, bytes.length);
            p.setDataPosition(0);
            pkg.mSignatures = p.createTypedArray(Signature.CREATOR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            p.recycle();
        }
    }

    public static void savePackageCache(VPackage pkg) {
        final String packageName = pkg.packageName;
        Parcel p = Parcel.obtain();
        try {
            p.writeInt(4);
            pkg.writeToParcel(p, 0);
            FileOutputStream fos = new FileOutputStream(VEnvironment.getPackageCacheFile(packageName));
            fos.write(p.marshall());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            p.recycle();
        }
        Signature[] signatures = pkg.mSignatures;
        if (signatures != null) {
            File signatureFile = VEnvironment.getSignatureFile(packageName);
            if (signatureFile.exists() && !signatureFile.delete()) {
                VLog.w(TAG, "Unable to delete the signatures of " + packageName);
            }
            p = Parcel.obtain();
            try {
                p.writeTypedArray(signatures, 0);
                FileUtils.writeParcelToFile(p, signatureFile);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                p.recycle();
            }
        }
    }

    private static VPackage buildPackageCache(PackageParser.Package p) {
        VPackage cache = new VPackage();
        cache.activities = new ArrayList<>(p.activities.size());
        cache.services = new ArrayList<>(p.services.size());
        cache.receivers = new ArrayList<>(p.receivers.size());
        cache.providers = new ArrayList<>(p.providers.size());
        cache.instrumentation = new ArrayList<>(p.instrumentation.size());
        cache.permissions = new ArrayList<>(p.permissions.size());
        cache.permissionGroups = new ArrayList<>(p.permissionGroups.size());

        for (PackageParser.Activity activity : p.activities) {
            cache.activities.add(new VPackage.ActivityComponent(activity));
        }
        for (PackageParser.Service service : p.services) {
            cache.services.add(new VPackage.ServiceComponent(service));
        }
        for (PackageParser.Activity receiver : p.receivers) {
            cache.receivers.add(new VPackage.ActivityComponent(receiver));
        }
        for (PackageParser.Provider provider : p.providers) {
            cache.providers.add(new VPackage.ProviderComponent(provider));
        }
        for (PackageParser.Instrumentation instrumentation : p.instrumentation) {
            cache.instrumentation.add(new VPackage.InstrumentationComponent(instrumentation));
        }
        cache.requestedPermissions = new ArrayList<>(p.requestedPermissions.size());
        cache.requestedPermissions.addAll(p.requestedPermissions);
        if (mirror.android.content.pm.PackageParser.Package.protectedBroadcasts != null) {
            List<String> protectedBroadcasts = mirror.android.content.pm.PackageParser.Package.protectedBroadcasts.get(p);
            if (protectedBroadcasts != null) {
                cache.protectedBroadcasts = new ArrayList<>(protectedBroadcasts);
                cache.protectedBroadcasts.addAll(protectedBroadcasts);
            }
        }
        cache.applicationInfo = p.applicationInfo;
        if (Build.VERSION.SDK_INT < 28) {
            cache.mSignatures = p.mSignatures;
        } else {
            Object signingDetails = mirror.android.content.pm.PackageParser.Package.mSigningDetails.get(p);
            boolean hasPastSigningCertificates = mirror.android.content.pm.PackageParser.SigningDetails.hasPastSigningCertificates.call(signingDetails);
            if (hasPastSigningCertificates) {
                cache.mSignatures = new Signature[1];
                cache.mSignatures[0] = mirror.android.content.pm.PackageParser.SigningDetails.pastSigningCertificates.get(signingDetails)[0];
            } else {
                boolean hasSignatures = mirror.android.content.pm.PackageParser.SigningDetails.hasSignatures.call(signingDetails);
                if (hasSignatures) {
                    Signature[] signatures = mirror.android.content.pm.PackageParser.SigningDetails.signatures.get(signingDetails);
                    int numberOfSigs = signatures.length;
                    cache.mSignatures = new Signature[numberOfSigs];
                    System.arraycopy(signatures, 0, cache.mSignatures, 0, numberOfSigs);
                }
            }
        }
        cache.mAppMetaData = p.mAppMetaData;
        cache.packageName = p.packageName;
        cache.mPreferredOrder = p.mPreferredOrder;
        cache.mVersionName = p.mVersionName;
        cache.mSharedUserId = p.mSharedUserId;
        cache.mSharedUserLabel = p.mSharedUserLabel;
        cache.usesLibraries = p.usesLibraries;
        cache.mVersionCode = p.mVersionCode;
        cache.mAppMetaData = p.mAppMetaData;
        cache.configPreferences = p.configPreferences;
        cache.reqFeatures = p.reqFeatures;
        addOwner(cache);
        return cache;
    }

    public static void initApplicationInfoBase(PackageSetting ps, VPackage p) {
        ApplicationInfo ai = p.applicationInfo;
        ai.flags |= ApplicationInfo.FLAG_HAS_CODE;
        if (TextUtils.isEmpty(ai.processName)) {
            ai.processName = ai.packageName;
        }
        ai.enabled = true;
        ai.nativeLibraryDir = ps.libPath;
        ai.uid = ps.appId;
        ai.name = ComponentFixer.fixComponentClassName(ps.packageName, ai.name);
        ai.publicSourceDir = ps.apkPath;
        ai.sourceDir = ps.apkPath;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ai.splitSourceDirs = new String[]{ps.apkPath};
            ai.splitPublicSourceDirs = ai.splitSourceDirs;
            ApplicationInfoL.scanSourceDir.set(ai, ai.dataDir);
            ApplicationInfoL.scanPublicSourceDir.set(ai, ai.dataDir);
            String hostPrimaryCpuAbi = ApplicationInfoL.primaryCpuAbi.get(VirtualCore.get().getContext().getApplicationInfo());
            ApplicationInfoL.primaryCpuAbi.set(ai, hostPrimaryCpuAbi);
        }

        if (ps.dependSystem) {
            String[] sharedLibraryFiles = sSharedLibCache.get(ps.packageName);
            if (sharedLibraryFiles == null) {
                PackageManager hostPM = VirtualCore.get().getUnHookPackageManager();
                try {
                    ApplicationInfo hostInfo = hostPM.getApplicationInfo(ps.packageName, PackageManager.GET_SHARED_LIBRARY_FILES);
                    sharedLibraryFiles = hostInfo.sharedLibraryFiles;
                    if (sharedLibraryFiles == null) sharedLibraryFiles = new String[0];
                    sSharedLibCache.put(ps.packageName, sharedLibraryFiles);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            ai.sharedLibraryFiles = sharedLibraryFiles;
        }
    }

    private static void initApplicationAsUser(ApplicationInfo ai, int userId) {
        ai.dataDir = VEnvironment.getDataUserPackageDirectory(userId, ai.packageName).getPath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ApplicationInfoL.scanSourceDir.set(ai, ai.dataDir);
            ApplicationInfoL.scanPublicSourceDir.set(ai, ai.dataDir);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(Build.VERSION.SDK_INT < 26) {
                ApplicationInfoN.deviceEncryptedDataDir.set(ai, ai.dataDir);
                ApplicationInfoN.credentialEncryptedDataDir.set(ai, ai.dataDir);
            }
            ApplicationInfoN.deviceProtectedDataDir.set(ai, ai.dataDir);
            ApplicationInfoN.credentialProtectedDataDir.set(ai, ai.dataDir);
        }
    }

    private static void addOwner(VPackage p) {
        for (VPackage.ActivityComponent activity : p.activities) {
            activity.owner = p;
            for (VPackage.ActivityIntentInfo info : activity.intents) {
                info.activity = activity;
            }
        }
        for (VPackage.ServiceComponent service : p.services) {
            service.owner = p;
            for (VPackage.ServiceIntentInfo info : service.intents) {
                info.service = service;
            }
        }
        for (VPackage.ActivityComponent receiver : p.receivers) {
            receiver.owner = p;
            for (VPackage.ActivityIntentInfo info : receiver.intents) {
                info.activity = receiver;
            }
        }
        for (VPackage.ProviderComponent provider : p.providers) {
            provider.owner = p;
            for (VPackage.ProviderIntentInfo info : provider.intents) {
                info.provider = provider;
            }
        }
        for (VPackage.InstrumentationComponent instrumentation : p.instrumentation) {
            instrumentation.owner = p;
        }
        for (VPackage.PermissionComponent permission : p.permissions) {
            permission.owner = p;
        }
        for (VPackage.PermissionGroupComponent group : p.permissionGroups) {
            group.owner = p;
        }
    }

    public static PackageInfo generatePackageInfo(VPackage p, int flags, long firstInstallTime, long lastUpdateTime, PackageUserState state, int userId) {
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }
        if (p.mSignatures == null) {
            readSignature(p);
        }
        PackageInfo pi = new PackageInfo();
        pi.packageName = p.packageName;
        pi.versionCode = p.mVersionCode;
        pi.sharedUserLabel = p.mSharedUserLabel;
        pi.versionName = p.mVersionName;
        pi.sharedUserId = p.mSharedUserId;
        pi.sharedUserLabel = p.mSharedUserLabel;
        pi.applicationInfo = generateApplicationInfo(p, flags, state, userId);
        pi.firstInstallTime = firstInstallTime;
        pi.lastUpdateTime = lastUpdateTime;
        if (p.requestedPermissions != null && !p.requestedPermissions.isEmpty()) {
            String[] requestedPermissions = new String[p.requestedPermissions.size()];
            p.requestedPermissions.toArray(requestedPermissions);
            pi.requestedPermissions = requestedPermissions;
        }
        if ((flags & PackageManager.GET_GIDS) != 0) {
            pi.gids = PackageParserCompat.GIDS;
        }
        if ((flags & PackageManager.GET_CONFIGURATIONS) != 0) {
            int N = p.configPreferences != null ? p.configPreferences.size() : 0;
            if (N > 0) {
                pi.configPreferences = new ConfigurationInfo[N];
                p.configPreferences.toArray(pi.configPreferences);
            }
            N = p.reqFeatures != null ? p.reqFeatures.size() : 0;
            if (N > 0) {
                pi.reqFeatures = new FeatureInfo[N];
                p.reqFeatures.toArray(pi.reqFeatures);
            }
        }
        if ((flags & PackageManager.GET_ACTIVITIES) != 0) {
            final int N = p.activities.size();
            if (N > 0) {
                int num = 0;
                final ActivityInfo[] res = new ActivityInfo[N];
                for (int i = 0; i < N; i++) {
                    final VPackage.ActivityComponent a = p.activities.get(i);
                    res[num++] = generateActivityInfo(a, flags, state, userId);
                }
                pi.activities = res;
            }
        }
        if ((flags & PackageManager.GET_RECEIVERS) != 0) {
            final int N = p.receivers.size();
            if (N > 0) {
                int num = 0;
                final ActivityInfo[] res = new ActivityInfo[N];
                for (int i = 0; i < N; i++) {
                    final VPackage.ActivityComponent a = p.receivers.get(i);
                    res[num++] = generateActivityInfo(a, flags, state, userId);
                }
                pi.receivers = res;
            }
        }
        if ((flags & PackageManager.GET_SERVICES) != 0) {
            final int N = p.services.size();
            if (N > 0) {
                int num = 0;
                final ServiceInfo[] res = new ServiceInfo[N];
                for (int i = 0; i < N; i++) {
                    final VPackage.ServiceComponent s = p.services.get(i);
                    res[num++] = generateServiceInfo(s, flags, state, userId);
                }
                pi.services = res;
            }
        }
        if ((flags & PackageManager.GET_PROVIDERS) != 0) {
            final int N = p.providers.size();
            if (N > 0) {
                int num = 0;
                final ProviderInfo[] res = new ProviderInfo[N];
                for (int i = 0; i < N; i++) {
                    final VPackage.ProviderComponent pr = p.providers.get(i);
                    res[num++] = generateProviderInfo(pr, flags, state, userId);
                }
                pi.providers = res;
            }
        }
        if ((flags & PackageManager.GET_INSTRUMENTATION) != 0) {
            int N = p.instrumentation.size();
            if (N > 0) {
                pi.instrumentation = new InstrumentationInfo[N];
                for (int i = 0; i < N; i++) {
                    pi.instrumentation[i] = generateInstrumentationInfo(
                            p.instrumentation.get(i), flags);
                }
            }
        }
        if ((flags & PackageManager.GET_SIGNATURES) != 0) {
            int N = (p.mSignatures != null) ? p.mSignatures.length : 0;
            if (N > 0) {
                pi.signatures = new Signature[N];
                System.arraycopy(p.mSignatures, 0, pi.signatures, 0, N);
            }
        }
        return pi;
    }

    public static ApplicationInfo generateApplicationInfo(VPackage p, int flags,
                                                          PackageUserState state, int userId) {
        if (p == null) return null;
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }

        // Make shallow copy so we can store the metadata/libraries safely
        ApplicationInfo ai = new ApplicationInfo(p.applicationInfo);
        if ((flags & PackageManager.GET_META_DATA) != 0) {
            ai.metaData = p.mAppMetaData;
        }
        initApplicationAsUser(ai, userId);
        return ai;
    }


    public static ActivityInfo generateActivityInfo(VPackage.ActivityComponent a, int flags,
                                                    PackageUserState state, int userId) {
        if (a == null) return null;
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }
        // Make shallow copies so we can store the metadata safely
        ActivityInfo ai = new ActivityInfo(a.info);
        if ((flags & PackageManager.GET_META_DATA) != 0
                && (a.metaData != null)) {
            ai.metaData = a.metaData;
        }
        ai.applicationInfo = generateApplicationInfo(a.owner, flags, state, userId);
        return ai;
    }

    public static ServiceInfo generateServiceInfo(VPackage.ServiceComponent s, int flags,
                                                  PackageUserState state, int userId) {
        if (s == null) return null;
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }
        ServiceInfo si = new ServiceInfo(s.info);
        // Make shallow copies so we can store the metadata safely
        if ((flags & PackageManager.GET_META_DATA) != 0 && s.metaData != null) {
            si.metaData = s.metaData;
        }
        si.applicationInfo = generateApplicationInfo(s.owner, flags, state, userId);
        return si;
    }

    public static ProviderInfo generateProviderInfo(VPackage.ProviderComponent p, int flags,
                                                    PackageUserState state, int userId) {
        if (p == null) return null;
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }
        // Make shallow copies so we can store the metadata safely
        ProviderInfo pi = new ProviderInfo(p.info);
        if ((flags & PackageManager.GET_META_DATA) != 0
                && (p.metaData != null)) {
            pi.metaData = p.metaData;
        }

        if ((flags & PackageManager.GET_URI_PERMISSION_PATTERNS) == 0) {
            pi.uriPermissionPatterns = null;
        }
        pi.applicationInfo = generateApplicationInfo(p.owner, flags, state, userId);
        return pi;
    }

    public static InstrumentationInfo generateInstrumentationInfo(
            VPackage.InstrumentationComponent i, int flags) {
        if (i == null) return null;
        if ((flags & PackageManager.GET_META_DATA) == 0) {
            return i.info;
        }
        InstrumentationInfo ii = new InstrumentationInfo(i.info);
        ii.metaData = i.metaData;
        return ii;
    }

    public static PermissionInfo generatePermissionInfo(
            VPackage.PermissionComponent p, int flags) {
        if (p == null) return null;
        if ((flags & PackageManager.GET_META_DATA) == 0) {
            return p.info;
        }
        PermissionInfo pi = new PermissionInfo(p.info);
        pi.metaData = p.metaData;
        return pi;
    }

    public static PermissionGroupInfo generatePermissionGroupInfo(
            VPackage.PermissionGroupComponent pg, int flags) {
        if (pg == null) return null;
        if ((flags & PackageManager.GET_META_DATA) == 0) {
            return pg.info;
        }
        PermissionGroupInfo pgi = new PermissionGroupInfo(pg.info);
        pgi.metaData = pg.metaData;
        return pgi;
    }

    private static boolean checkUseInstalledOrHidden(PackageUserState state, int flags) {
        //noinspection deprecation
        return (state.installed && !state.hidden)
                || (flags & PackageManager.GET_UNINSTALLED_PACKAGES) != 0;
    }

}
