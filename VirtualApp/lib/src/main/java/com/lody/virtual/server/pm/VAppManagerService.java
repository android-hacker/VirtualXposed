package com.lody.virtual.server.pm;

import android.content.Intent;
import android.content.pm.PackageParser;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.collection.IntArray;
import com.lody.virtual.helper.compat.NativeLibraryHelperCompat;
import com.lody.virtual.helper.compat.PackageParserCompat;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;
import com.lody.virtual.server.IAppManager;
import com.lody.virtual.server.accounts.VAccountManagerService;
import com.lody.virtual.server.am.BroadcastSystem;
import com.lody.virtual.server.am.UidSystem;
import com.lody.virtual.server.am.VActivityManagerService;
import com.lody.virtual.server.interfaces.IAppObserver;
import com.lody.virtual.server.interfaces.IAppRequestListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Lody
 */
public class VAppManagerService extends IAppManager.Stub {

    private static final String TAG = VAppManagerService.class.getSimpleName();
    private static final AtomicReference<VAppManagerService> sService = new AtomicReference<>();
    private final UidSystem mUidSystem = new UidSystem();
    private final PackagePersistenceLayer mPersistenceLayer = new PackagePersistenceLayer(this);
    private boolean isBooting;
    private RemoteCallbackList<IAppObserver> mRemoteCallbackList = new RemoteCallbackList<IAppObserver>();

    private IAppRequestListener listener;

    public static VAppManagerService get() {
        return sService.get();
    }

    public static void systemReady() {
        VEnvironment.systemReady();
        VAppManagerService instance = new VAppManagerService();
        instance.mUidSystem.initUidList();
        sService.set(instance);
    }

    public boolean isBooting() {
        return isBooting;
    }

    @Override
    public void scanApps() {
        isBooting = true;
        mPersistenceLayer.read();
        isBooting = false;
    }

