package com.lody.virtual.client.core;

import android.os.Build;
import android.provider.Settings;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.delegate.AppInstrumentation;
import com.lody.virtual.client.hook.patchs.accessibility.AccessibilityPatch;
import com.lody.virtual.client.hook.patchs.account.AccountManagerPatch;
import com.lody.virtual.client.hook.patchs.alerm.AlarmManagerPatch;
import com.lody.virtual.client.hook.patchs.am.ActivityManagerPatch;
import com.lody.virtual.client.hook.patchs.am.HCallbackHook;
import com.lody.virtual.client.hook.patchs.appops.AppOpsManagerPatch;
import com.lody.virtual.client.hook.patchs.appwidget.AppWidgetManagerPatch;
import com.lody.virtual.client.hook.patchs.audio.AudioManagerPatch;
import com.lody.virtual.client.hook.patchs.backup.BackupManagerPatch;
import com.lody.virtual.client.hook.patchs.camera.CameraPatch;
import com.lody.virtual.client.hook.patchs.clipboard.ClipBoardPatch;
import com.lody.virtual.client.hook.patchs.content.ContentServicePatch;
import com.lody.virtual.client.hook.patchs.display.DisplayManagerPatch;
import com.lody.virtual.client.hook.patchs.dropbox.DropBoxManagerPatch;
import com.lody.virtual.client.hook.patchs.graphics.GraphicsStatsPatch;
import com.lody.virtual.client.hook.patchs.imms.MmsPatch;
import com.lody.virtual.client.hook.patchs.input.InputMethodManagerPatch;
import com.lody.virtual.client.hook.patchs.isub.SubPatch;
import com.lody.virtual.client.hook.patchs.job.JobPatch;
import com.lody.virtual.client.hook.patchs.libcore.LibCorePatch;
import com.lody.virtual.client.hook.patchs.location.LocationManagerPatch;
import com.lody.virtual.client.hook.patchs.media.router.MediaRouterServicePatch;
import com.lody.virtual.client.hook.patchs.media.session.SessionManagerPatch;
import com.lody.virtual.client.hook.patchs.miui.security.MIUISecurityManagerPatch;
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
import static android.os.Build.VERSION_CODES.LOLLIPOP;
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

	private static void fixSetting(Class<?> settingClass) {
		Reflect.on(settingClass).field("sNameValueCache").set("mContentProvider", null);
	}

	public static void fixAllSettings() {
		try {
			fixSetting(Settings.System.class);
			fixSetting(Settings.Secure.class);
			fixSetting(Settings.Global.class);
		} catch (Throwable e) {
			// No class def
		}
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
		if (VirtualCore.getCore().isMainProcess()) {
			addPatch(new ActivityManagerPatch());
			return;
		} else if (VirtualCore.getCore().isServiceProcess()) {
			addPatch(new ActivityManagerPatch());
			return;
		}
		if (VirtualCore.getCore().isVAppProcess()) {
			addPatch(new ActivityManagerPatch());
			addPatch(new PackageManagerPatch());
			addPatch(new LibCorePatch());
			// ## Fuck the MIUI Security
			if (MIUISecurityManagerPatch.needInject()) {
				addPatch(new MIUISecurityManagerPatch());
			}
			// ## End
			addPatch(HCallbackHook.getDefault());
			addPatch(AppInstrumentation.getDefault());

			addPatch(new DropBoxManagerPatch());
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
			addPatch(new AudioManagerPatch());
			addPatch(new SearchManagerPatch());
			addPatch(new AlarmManagerPatch());
			addPatch(new AccessibilityPatch());
			addPatch(new ContentServicePatch());

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
			if (Build.VERSION.SDK_INT >= LOLLIPOP) {
				addPatch(new InputMethodManagerPatch());
				addPatch(new MmsPatch());
				addPatch(new SessionManagerPatch());
				addPatch(new JobPatch());
				addPatch(new RestrictionPatch());
				addPatch(new CameraPatch());
			}
			if (Build.VERSION.SDK_INT >= KITKAT) {
				addPatch(new AppOpsManagerPatch());
				addPatch(new MediaRouterServicePatch());
			}
			if (Build.VERSION.SDK_INT >= LOLLIPOP_MR1) {
				addPatch(new GraphicsStatsPatch());
				addPatch(new SubPatch());
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