package io.virtualapp.sys;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.lody.virtual.GmsSupport;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.DeviceUtil;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.XApp;
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

        void fail(String msg);
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
                    pkgInfo = XApp.getApp().getPackageManager().getPackageArchiveInfo(info.path, 0);
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
                    throw new IllegalStateException(res.error);
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
        }).fail(result -> {
            if (refreshListener != null) {
                refreshListener.fail(result.getMessage());

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

    private static ArrayList<AppInfoLite> getAppInfoLiteFromPath(Context context, String path) {
        if (context == null) {
            return null;
        }
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = context.getPackageManager().getPackageArchiveInfo(path, PackageManager.GET_META_DATA);
            pkgInfo.applicationInfo.sourceDir = path;
            pkgInfo.applicationInfo.publicSourceDir = path;
        } catch (Exception e) {
            // Ignore
        }
        if (pkgInfo == null) {
            return null;
        }

        if (TextUtils.equals(VirtualCore.TAICHI_PACKAGE, pkgInfo.packageName)) {
            return null;
        }

        if (VirtualCore.get().getHostPkg().equals(pkgInfo.packageName)) {
            Toast.makeText(VirtualCore.get().getContext(), R.string.install_self_eggs, Toast.LENGTH_SHORT).show();
            return null;
        }

        boolean isXposed = pkgInfo.applicationInfo.metaData != null
                && pkgInfo.applicationInfo.metaData.containsKey("xposedmodule");
        AppInfoLite appInfoLite = new AppInfoLite(pkgInfo.packageName, path, false, isXposed);
        ArrayList<AppInfoLite> dataList = new ArrayList<>();
        dataList.add(appInfoLite);
        return dataList;
    }

    public static void handleRequestFromFile(Context context, String path) {

        ArrayList<AppInfoLite> dataList = getAppInfoLiteFromPath(context, path);
        if (dataList == null) {
            return;
        }
        startInstallerActivity(context, dataList);
    }

    public static void startInstallerActivity(Context context, ArrayList<AppInfoLite> data) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, InstallerActivity.class);
        intent.putParcelableArrayListExtra(VCommends.EXTRA_APP_INFO_LIST, data);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void addGmsSupport() {
        List<String> gApps = new ArrayList<>();
        gApps.addAll(GmsSupport.GOOGLE_APP);
        gApps.addAll(GmsSupport.GOOGLE_SERVICE);

        VirtualCore core = VirtualCore.get();
        final int userId = 0;

        ArrayList<AppInfoLite> toInstalled = new ArrayList<>();
        for (String packageName : gApps) {
            if (core.isAppInstalledAsUser(userId, packageName)) {
                continue;
            }
            ApplicationInfo info = null;
            try {
                info = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
            if (info == null || info.sourceDir == null) {
                continue;
            }

            AppInfoLite lite = new AppInfoLite(info.packageName, info.sourceDir, false, true);
            toInstalled.add(lite);
        }
        startInstallerActivity(VirtualCore.get().getContext(), toInstalled);
    }
}
