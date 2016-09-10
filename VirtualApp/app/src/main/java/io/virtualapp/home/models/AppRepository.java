package io.virtualapp.home.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Environment;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.os.VUserHandle;

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
	private static List<String> sdCardScanPaths = new ArrayList<>();

	static {
		String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		sdCardScanPaths.add(sdCardPath);
		sdCardScanPaths.add(sdCardPath + File.separator + "wandoujia" + File.separator + "app");
		sdCardScanPaths
				.add(sdCardPath + File.separator + "tencent" + File.separator + "tassistant" + File.separator + "apk");
		sdCardScanPaths.add(sdCardPath + File.separator + "BaiduAsa9103056");
		sdCardScanPaths.add(sdCardPath + File.separator + "360Download");
		sdCardScanPaths.add(sdCardPath + File.separator + "pp/downloader");
		sdCardScanPaths.add(sdCardPath + File.separator + "pp/downloader/apk");
		sdCardScanPaths.add(sdCardPath + File.separator + "pp/downloader/silent/apk");
	}

	private Context mContext;

	public AppRepository(Context context) {
		mContext = context;
	}

	private static boolean isSystemApplication(PackageInfo packageInfo) {
		return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
	}

	@Override
	public Promise<List<AppModel>, Throwable, Void> getVirtualApps() {
		return VUiKit.defer().when(() -> {
			List<AppSetting> infos = VirtualCore.get().getAllApps();
			List<AppModel> models = new ArrayList<AppModel>();
			for (AppSetting info : infos) {
				if (VirtualCore.get().getLaunchIntent(info.packageName, VUserHandle.USER_OWNER) != null) {
					models.add(new AppModel(mContext, info));
				}
			}
			Collections.sort(models, (lhs, rhs) -> COLLATOR.compare(lhs.name, rhs.name));
			return models;
		});
	}

	@Override
	public Promise<List<AppModel>, Throwable, Void> getInstalledApps(Context context) {
		return VUiKit.defer().when(() -> {
			return pkgInfosToAppModels(context, context.getPackageManager().getInstalledPackages(0), true);
		});
	}

	@Override
	public Promise<List<AppModel>, Throwable, Void> getSdCardApps(Context context) {
		return VUiKit.defer().when(() -> {
			return pkgInfosToAppModels(context, findAndParseAPKs(context, sdCardScanPaths), false);
		});
	}

	private List<PackageInfo> findAndParseAPKs(Context context, List<String> pathes) {
		List<PackageInfo> pkgs = new ArrayList<>();
		if (pathes == null)
			return pkgs;
		for (String path : pathes) {
			File dir = new File(path);
			if (!dir.exists() || !dir.isDirectory())
				continue;
			for (File f : dir.listFiles()) {
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
					pkgs.add(pkgInfo);
			}
		}
		return pkgs;
	}

	private List<AppModel> pkgInfosToAppModels(Context context, List<PackageInfo> pkgList, boolean fastOpen) {
		List<AppModel> models = new ArrayList<>(pkgList.size());
		String hostPkg = VirtualCore.get().getHostPkg();
		for (PackageInfo pkg : pkgList) {
			if (hostPkg.equals(pkg.packageName)) {
				continue;
			}
			if (isSystemApplication(pkg)) {
				continue;
			}
			if (VirtualCore.get().isAppInstalled(pkg.packageName)) {
				continue;
			}
			AppModel model = new AppModel(context, pkg);
			model.fastOpen = fastOpen;
			models.add(model);
		}
		Collections.sort(models, (lhs, rhs) -> COLLATOR.compare(lhs.name, rhs.name));
		return models;
	}

	@Override
	public void addVirtualApp(AppModel app) throws Throwable {
		int flags = InstallStrategy.COMPARE_VERSION;
		if (app.fastOpen) {
			flags |= InstallStrategy.DEPEND_SYSTEM_IF_EXIST;
		}
		VirtualCore.get().installApp(app.path, flags);
	}

	@Override
	public void removeVirtualApp(AppModel app) throws Throwable {
		VirtualCore.get().uninstallApp(app.packageName);
	}

}
