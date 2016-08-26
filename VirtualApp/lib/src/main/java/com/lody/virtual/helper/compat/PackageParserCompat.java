package com.lody.virtual.helper.compat;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Provider;
import android.content.pm.PackageParser.Service;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Pair;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VUserHandle;

import java.io.File;

import mirror.android.content.pm.PackageParserJellyBean;
import mirror.android.content.pm.PackageParserJellyBean17;
import mirror.android.content.pm.PackageParserLollipop;
import mirror.android.content.pm.PackageParserLollipop22;
import mirror.android.content.pm.PackageParserMarshmallow;
import mirror.android.content.pm.PackageUserState;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;

/**
 * @author Lody
 */

public class PackageParserCompat {

	private static final int SDK = Build.VERSION.SDK_INT;
	private static final int[] gids = VirtualCore.get().getGids();
	private static final int myUserId = VUserHandle.getUserId(Process.myUid());
	private static final Object sUserState = SDK >= JELLY_BEAN_MR1 ? PackageUserState.ctor.newInstance() : null;

	public static Pair<PackageParser, Package> parsePackage(File packageFile, int flags) throws Throwable {
		if (SDK >= M) {

			PackageParser parser = PackageParserMarshmallow.ctor.newInstance();
			return new Pair<>(parser, PackageParserMarshmallow.parsePackage.callWithException(parser, packageFile, flags));

		} else if (SDK >= LOLLIPOP_MR1) {

			PackageParser parser = PackageParserLollipop22.ctor.newInstance();
			return new Pair<>(parser, PackageParserLollipop22.parsePackage.callWithException(parser, packageFile, flags));

		} else if (SDK >= LOLLIPOP) {

			PackageParser parser = PackageParserLollipop.ctor.newInstance();
			return new Pair<>(parser, PackageParserLollipop.parsePackage.callWithException(parser, packageFile, flags));

		} else if (SDK >= JELLY_BEAN_MR1) {

			PackageParser parser = PackageParserJellyBean17.ctor.newInstance(packageFile.getAbsolutePath());
			return new Pair<>(parser, PackageParserJellyBean17.parsePackage.callWithException(parser, packageFile, null,
					new DisplayMetrics(), flags));

		} else if (SDK >= JELLY_BEAN) {

			PackageParser parser = PackageParserJellyBean.ctor.newInstance(packageFile.getAbsolutePath());
			return new Pair<>(parser, PackageParserJellyBean.parsePackage.callWithException(parser, packageFile, null,
					new DisplayMetrics(), flags));

		} else {

			PackageParser parser = mirror.android.content.pm.PackageParser.ctor.newInstance(packageFile.getAbsolutePath());
			return new Pair<>(parser, mirror.android.content.pm.PackageParser.parsePackage.callWithException(parser, packageFile, null,
					new DisplayMetrics(), flags));

		}
	}

	public static ServiceInfo generateServiceInfo(Service service, int flags) {

		if (SDK >= M) {

			return PackageParserMarshmallow.generateServiceInfo.call(service, flags, sUserState, myUserId);

		} else if (SDK >= LOLLIPOP_MR1) {

			return PackageParserLollipop22.generateServiceInfo.call(service, flags, sUserState, myUserId);

		} else if (SDK >= LOLLIPOP) {

			return PackageParserLollipop.generateServiceInfo.call(service, flags, sUserState, myUserId);

		} else if (SDK >= JELLY_BEAN_MR1) {

			return PackageParserJellyBean17.generateServiceInfo.call(service, flags, sUserState, myUserId);

		} else if (SDK >= JELLY_BEAN) {

			return PackageParserJellyBean.generateServiceInfo.call(service, flags, false, 1, myUserId);

		} else {
			return mirror.android.content.pm.PackageParser.generateServiceInfo.call(service, flags);
		}
	}

	public static ApplicationInfo generateApplicationInfo(Package p, int flags) {
		if (SDK >= M) {

			return PackageParserMarshmallow.generateApplicationInfo.call(p, flags, sUserState);

		} else if (SDK >= LOLLIPOP_MR1) {

			return PackageParserLollipop22.generateApplicationInfo.call(p, flags, sUserState);

		} else if (SDK >= LOLLIPOP) {

			return PackageParserLollipop.generateApplicationInfo.call(p, flags, sUserState);

		} else if (SDK >= JELLY_BEAN_MR1) {

			return PackageParserJellyBean17.generateApplicationInfo.call(p, flags, sUserState);

		} else if (SDK >= JELLY_BEAN) {

			return PackageParserJellyBean.generateApplicationInfo.call(p, flags, false, 1);

		} else {
			return mirror.android.content.pm.PackageParser.generateApplicationInfo.call(p, flags);
		}
	}

