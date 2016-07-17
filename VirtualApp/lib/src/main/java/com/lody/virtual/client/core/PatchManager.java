package com.lody.virtual.client.core;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.delegate.AppInstrumentation;
import com.lody.virtual.client.hook.patchs.account.AccountManagerPatch;
import com.lody.virtual.client.hook.patchs.am.ActivityManagerPatch;
import com.lody.virtual.client.hook.patchs.am.HCallbackHook;
import com.lody.virtual.client.hook.patchs.appops.AppOpsManagerPatch;
import com.lody.virtual.client.hook.patchs.appwidget.AppWidgetManagerPatch;
import com.lody.virtual.client.hook.patchs.backup.BackupManagerPatch;
import com.lody.virtual.client.hook.patchs.clipboard.ClipBoardPatch;
import com.lody.virtual.client.hook.patchs.display.DisplayManagerPatch;
import com.lody.virtual.client.hook.patchs.graphics.GraphicsStatsPatch;
import com.lody.virtual.client.hook.patchs.imms.MmsPatch;
import com.lody.virtual.client.hook.patchs.input.InputMethodManagerPatch;
import com.lody.virtual.client.hook.patchs.job.JobPatch;
import com.lody.virtual.client.hook.patchs.location.LocationManagerPatch;
import com.lody.virtual.client.hook.patchs.media.router.MediaRouterServicePatch;
import com.lody.virtual.client.hook.patchs.media.session.SessionManagerPatch;
import com.lody.virtual.client.hook.patchs.mount.MountServicePatch;
import com.lody.virtual.client.hook.patchs.notification.NotificationManagerPatch;
import com.lody.virtual.client.hook.patchs.phonesubinfo.PhoneSubInfoPatch;
import com.lody.virtual.client.hook.patchs.pm.PackageManagerPatch;
import com.lody.virtual.client.hook.patchs.power.PowerManagerPatch;
import com.lody.virtual.client.hook.patchs.restriction.RestrictionPatch;
import com.lody.virtual.client.hook.patchs.search.SearchManagerPatch;
import com.lody.virtual.client.hook.patchs.telephony.TelephonyPatch;
import com.lody.virtual.client.hook.patchs.telephony_registry.TelephonyRegistryPatch;
import com.lody.virtual.client.hook.patchs.user.UserManagerPatch;
import com.lody.virtual.client.hook.patchs.vibrator.VibratorPatch;
import com.lody.virtual.client.hook.patchs.wifi.WifiManagerPatch;
import com.lody.virtual.client.hook.patchs.window.WindowManagerPatch;
import com.lody.virtual.client.interfaces.IHookObject;
import com.lody.virtual.client.interfaces.Injectable;
import com.lody.virtual.helper.utils.Reflect;

import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

/**
 * @author Lody
 *
 *         <p/>
 *         注入管理器,维护全部的注入对象.
 */
public final class PatchManager {

	private static final String TAG = PatchManager.class.getSimpleName();
	private Map<Class<?>, Injectable> injectableMap = new HashMap<Class<?>, Injectable>(12);

	private PatchManager() {
	}

	/**
	 * @return 注入管理器实例
	 */
	public static PatchManager getInstance() {
		return PatchManagerHolder.sPatchManager;
	}

	public void checkEnv() throws Throwable {
		for (Injectable injectable : injectableMap.values()) {
			if (injectable.isEnvBad()) {
				injectable.inject();
			}
		}
	}

	/**
	 * @return 是否已经初始化
	 */
	public boolean isInit() {
		return PatchManagerHolder.sInit;
	}

	/**
	 * 初始化PatchManager
	 * <h1>必须确保只调用一次.</h1>
	 */
	public void injectAll() throws Throwable {
		if (PatchManagerHolder.sInit) {
			throw new IllegalStateException("PatchManager Has been initialized.");
		}
		injectInternal();
		PatchManagerHolder.sInit = true;

	}

	private void injectInternal() throws Throwable {
		addPatch(new ActivityManagerPatch());
		addPatch(new PackageManagerPatch());

		if (VirtualCore.getCore().isVAppProcess()) {
			addPatch(HCallbackHook.getDefault());
			addPatch(AppInstrumentation.getDefault());
			addPatch(new NotificationManagerPatch());
			addPatch(new LocationManagerPatch());
			addPatch(new WindowManagerPatch());
			addPatch(new ClipBoardPatch());
			addPatch(new MountServicePatch());
			addPatch(new BackupManagerPatch());
			addPatch(new TelephonyPatch());
			addPatch(new PhoneSubInfoPatch());
			addPatch(new PowerManagerPatch());
			addPatch(new TelephonyRegistryPatch());
			addPatch(new AppWidgetManagerPatch());
			addPatch(new AccountManagerPatch());

			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR2) {
				addPatch(new VibratorPatch());
				addPatch(new WifiManagerPatch());
			}
			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
				addPatch(new UserManagerPatch());
			}

			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
				addPatch(new DisplayManagerPatch());
			}
			if (Build.VERSION.SDK_INT >= L) {
				addPatch(new InputMethodManagerPatch());
				addPatch(new MmsPatch());
				addPatch(new SessionManagerPatch());
				addPatch(new JobPatch());
				addPatch(new RestrictionPatch());
			}
			if (Build.VERSION.SDK_INT >= KITKAT) {
				addPatch(new AppOpsManagerPatch());
				addPatch(new MediaRouterServicePatch());
			}
			if (ServiceManager.getService(Context.SEARCH_SERVICE) != null) {
				addPatch(new SearchManagerPatch());
			}
			if (Build.VERSION.SDK_INT >= LOLLIPOP_MR1) {
				addPatch(new GraphicsStatsPatch());
			}
		}
	}

	private void addPatch(Injectable injectable) {
		injectableMap.put(injectable.getClass(), injectable);
	}

	public <T extends Injectable> T findPatch(Class<T> clazz) {
		// noinspection unchecked
		return (T) injectableMap.get(clazz);
	}

	public <T extends Injectable> void checkEnv(Class<T> clazz) {
		Injectable injectable = findPatch(clazz);
		if (injectable != null && injectable.isEnvBad()) {
			try {
				injectable.inject();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public void fixContext(Context context) {
		while (context instanceof ContextWrapper) {
			context = ((ContextWrapper) context).getBaseContext();
		}
		try {
			Reflect.on(context).set("mPackageManager", null);
			context.getPackageManager();
		} catch (Throwable e) {
			// Ignore
		}
	}

	public <T extends Injectable, H extends IHookObject> H getHookObject(Class<T> patchClass) {
		T patch = findPatch(patchClass);
		if (patch != null && patch instanceof PatchObject) {
			// noinspection unchecked
			return (H) ((PatchObject) patch).getHookObject();
		}
		return null;
	}


	private static final class PatchManagerHolder {
		private static PatchManager sPatchManager = new PatchManager();
		private static boolean sInit;
	}

}