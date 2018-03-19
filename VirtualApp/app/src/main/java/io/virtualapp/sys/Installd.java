package io.virtualapp.sys;

import android.content.pm.PackageInfo;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.DeviceUtil;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;

import java.io.IOException;

import io.virtualapp.VApp;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.home.models.MultiplePackageAppData;
import io.virtualapp.home.models.PackageAppData;
import io.virtualapp.home.repo.PackageAppDataStorage;

/**
 * author: weishu on 18/3/19.
 */
public class Installd {

    public interface UpdateListener {
        void update(AppData model);
    }

    public static void addApp(AppInfoLite info, UpdateListener refreshListener) {
        class AddResult {
            private PackageAppData appData;
            private int userId;
            private boolean justEnableHidden;
        }
        AddResult addResult = new AddResult();
        VUiKit.defer().when(() -> {
            InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(info.packageName, 0);
            addResult.justEnableHidden = installedAppInfo != null;

            if (info.disableMultiVersion) {
                addResult.justEnableHidden = false;
            }
            if (addResult.justEnableHidden) {
                int[] userIds = installedAppInfo.getInstalledUsers();
                int nextUserId = userIds.length;
                /*
                  Input : userIds = {0, 1, 3}
                  Output: nextUserId = 2
                 */
                for (int i = 0; i < userIds.length; i++) {
                    if (userIds[i] != i) {
                        nextUserId = i;
                        break;
                    }
                }
                addResult.userId = nextUserId;

                if (VUserManager.get().getUserInfo(nextUserId) == null) {
                    // user not exist, create it automatically.
                    String nextUserName = "Space " + (nextUserId + 1);
                    VUserInfo newUserInfo = VUserManager.get().createUser(nextUserName, VUserInfo.FLAG_ADMIN);
                    if (newUserInfo == null) {
                        throw new IllegalStateException();
                    }
                }
                boolean success = VirtualCore.get().installPackageAsUser(nextUserId, info.packageName);
                if (!success) {
                    throw new IllegalStateException();
                }
            } else {
                PackageInfo pkgInfo = null;
                try {
                    pkgInfo = VApp.getApp().getPackageManager().getPackageArchiveInfo(info.path, 0);
                    pkgInfo.applicationInfo.sourceDir = info.path;
                    pkgInfo.applicationInfo.publicSourceDir = info.path;
                } catch (Exception e) {
                }
                if(pkgInfo != null) {
                    PackageAppData data = PackageAppDataStorage.get().acquire(pkgInfo.applicationInfo);
                    addResult.appData = data;
                    data.isInstalling = true;
                    data.isFirstOpen = false;
                    if (refreshListener != null) {
                        refreshListener.update(data);
                    }
                }

                InstallResult res = addVirtualApp(info);
                if (!res.isSuccess) {
                    if (addResult.appData != null) {
                        // mView.removeAppToLauncher(addResult.appData);
                    }
                    throw new IllegalStateException();
                }
            }
        }).then((res) -> {
            if (addResult.appData == null) {
                addResult.appData = PackageAppDataStorage.get().acquire(info.packageName);
            }
        }).done(res -> {
            boolean multipleVersion = addResult.justEnableHidden && addResult.userId != 0;
            if (!multipleVersion) {
                PackageAppData data = addResult.appData;
                data.isInstalling = false;
                data.isLoading = true;

                if (refreshListener != null) {
                    refreshListener.update(data);
                }
                handleOptApp(data, info.packageName, true, refreshListener);
            } else {
                MultiplePackageAppData data = new MultiplePackageAppData(addResult.appData, addResult.userId);
                data.isInstalling = false;
                data.isLoading = true;

                if (refreshListener != null) {
                    refreshListener.update(data);
                }
                handleOptApp(data, info.packageName, false, refreshListener);
            }
        });
    }


    private static void handleOptApp(AppData data, String packageName, boolean needOpt, UpdateListener refreshListener) {
        VUiKit.defer().when(() -> {
            long time = System.currentTimeMillis();
            if (needOpt) {
                try {
                    VirtualCore.get().preOpt(packageName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            time = System.currentTimeMillis() - time;
            if (time < 1500L) {
                try {
                    Thread.sleep(1500L - time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).done((res) -> {
            if (data instanceof PackageAppData) {
                ((PackageAppData) data).isLoading = false;
                ((PackageAppData) data).isFirstOpen = true;
            } else if (data instanceof MultiplePackageAppData) {
                ((MultiplePackageAppData) data).isLoading = false;
                ((MultiplePackageAppData) data).isFirstOpen = true;
            }
            if (refreshListener != null) {
                refreshListener.update(data);
            }
        });
    }

    public static InstallResult addVirtualApp(AppInfoLite info) {
        int flags = InstallStrategy.COMPARE_VERSION | InstallStrategy.SKIP_DEX_OPT;
        info.fastOpen = false; // disable fast open for compile.
        if (DeviceUtil.isMeizuBelowN()) {
            info.fastOpen = true;
        }
        if (info.fastOpen) {
            flags |= InstallStrategy.DEPEND_SYSTEM_IF_EXIST;
        }
        if (info.disableMultiVersion) {
            flags |= InstallStrategy.UPDATE_IF_EXIST;
        }
        return VirtualCore.get().installPackage(info.path, flags);
    }
}