    private void cleanUpResidualFiles(PackageSetting setting) {
        File dataAppDir = VEnvironment.getDataAppPackageDirectory(setting.packageName);
        FileUtils.deleteDir(dataAppDir);
        for (int userId : VUserManagerService.get().getUserIds()) {
            FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory(userId, setting.packageName));
        }
    }


    synchronized void loadPackage(PackageSetting setting) {
        if (!loadPackageInnerLocked(setting)) {
            cleanUpResidualFiles(setting);
        }
    }

    private boolean loadPackageInnerLocked(PackageSetting setting) {
        if (setting.dependSystem) {
            if (!VirtualCore.get().isOutsideInstalled(setting.packageName)) {
                return false;
            }
        }
        File packageFile = new File(setting.apkPath);
        PackageParser parser = PackageParserCompat.createParser(packageFile);
        PackageParser.Package pkg = null;
        try {
            pkg = PackageParserCompat.parsePackage(parser, packageFile, 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (parser == null || pkg == null || pkg.packageName == null) {
            return false;
        }
        chmodPackageDictionary(packageFile);
        setting.parser = parser;
        PackageCache.put(pkg, setting);
        BroadcastSystem.get().startApp(pkg);
        return true;
    }

    @Override
    public synchronized InstallResult installPackage(String path, int flags) {
        long installTime = System.currentTimeMillis();
        if (path == null) {
            return InstallResult.makeFailure("path = NULL");
        }
        File packageFile = new File(path);
        if (!packageFile.exists() || !packageFile.isFile()) {
            return InstallResult.makeFailure("Package File is not exist.");
        }
        PackageParser parser = PackageParserCompat.createParser(packageFile);
        PackageParser.Package pkg = null;
        try {
            pkg = PackageParserCompat.parsePackage(parser, packageFile, 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (parser == null || pkg == null || pkg.packageName == null) {
            return InstallResult.makeFailure("Unable to parse the package.");
        }
        InstallResult res = new InstallResult();
        res.packageName = pkg.packageName;
        // PackageCache holds all packages, try to check if we need to update.
        PackageParser.Package existOne = PackageCache.get(pkg.packageName);
        PackageSetting existSetting = existOne != null ? (PackageSetting) existOne.mExtras : null;
        if (existOne != null) {
            if ((flags & InstallStrategy.IGNORE_NEW_VERSION) != 0) {
                res.isUpdate = true;
                return res;
            }
            if (!canUpdate(existOne, pkg, flags)) {
                return InstallResult.makeFailure("Not allowed to update the package.");
            }
            res.isUpdate = true;
        }
        File appDir = VEnvironment.getDataAppPackageDirectory(pkg.packageName);
        File libDir = new File(appDir, "lib");
        if (res.isUpdate) {
            FileUtils.deleteDir(libDir);
            VEnvironment.getOdexFile(pkg.packageName).delete();
            VActivityManagerService.get().killAppByPkg(pkg.packageName, VUserHandle.USER_ALL);
        }
        if (!libDir.exists() && !libDir.mkdirs()) {
            return InstallResult.makeFailure("Unable to create lib dir.");
        }
        boolean dependSystem = (flags & InstallStrategy.DEPEND_SYSTEM_IF_EXIST) != 0
                && VirtualCore.get().isOutsideInstalled(pkg.packageName);

        if (existSetting != null && existSetting.dependSystem) {
            dependSystem = false;
        }

        NativeLibraryHelperCompat.copyNativeBinaries(new File(path), libDir);
        if (!dependSystem) {
            File privatePackageFile = new File(appDir, "base.apk");
            File parentFolder = privatePackageFile.getParentFile();
            if (!parentFolder.exists() && !parentFolder.mkdirs()) {
                VLog.w(TAG, "Warning: unable to create folder : " + privatePackageFile.getPath());
            } else if (privatePackageFile.exists() && !privatePackageFile.delete()) {
                VLog.w(TAG, "Warning: unable to delete file : " + privatePackageFile.getPath());
            }
            try {
                FileUtils.copyFile(packageFile, privatePackageFile);
            } catch (IOException e) {
                privatePackageFile.delete();
                return InstallResult.makeFailure("Unable to copy the package file.");
            }
            packageFile = privatePackageFile;
        }
        if (existOne != null) {
            PackageCache.remove(pkg.packageName);
        }
        chmodPackageDictionary(packageFile);
        PackageSetting setting;
        if (existSetting != null) {
            setting = existSetting;
        } else {
            setting = new PackageSetting();
        }
        setting.parser = parser;
        setting.dependSystem = dependSystem;
        setting.apkPath = packageFile.getPath();
        setting.libPath = libDir.getPath();
        setting.packageName = pkg.packageName;
        setting.appId = VUserHandle.getAppId(mUidSystem.getOrCreateUid(pkg));
        if (res.isUpdate) {
            setting.lastUpdateTime = installTime;
        } else {
            setting.firstInstallTime = installTime;
            setting.lastUpdateTime = installTime;
            for (int userId : VUserManagerService.get().getUserIds()) {
                boolean installed = userId == 0;
                setting.setUserState(userId, false/*launched*/, false/*hidden*/, installed);
            }
        }
        PackageCache.put(pkg, setting);
        mPersistenceLayer.save();
        BroadcastSystem.get().startApp(pkg);
        notifyAppInstalled(setting);
        res.isSuccess = true;
        return res;
    }


    @Override
    public synchronized boolean installPackageAsUser(int userId, String packageName) {
        if (VUserManagerService.get().exists(userId)) {
            PackageSetting setting = PackageCache.getSetting(packageName);
            if (setting != null) {
                if (!setting.isInstalled(userId)) {
                    setting.setInstalled(userId, true);
                    mPersistenceLayer.save();
                    return true;
                }
            }
        }
        return false;
    }

    private void chmodPackageDictionary(File packageFile) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (FileUtils.isSymlink(packageFile)) {
                    return;
                }
                FileUtils.chmod(packageFile.getParentFile().getAbsolutePath(), FileUtils.FileMode.MODE_755);
                FileUtils.chmod(packageFile.getAbsolutePath(), FileUtils.FileMode.MODE_755);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean canUpdate(PackageParser.Package existOne, PackageParser.Package newOne, int flags) {
        if ((flags & InstallStrategy.COMPARE_VERSION) != 0) {
            if (existOne.mVersionCode < newOne.mVersionCode) {
                return true;
            }
        }
        if ((flags & InstallStrategy.TERMINATE_IF_EXIST) != 0) {
            return false;
        }
        if ((flags & InstallStrategy.UPDATE_IF_EXIST) != 0) {
            return true;
        }
        return false;
    }


    @Override
    public boolean uninstallPackage(String packageName, int userId) {
        if (!VUserManagerService.get().exists(userId)) {
            return false;
        }
        synchronized (PackageCache.PACKAGE_CACHE) {
            PackageSetting setting = PackageCache.getSetting(packageName);
            if (setting != null) {
                if (userId == 0) {
                    try {
                        BroadcastSystem.get().stopApp(packageName);
                        VActivityManagerService.get().killAppByPkg(packageName, VUserHandle.USER_ALL);
                        VEnvironment.getPackageResourcePath(packageName).delete();
                        FileUtils.deleteDir(VEnvironment.getDataAppPackageDirectory(packageName));
                        VEnvironment.getOdexFile(packageName).delete();
                        for (int id : VUserManagerService.get().getUserIds()) {
                            FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory(id, packageName));
                        }
                        PackageCache.remove(packageName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        notifyAppUninstalled(setting);
                    }
                    return true;
                } else {
                    setting.setInstalled(userId, false);
                    FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory(userId, packageName));
                }
            }
        }
        return false;
    }

    @Override
    public int[] getPackageInstalledUsers(String packageName) {
        PackageSetting ps = PackageCache.getSetting(packageName);
        if (ps != null) {
            IntArray installedUsers = new IntArray(5);
            int[] userIds = VUserManagerService.get().getUserIds();
            for (int userId : userIds) {
                if (ps.readUserState(userId).installed) {
                    installedUsers.add(userId);
                }
            }
            return installedUsers.getAll();
        }
        return new int[0];
    }

    @Override
    public List<InstalledAppInfo> getInstalledApps(int flags) {
        List<InstalledAppInfo> infoList = new ArrayList<>(getInstalledAppCount());
        for (PackageParser.Package p : PackageCache.PACKAGE_CACHE.values()) {
            PackageSetting setting = (PackageSetting) p.mExtras;
            infoList.add(setting.getAppInfo(flags));
        }
        return infoList;
    }

    @Override
    public List<InstalledAppInfo> getInstalledAppsAsUser(int userId, int flags) {
        List<InstalledAppInfo> infoList = new ArrayList<>(getInstalledAppCount());
        for (PackageParser.Package p : PackageCache.PACKAGE_CACHE.values()) {
            PackageSetting setting = (PackageSetting) p.mExtras;
            boolean visible = setting.isInstalled(userId);
            if ((flags & VirtualCore.GET_HIDDEN_APP) == 0 && setting.isHidden(userId)) {
                visible = false;
            }
            if (visible) {
                infoList.add(setting.getAppInfo(flags));
            }
        }
        return infoList;
    }

    @Override
    public int getInstalledAppCount() {
        return PackageCache.PACKAGE_CACHE.size();
    }

    @Override
    public boolean isAppInstalled(String packageName) {
        return packageName != null && PackageCache.PACKAGE_CACHE.containsKey(packageName);
    }

    @Override
    public boolean isAppInstalledAsUser(int userId, String packageName) {
        if (packageName == null || !VUserManagerService.get().exists(userId)) {
            return false;
        }
        PackageSetting setting = PackageCache.getSetting(packageName);
        if (setting == null) {
            return false;
        }
        return setting.isInstalled(userId);
    }

    private void notifyAppInstalled(PackageSetting setting) {
        int N = mRemoteCallbackList.beginBroadcast();
        while (N-- > 0) {
            try {
                mRemoteCallbackList.getBroadcastItem(N).onNewApp(setting.packageName);
            } catch (RemoteException e) {
                // Ignore
            }
        }
        mRemoteCallbackList.finishBroadcast();
        Intent virtualIntent = new Intent(Constants.ACTION_PACKAGE_ADDED);
        Uri uri = Uri.fromParts("package", setting.packageName, null);
        virtualIntent.setData(uri);
        for (int userId : VUserManagerService.get().getUserIds()) {
            Intent intent = new Intent(virtualIntent);
            intent.putExtra(Intent.EXTRA_UID, VUserHandle.getUid(userId, setting.appId));
            VirtualCore.get().getContext().sendBroadcast(virtualIntent);
        }
        VAccountManagerService.get().refreshAuthenticatorCache(null);
    }

    private void notifyAppUninstalled(PackageSetting setting) {
        int N = mRemoteCallbackList.beginBroadcast();
        while (N-- > 0) {
            try {
                mRemoteCallbackList.getBroadcastItem(N).onRemoveApp(setting.packageName);
            } catch (RemoteException e) {
                // Ignore
            }
        }
        mRemoteCallbackList.finishBroadcast();
        Intent virtualIntent = new Intent(Constants.ACTION_PACKAGE_REMOVED);
        Uri uri = Uri.fromParts("package", setting.packageName, null);
        virtualIntent.setData(uri);
        for (int userId : VUserManagerService.get().getUserIds()) {
            Intent intent = new Intent(virtualIntent);
            intent.putExtra(Intent.EXTRA_UID, VUserHandle.getUid(userId, setting.appId));
            VirtualCore.get().getContext().sendBroadcast(virtualIntent);
        }
        VAccountManagerService.get().refreshAuthenticatorCache(null);
    }

    @Override
    public void registerObserver(IAppObserver observer) {
        try {
            mRemoteCallbackList.register(observer);
        } catch (Throwable e) {
            // Ignore
        }
    }

    @Override
    public void unregisterObserver(IAppObserver observer) {
        try {
            mRemoteCallbackList.unregister(observer);
        } catch (Throwable e) {
            // Ignore
        }
    }

    @Override
    public IAppRequestListener getAppRequestListener() {
        return listener;
    }

    @Override
    public void setAppRequestListener(final IAppRequestListener listener) {
        this.listener = listener;
        if (listener != null) {
            try {
                listener.asBinder().linkToDeath(new DeathRecipient() {
                    @Override
                    public void binderDied() {
                        listener.asBinder().unlinkToDeath(this, 0);
                        VAppManagerService.this.listener = null;
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void clearAppRequestListener() {
        this.listener = null;
    }

    @Override
    public InstalledAppInfo getInstalledAppInfo(String packageName, int flags) {
        synchronized (PackageCache.class) {
            if (packageName != null) {
                PackageSetting setting = PackageCache.getSetting(packageName);
                if (setting != null) {
                    return setting.getAppInfo(flags);
                }
            }
            return null;
        }
    }

    public boolean isPackageLaunched(int userId, String packageName) {
        PackageSetting ps = PackageCache.getSetting(packageName);
        return ps != null && ps.isLaunched(userId);
    }

    public void setPackageHidden(int userId, String packageName, boolean hidden) {
        PackageSetting ps = PackageCache.getSetting(packageName);
        if (ps != null && VUserManagerService.get().exists(userId)) {
            ps.setHidden(userId, hidden);
            mPersistenceLayer.save();
        }
    }

    public int getAppId(String packageName) {
        PackageSetting setting = PackageCache.getSetting(packageName);
        return setting != null ? setting.appId : -1;
    }


    void restoreFactoryState() {
        VLog.w(TAG, "Warning: Restore the factory state...");
        VEnvironment.getDalvikCacheDirectory().delete();
        VEnvironment.getUserSystemDirectory().delete();
        VEnvironment.getDataAppDirectory().delete();
    }

    public void savePersistenceData() {
        mPersistenceLayer.save();
    }
}
