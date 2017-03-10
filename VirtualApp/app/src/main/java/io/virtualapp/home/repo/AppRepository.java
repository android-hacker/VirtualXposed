package io.virtualapp.home.repo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstalledAppInfo;
import com.lody.virtual.remote.InstallResult;

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
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.home.models.PackageAppData;

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

    private Context mContext;

    public AppRepository(Context context) {
        mContext = context;
    }

    private static boolean isSystemApplication(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    @Override
    public Promise<List<AppData>, Throwable, Void> getVirtualApps() {
        return VUiKit.defer().when(() -> {
            List<InstalledAppInfo> infos = VirtualCore.get().getInstalledApps();
            List<AppData> models = new ArrayList<AppData>();
            for (InstalledAppInfo info : infos) {
                models.add(new PackageAppData(mContext, info));
            }
            Collections.sort(models, (lhs, rhs) -> COLLATOR.compare(lhs.getName(), rhs.getName()));
            return models;
        });
    }

    @Override
    public Promise<List<AppData>, Throwable, Void> getInstalledApps(Context context) {
        return VUiKit.defer().when(() -> convertPackageInfoToAppData(context, context.getPackageManager().getInstalledPackages(0), true));
    }

    @Override
    public Promise<List<AppData>, Throwable, Void> getStorageApps(Context context, File rootDir) {
        return VUiKit.defer().when(() -> convertPackageInfoToAppData(context, findAndParseAPKs(context, rootDir, SCAN_PATH_LIST), false));
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

    private List<AppData> convertPackageInfoToAppData(Context context, List<PackageInfo> pkgList, boolean fastOpen) {
        List<AppData> models = new ArrayList<>(pkgList.size());
        String hostPkg = VirtualCore.get().getHostPkg();
        for (PackageInfo pkg : pkgList) {
            // ignore the host package
            if (hostPkg.equals(pkg.packageName)) {
                continue;
            }
            // ignore the System package
            if (isSystemApplication(pkg)) {
                continue;
            }
            // ignore the installed package
            if (VirtualCore.get().isAppInstalled(pkg.packageName)) {
                continue;
            }
            PackageAppData model = new PackageAppData(context, pkg);
            model.fastOpen = fastOpen;
            models.add(model);
        }
        Collections.sort(models, (lhs, rhs) -> COLLATOR.compare(lhs.getName(), rhs.getName()));
        return models;
    }

    @Override
    public InstallResult addVirtualApp(AppInfoLite info) {
        int flags = InstallStrategy.COMPARE_VERSION;
        if (info.fastOpen) {
            flags |= InstallStrategy.DEPEND_SYSTEM_IF_EXIST;
        }
        return VirtualCore.get().installApp(info.path, flags);
    }

    @Override
    public boolean removeVirtualApp(String packageName) {
        return VirtualCore.get().uninstallApp(packageName);
    }

}
