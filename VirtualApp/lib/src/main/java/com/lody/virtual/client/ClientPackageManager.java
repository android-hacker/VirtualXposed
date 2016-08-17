package com.lody.virtual.client;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PackageInstallObserver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ContainerEncryptionParams;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.KeySet;
import android.content.pm.ManifestDigest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.VerificationParams;
import android.content.pm.VerifierDeviceIdentity;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.ExtraConstants;

import java.util.List;

/**
 * @author Lody
 */

public class ClientPackageManager extends PackageManager {

    private final int mUserId;
    private Context mContext;
    private VPackageManager mPM;
    private PackageManager mHostPM;


    public ClientPackageManager(int mUserId) {
        this.mUserId = mUserId;
        this.mContext = VirtualCore.getCore().getContext();
        mPM = VPackageManager.getInstance();
        mHostPM = VirtualCore.getPM();
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        return mPM.getPackageInfo(packageName, flags, mUserId);
    }

    @Override
    public String[] currentToCanonicalPackageNames(String[] names) {
        return mHostPM.currentToCanonicalPackageNames(names);
    }

    @Override
    public String[] canonicalToCurrentPackageNames(String[] names) {
        return mHostPM.canonicalToCurrentPackageNames(names);
    }

    @Override
    public Intent getLaunchIntentForPackage(String packageName) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = mPM.queryIntentActivities(intentToResolve, intentToResolve.resolveType(mContext), 0, mUserId);

