package io.virtualapp.home.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppInfo;

import org.jdeferred.Promise;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.virtualapp.abs.ui.VUiKit;

/**
 * @author Lody
 */
public class AppRepository implements AppDataSource {

    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);
    private Context mContext;
    private static List<String> sdCardScanPathes = new ArrayList<>();

    static {
        String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        sdCardScanPathes.add(sdCardPath);
        sdCardScanPathes.add(sdCardPath + File.separator + "wandoujia" + File.separator + "app");
        sdCardScanPathes.add(sdCardPath + File.separator + "tencent" + File.separator + "tassistant" + File.separator + "apk");
        sdCardScanPathes.add(sdCardPath + File.separator + "BaiduAsa9103056");
        sdCardScanPathes.add(sdCardPath + File.separator + "360Download");
    }

    public AppRepository(Context context) {
        mContext = context;
    }

    private static boolean isSystemApplication(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    @Override
    public Promise<List<AppModel>, Throwable, Void> getVirtualApps() {
        return VUiKit.defer().when(() -> {
            List<AppInfo> infos = VirtualCore.getCore().getAllApps();
            List<AppModel> models = new ArrayList<AppModel>();
            for (AppInfo info : infos) {
                models.add(new AppModel(mContext, info));
            }
            Collections.sort(models, (lhs, rhs) -> COLLATOR.compare(lhs.name, rhs.name));
            return models;
        });
    }

    @Override
    public Promise<List<AppModel>, Throwable, Void> getInstalledApps(Context context) {
        return VUiKit.defer().when(() -> {
            return pkgInfosToAppModels(context, context.getPackageManager().getInstalledPackages(0));
        });
    }

    @Override
    public Promise<List<AppModel>, Throwable, Void> getSdCardApps(Context context) {
        return VUiKit.defer().when(() -> {
            return pkgInfosToAppModels(context, findAndParseAPKs(context, sdCardScanPathes));
        });
    }


    private List<PackageInfo> findAndParseAPKs(Context context, List<String> pathes) {
        List<PackageInfo> pkgs = new ArrayList<>();
        if (pathes == null) return pkgs;
        for (String path : pathes) {
            File dir = new File(path);
            if (!dir.exists() || !dir.isDirectory()) continue;
            for (File f : dir.listFiles()) {
                if (!f.getName().toLowerCase().endsWith(".apk")) continue;
                PackageInfo pkgInfo = null;
                try {
                    pkgInfo = context.getPackageManager().getPackageArchiveInfo(f.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
                    pkgInfo.applicationInfo.sourceDir = f.getAbsolutePath();
                    pkgInfo.applicationInfo.publicSourceDir = f.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (pkgInfo != null) pkgs.add(pkgInfo);
            }
        }
        return pkgs;
    }


    private List<AppModel> pkgInfosToAppModels(Context context, List<PackageInfo> pkgList) {
        List<AppModel> models = new ArrayList<>(pkgList.size());
        String hostPkg = VirtualCore.getCore().getHostPkg();
        for (PackageInfo pkg : pkgList) {
            if (hostPkg.equals(pkg.packageName)) {
                continue;
            }
            if (isSystemApplication(pkg)) {
                continue;
            }
            models.add(new AppModel(context, pkg));
        }
        Collections.sort(models, (lhs, rhs) -> COLLATOR.compare(lhs.name, rhs.name));
        return models;
    }


    @Override
    public void addVirtualApp(AppModel app) throws Throwable {
        VirtualCore.getCore().installApp(app.path, InstallStrategy.COMPARE_VERSION);
    }

    @Override
    public void removeVirtualApp(AppModel app) throws Throwable {
        VirtualCore.getCore().uninstallApp(app.packageName);
    }

}
