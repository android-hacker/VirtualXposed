package io.virtualapp.home.repo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.GmsSupport;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.DeviceUtil;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;

import org.jdeferred.Promise;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.AppInfo;
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.home.models.MultiplePackageAppData;
import io.virtualapp.home.models.PackageAppData;
import io.virtualapp.utils.HanziToPinyin;

/**
 * @author Lody
 */
public class AppRepository implements AppDataSource {

    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);
    private static final List<String> SCAN_PATH_LIST = Arrays.asList(
            ".",
            "wandoujia/app",
            "tencent/tassistant/apk",
            "BaiduAsa9103056",
            "360Download",
            "pp/downloader",
            "pp/downloader/apk",
            "pp/downloader/silent/apk");

    private static final int MAX_SCAN_DEPTH = 2;

    private Context mContext;

    public AppRepository(Context context) {
        mContext = context;
    }

    private static boolean isSystemApplication(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                && !GmsSupport.isGmsFamilyPackage(packageInfo.packageName);
    }

    @Override
    public Promise<List<AppData>, Throwable, Void> getVirtualApps() {
        return VUiKit.defer().when(() -> {
            List<InstalledAppInfo> infos = VirtualCore.get().getInstalledApps(0);
            List<AppData> models = new ArrayList<>();
            for (InstalledAppInfo info : infos) {
                if (!VirtualCore.get().isPackageLaunchable(info.packageName)) {
                    continue;
                }
                PackageAppData data = new PackageAppData(mContext, info);
                if (VirtualCore.get().isAppInstalledAsUser(0, info.packageName)) {
                    models.add(data);
                }
                int[] userIds = info.getInstalledUsers();
                for (int userId : userIds) {
                    if (userId != 0) {
                        models.add(new MultiplePackageAppData(data, userId));
                    }
                }
            }
            return models;
        });
    }

    @Override
    public Promise<List<AppInfo>, Throwable, Void> getInstalledApps(Context context) {
        return VUiKit.defer().when(() -> convertPackageInfoToAppData(context, context.getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA), true));
    }

    @Override
    public Promise<List<AppInfo>, Throwable, Void> getStorageApps(Context context, File rootDir) {
        // return VUiKit.defer().when(() -> convertPackageInfoToAppData(context, findAndParseAPKs(context, rootDir, SCAN_PATH_LIST), false));
        return VUiKit.defer().when(() -> convertPackageInfoToAppData(context, findAndParseApkRecursively(context, rootDir,null, 0), false));
    }

    private List<PackageInfo> findAndParseApkRecursively(Context context, File rootDir, List<PackageInfo> result, int depth) {
        if (result == null) {
            result = new ArrayList<>();
        }

        if (depth > MAX_SCAN_DEPTH) {
            return result;
        }

        File[] dirFiles = rootDir.listFiles();

        if (dirFiles == null) {
            return Collections.emptyList();
        }

        for (File f: dirFiles) {
            if (f.isDirectory()) {
                List<PackageInfo> andParseApkRecursively = findAndParseApkRecursively(context, f, new ArrayList<>(), depth + 1);
                result.addAll(andParseApkRecursively);
            }

            if (!(f.isFile() && f.getName().toLowerCase().endsWith(".apk"))) {
                continue;
            }

            PackageInfo pkgInfo = null;
            try {
                pkgInfo = context.getPackageManager().getPackageArchiveInfo(f.getAbsolutePath(), PackageManager.GET_META_DATA);
                pkgInfo.applicationInfo.sourceDir = f.getAbsolutePath();
                pkgInfo.applicationInfo.publicSourceDir = f.getAbsolutePath();
            } catch (Exception e) {
                // Ignore
            }
            if (pkgInfo != null) {
                result.add(pkgInfo);
            }
        }
        return result;
    }

    private List<PackageInfo> findAndParseAPKs(Context context, File rootDir, List<String> paths) {
        List<PackageInfo> packageList = new ArrayList<>();
        if (paths == null)
            return packageList;
        for (String path : paths) {
            File[] dirFiles = new File(rootDir, path).listFiles();
            if (dirFiles == null)
                continue;
            for (File f : dirFiles) {
                if (!f.getName().toLowerCase().endsWith(".apk"))
                    continue;
                PackageInfo pkgInfo = null;
                try {
                    pkgInfo = context.getPackageManager().getPackageArchiveInfo(f.getAbsolutePath(), 0);
                    pkgInfo.applicationInfo.sourceDir = f.getAbsolutePath();
                    pkgInfo.applicationInfo.publicSourceDir = f.getAbsolutePath();
                } catch (Exception e) {
                    // Ignore
                }
                if (pkgInfo != null)
                    packageList.add(pkgInfo);
            }
        }
        return packageList;
    }

    private List<AppInfo> convertPackageInfoToAppData(Context context, List<PackageInfo> pkgList, boolean fastOpen) {
        PackageManager pm = context.getPackageManager();
        List<AppInfo> list = new ArrayList<>(pkgList.size());
        String hostPkg = VirtualCore.get().getHostPkg();
        for (PackageInfo pkg : pkgList) {
            // ignore the host package
            if (hostPkg.equals(pkg.packageName)) {
                continue;
            }

            // ignore taichi package
            if (VirtualCore.TAICHI_PACKAGE.equals(pkg.packageName)) {
                continue;
            }

            // ignore the System package
            if (isSystemApplication(pkg)) {
                continue;
            }
            ApplicationInfo ai = pkg.applicationInfo;
            String path = ai.publicSourceDir != null ? ai.publicSourceDir : ai.sourceDir;
            if (path == null) {
                continue;
            }
            AppInfo info = new AppInfo();
            info.packageName = pkg.packageName;
            info.fastOpen = fastOpen;
            info.path = path;
//            info.icon = ai.loadIcon(pm);
            info.icon = null;  // Use Glide to load the icon async
            info.name = ai.loadLabel(pm);
            info.version = pkg.versionName;
            InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(pkg.packageName, 0);
            if (installedAppInfo != null) {
                info.cloneCount = installedAppInfo.getInstalledUsers().length;
            }
            if (ai.metaData != null && ai.metaData.containsKey("xposedmodule")) {
                info.disableMultiVersion = true;
                info.cloneCount = 0;
            }
            list.add(info);
        }
        // sort by name
        Collections.sort(list, (o1, o2) -> {
            HanziToPinyin hanziToPinyin = HanziToPinyin.getInstance();
            String pinyin1 = hanziToPinyin.toPinyinString(o1.name.toString().trim());
            String pinyin2 = hanziToPinyin.toPinyinString(o2.name.toString().trim());
            return pinyin1.compareTo(pinyin2);
        });
        return list;
    }

    @Override
    public InstallResult addVirtualApp(AppInfoLite info) {
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

    @Override
    public boolean removeVirtualApp(String packageName, int userId) {
        return VirtualCore.get().uninstallPackageAsUser(packageName, userId);
    }

}