	public static ActivityInfo generateActivityInfo(Activity activity, int flags) {

		if (SDK >= M) {

			return PackageParserMarshmallow.generateActivityInfo.call(activity, flags, sUserState, myUserId);

		} else if (SDK >= LOLLIPOP_MR1) {

			return PackageParserLollipop22.generateActivityInfo.call(activity, flags, sUserState, myUserId);

		} else if (SDK >= LOLLIPOP) {

			return PackageParserLollipop.generateActivityInfo.call(activity, flags, sUserState, myUserId);

		} else if (SDK >= JELLY_BEAN_MR1) {

			return PackageParserJellyBean17.generateActivityInfo.call(activity, flags, sUserState, myUserId);

		} else if (SDK >= JELLY_BEAN) {

			return PackageParserJellyBean.generateActivityInfo.call(activity, flags, false, 1, myUserId);

		} else {
			return mirror.android.content.pm.PackageParser.generateActivityInfo.call(activity, flags);
		}
	}

	public static ProviderInfo generateProviderInfo(Provider provider, int flags) {

		if (SDK >= M) {

			return PackageParserMarshmallow.generateProviderInfo.call(provider, flags, sUserState, myUserId);

		} else if (SDK >= LOLLIPOP_MR1) {

			return PackageParserLollipop22.generateProviderInfo.call(provider, flags, sUserState, myUserId);

		} else if (SDK >= LOLLIPOP) {

			return PackageParserLollipop.generateProviderInfo.call(provider, flags, sUserState, myUserId);

		} else if (SDK >= JELLY_BEAN_MR1) {

			return PackageParserJellyBean17.generateProviderInfo.call(provider, flags, sUserState, myUserId);

		} else if (SDK >= JELLY_BEAN) {

			return PackageParserJellyBean.generateProviderInfo.call(provider, flags, false, 1, myUserId);

		} else {
			return mirror.android.content.pm.PackageParser.generateProviderInfo.call(provider, flags);
		}
	}

	public static PackageInfo generatePackageInfo(Package p, int flags, long firstInstallTime, long lastUpdateTime) {
		if (SDK >= M) {

			return PackageParserMarshmallow.generatePackageInfo.call(p, gids, flags, firstInstallTime, lastUpdateTime,
					null, sUserState);

		} else if (SDK >= LOLLIPOP) {
			if (PackageParserLollipop22.generatePackageInfo != null) {
				return PackageParserLollipop22.generatePackageInfo.call(p, gids, flags, firstInstallTime, lastUpdateTime,
						null, sUserState);
			} else {
				return PackageParserLollipop.generatePackageInfo.call(p, gids, flags, firstInstallTime, lastUpdateTime,
						null, sUserState);
			}

		} else if (SDK >= JELLY_BEAN_MR1) {

			return PackageParserJellyBean17.generatePackageInfo.call(p, gids, flags, firstInstallTime, lastUpdateTime,
					null, sUserState);

		} else if (SDK >= JELLY_BEAN) {

			return PackageParserJellyBean.generatePackageInfo.call(p, gids, flags, firstInstallTime, lastUpdateTime,
					null);

		} else {
			return mirror.android.content.pm.PackageParser.generatePackageInfo.call(p, gids, flags, firstInstallTime,
					lastUpdateTime);
		}
	}

	public static void collectCertificates(PackageParser parser, Package p, int flags) {
		if (SDK >= M) {

			PackageParserMarshmallow.collectCertificates.call(parser, p, flags);

		} else if (SDK >= LOLLIPOP_MR1) {

			PackageParserLollipop22.collectCertificates.call(parser, p, flags);

		} else if (SDK >= LOLLIPOP) {

			PackageParserLollipop.collectCertificates.call(parser, p, flags);

		} else if (SDK >= JELLY_BEAN_MR1) {

			 PackageParserJellyBean17.collectCertificates.call(parser, p, flags);

		} else if (SDK >= JELLY_BEAN) {

			PackageParserJellyBean.collectCertificates.call(parser, p, flags);

		} else {
			mirror.android.content.pm.PackageParser.collectCertificates.call(parser, p, flags);
		}
	}
}