        // Otherwise, try to find a main launcher activity.
        if (ris == null || ris.size() <= 0) {
            // reuse the intent instance
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = mPM.queryIntentActivities(intentToResolve, intentToResolve.resolveType(mContext), 0, mUserId);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName,
                ris.get(0).activityInfo.name);
        intent.putExtra(ExtraConstants.EXTRA_TARGET_USER, mUserId);
        return intent;
    }

    @Override
    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        return null;
    }

    @Override
    public int[] getPackageGids(String packageName) throws NameNotFoundException {
        return new int[0];
    }

    @SuppressLint("Override")
    @TargetApi(24)
    public int getPackageUid(String packageName, int userHandle) throws NameNotFoundException {
        return mPM.getPackageUid(packageName, userHandle);
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
        return mPM.getPermissionInfo(name, flags);
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
        return mPM.queryPermissionsByGroup(group, flags);
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
        return mPM.getPermissionGroupInfo(name, flags);
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        return mPM.getAllPermissionGroups(flags);
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
        return mPM.getApplicationInfo(packageName, flags, mUserId);
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException {
        return mPM.getActivityInfo(component, flags, mUserId);
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName component, int flags) throws NameNotFoundException {
        return mPM.getReceiverInfo(component, flags, mUserId);
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName component, int flags) throws NameNotFoundException {
        return mPM.getServiceInfo(component, flags, mUserId);
    }

    @Override
    public ProviderInfo getProviderInfo(ComponentName component, int flags) throws NameNotFoundException {
        return mPM.getProviderInfo(component, flags, mUserId);
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        return mPM.getInstalledPackages(flags, mUserId);
    }

    @Override
    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return mHostPM.getPackagesHoldingPermissions(permissions, flags);
        }
        return null;
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        return mPM.getInstalledPackages(flags, userId);
    }

    @Override
    public int checkPermission(String permName, String pkgName) {
        return mPM.checkPermission(permName, pkgName, mUserId);
    }

    @Override
    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
        return false;
    }

    @Override
    public String getPermissionControllerPackageName() {
        return null;
    }

    @Override
    public boolean addPermission(PermissionInfo info) {
        return false;
    }

    @Override
    public boolean addPermissionAsync(PermissionInfo info) {
        return false;
    }

    @Override
    public void removePermission(String name) {

    }

    @Override
    public void grantRuntimePermission(String packageName, String permissionName, UserHandle user) {

    }

    @Override
    public void revokeRuntimePermission(String packageName, String permissionName, UserHandle user) {

    }

    @Override
    public int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
        return 0;
    }

    @Override
    public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, UserHandle user) {

    }

    @Override
    public boolean shouldShowRequestPermissionRationale(String permission) {
        return false;
    }

    @Override
    public int checkSignatures(String pkg1, String pkg2) {
        return 0;
    }

    @Override
    public int checkSignatures(int uid1, int uid2) {
        return 0;
    }

    @Override
    public String[] getPackagesForUid(int uid) {
        return mPM.getPackagesForUid(uid);
    }

    @Override
    public String getNameForUid(int uid) {
        return null;
    }

    @Override
    public int getUidForSharedUser(String sharedUserName) throws NameNotFoundException {
        return 0;
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) {
        return mPM.getInstalledApplications(flags, mUserId);
    }

    @Override
    public String[] getSystemSharedLibraryNames() {
        return new String[0];
    }

    @Override
    public FeatureInfo[] getSystemAvailableFeatures() {
        return new FeatureInfo[0];
    }

    @Override
    public boolean hasSystemFeature(String name) {
        return mHostPM.hasSystemFeature(name);
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        return mPM.resolveIntent(intent, intent.resolveType(mContext), flags, mUserId);
    }

    @Override
    public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
        return mPM.resolveIntent(intent, intent.resolveType(mContext), flags, userId);
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        return mPM.queryIntentActivities(intent, intent.resolveType(mContext), flags, mUserId);
    }

    @Override
    public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
        return mPM.queryIntentActivities(intent, intent.resolveType(mContext), flags, userId);
    }

    @Override
    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        return null;
    }

    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return null;
    }

    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, int userId) {
        return null;
    }

    @Override
    public ResolveInfo resolveService(Intent intent, int flags) {
        return mPM.resolveService(intent, intent.resolveType(mContext), flags, mUserId);
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return mPM.queryIntentServices(intent, intent.resolveType(mContext), flags, mUserId);
    }

    @Override
    public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
        return mPM.queryIntentActivities(intent, intent.resolveType(mContext), flags, userId);
    }

    @Override
    public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return null;
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) {
        return null;
    }

    @Override
    public ProviderInfo resolveContentProviderAsUser(String name, int flags, int userId) {
        return null;
    }

    @Override
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        return null;
    }

    @Override
    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
        return null;
    }

    @Override
    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        return null;
    }

    @Override
    public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
        return null;
    }

    @Override
    public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
        return null;
    }

    @Override
    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
        return null;
    }

    @Override
    public Drawable getDefaultActivityIcon() {
        return null;
    }

    @Override
    public Drawable getApplicationIcon(ApplicationInfo info) {
        return null;
    }

    @Override
    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Drawable getApplicationBanner(ApplicationInfo info) {
        return null;
    }

    @Override
    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
        return null;
    }

    @Override
    public Drawable getApplicationLogo(ApplicationInfo info) {
        return null;
    }

    @Override
    public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        return null;
    }

    @Override
    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        return null;
    }

    @Override
    public Drawable getUserBadgeForDensity(UserHandle user, int density) {
        return null;
    }

    @Override
    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        return null;
    }

    @Override
    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        return null;
    }

    @Override
    public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        return null;
    }

    @Override
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return null;
    }

    @Override
    public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
        return null;
    }

    @Override
    public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Resources getResourcesForApplicationAsUser(String appPackageName, int userId) throws NameNotFoundException {
        return null;
    }

    @Override
    public void installPackage(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName) {

    }

    @Override
    public void installPackageWithVerification(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName, Uri verificationURI, ManifestDigest manifestDigest, ContainerEncryptionParams encryptionParams) {

    }

    @Override
    public void installPackageWithVerificationAndEncryption(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName, VerificationParams verificationParams, ContainerEncryptionParams encryptionParams) {

    }

    @Override
    public void installPackage(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName) {

    }

    @Override
    public void installPackageWithVerification(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName, Uri verificationURI, ManifestDigest manifestDigest, ContainerEncryptionParams encryptionParams) {

    }

    @Override
    public void installPackageWithVerificationAndEncryption(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName, VerificationParams verificationParams, ContainerEncryptionParams encryptionParams) {

    }

    @Override
    public int installExistingPackage(String packageName) throws NameNotFoundException {
        return 0;
    }

    @Override
    public void verifyPendingInstall(int id, int verificationCode) {

    }

    @Override
    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {

    }

    @Override
    public void verifyIntentFilter(int verificationId, int verificationCode, List<String> outFailedDomains) {

    }

    @Override
    public int getIntentVerificationStatus(String packageName, int userId) {
        return 0;
    }

    @Override
    public boolean updateIntentVerificationStatus(String packageName, int status, int userId) {
        return false;
    }

    @Override
    public List<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
        return null;
    }

    @Override
    public List<IntentFilter> getAllIntentFilters(String packageName) {
        return null;
    }

    @Override
    public String getDefaultBrowserPackageName(int userId) {
        return null;
    }

    @Override
    public boolean setDefaultBrowserPackageName(String packageName, int userId) {
        return false;
    }

    @Override
    public void setInstallerPackageName(String targetPackage, String installerPackageName) {

    }

    @Override
    public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {

    }

    @Override
    public String getInstallerPackageName(String packageName) {
        return null;
    }

    @Override
    public void clearApplicationUserData(String packageName, IPackageDataObserver observer) {

    }

    @Override
    public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {

    }

    @Override
    public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, IPackageDataObserver observer) {

    }

    @Override
    public void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {

    }

    @Override
    public void getPackageSizeInfo(String packageName, int userHandle, IPackageStatsObserver observer) {

    }

    @Override
    public void addPackageToPreferred(String packageName) {

    }

    @Override
    public void removePackageFromPreferred(String packageName) {

    }

    @Override
    public List<PackageInfo> getPreferredPackages(int flags) {
        return null;
    }

    @Override
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {

    }

    @Override
    public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {

    }

    @Override
    public void clearPackagePreferredActivities(String packageName) {

    }

    @Override
    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        return 0;
    }

    @Override
    public ComponentName getHomeActivities(List<ResolveInfo> outActivities) {
        return null;
    }

    @Override
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {

    }

    @Override
    public int getComponentEnabledSetting(ComponentName componentName) {
        return 0;
    }

    @Override
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {

    }

    @Override
    public int getApplicationEnabledSetting(String packageName) {
        return 0;
    }

    @Override
    public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, UserHandle userHandle) {
        return false;
    }

    @Override
    public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle userHandle) {
        return false;
    }

    @Override
    public boolean isSafeMode() {
        return false;
    }

    @Override
    public void addOnPermissionsChangeListener(OnPermissionsChangedListener listener) {

    }

    @Override
    public void removeOnPermissionsChangeListener(OnPermissionsChangedListener listener) {

    }

    @Override
    public KeySet getKeySetByAlias(String packageName, String alias) {
        return null;
    }

    @Override
    public KeySet getSigningKeySet(String packageName) {
        return null;
    }

    @Override
    public boolean isSignedBy(String packageName, KeySet ks) {
        return false;
    }

    @Override
    public boolean isSignedByExactly(String packageName, KeySet ks) {
        return false;
    }

    @Override
    public int getMoveStatus(int moveId) {
        return 0;
    }

    @Override
    public void registerMoveCallback(MoveCallback callback, Handler handler) {

    }

    @Override
    public void unregisterMoveCallback(MoveCallback callback) {

    }

    @Override
    public int movePackage(String packageName, VolumeInfo vol) {
        return 0;
    }

    @Override
    public VolumeInfo getPackageCurrentVolume(ApplicationInfo app) {
        return null;
    }

    @Override
    public List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app) {
        return null;
    }

    @Override
    public int movePrimaryStorage(VolumeInfo vol) {
        return 0;
    }

    @Override
    public VolumeInfo getPrimaryStorageCurrentVolume() {
        return null;
    }

    @Override
    public List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
        return null;
    }

    @Override
    public VerifierDeviceIdentity getVerifierDeviceIdentity() {
        return null;
    }

    @Override
    public boolean isUpgrade() {
        return false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public PackageInstaller getPackageInstaller() {
        return null;
    }

    @Override
    public void addCrossProfileIntentFilter(IntentFilter filter, int sourceUserId, int targetUserId, int flags) {

    }

    @Override
    public void clearCrossProfileIntentFilters(int sourceUserId) {

    }

    @Override
    public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        return null;
    }

    @Override
    public Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        return null;
    }

    @Override
    public boolean isPackageAvailable(String packageName) {
        return true;
    }
}
