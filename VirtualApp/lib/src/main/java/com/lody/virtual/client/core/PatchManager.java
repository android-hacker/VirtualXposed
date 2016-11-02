package com.lody.virtual.client.core;

import android.os.Build;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.delegate.AppInstrumentation;
import com.lody.virtual.client.hook.patchs.account.AccountManagerPatch;
import com.lody.virtual.client.hook.patchs.alarm.AlarmManagerPatch;
import com.lody.virtual.client.hook.patchs.am.ActivityManagerPatch;
import com.lody.virtual.client.hook.patchs.am.HCallbackHook;
import com.lody.virtual.client.hook.patchs.appops.AppOpsManagerPatch;
import com.lody.virtual.client.hook.patchs.appwidget.AppWidgetManagerPatch;
import com.lody.virtual.client.hook.patchs.audio.AudioManagerPatch;
import com.lody.virtual.client.hook.patchs.backup.BackupManagerPatch;
import com.lody.virtual.client.hook.patchs.bluetooth.BluetoothPatch;
import com.lody.virtual.client.hook.patchs.clipboard.ClipBoardPatch;
import com.lody.virtual.client.hook.patchs.connectivity.ConnectivityPatch;
import com.lody.virtual.client.hook.patchs.content.ContentServicePatch;
import com.lody.virtual.client.hook.patchs.display.DisplayPatch;
import com.lody.virtual.client.hook.patchs.dropbox.DropBoxManagerPatch;
import com.lody.virtual.client.hook.patchs.graphics.GraphicsStatsPatch;
import com.lody.virtual.client.hook.patchs.imms.MmsPatch;
import com.lody.virtual.client.hook.patchs.input.InputMethodManagerPatch;
import com.lody.virtual.client.hook.patchs.isms.ISmsPatch;
import com.lody.virtual.client.hook.patchs.isub.ISubPatch;
import com.lody.virtual.client.hook.patchs.job.JobPatch;
import com.lody.virtual.client.hook.patchs.libcore.LibCorePatch;
import com.lody.virtual.client.hook.patchs.location.LocationManagerPatch;
import com.lody.virtual.client.hook.patchs.media.router.MediaRouterServicePatch;
import com.lody.virtual.client.hook.patchs.media.session.SessionManagerPatch;
import com.lody.virtual.client.hook.patchs.mount.MountServicePatch;
import com.lody.virtual.client.hook.patchs.net_management.NetworkManagementPatch;
import com.lody.virtual.client.hook.patchs.notification.NotificationManagerPatch;
import com.lody.virtual.client.hook.patchs.persistent_data_block.PersistentDataBlockServicePatch;
import com.lody.virtual.client.hook.patchs.phonesubinfo.PhoneSubInfoPatch;
import com.lody.virtual.client.hook.patchs.pm.PackageManagerPatch;
import com.lody.virtual.client.hook.patchs.power.PowerManagerPatch;
import com.lody.virtual.client.hook.patchs.restriction.RestrictionPatch;
import com.lody.virtual.client.hook.patchs.search.SearchManagerPatch;
import com.lody.virtual.client.hook.patchs.telephony.TelephonyPatch;
import com.lody.virtual.client.hook.patchs.user.UserManagerPatch;
import com.lody.virtual.client.hook.patchs.vibrator.VibratorPatch;
import com.lody.virtual.client.hook.patchs.wifi.WifiManagerPatch;
import com.lody.virtual.client.hook.patchs.window.WindowManagerPatch;
import com.lody.virtual.client.interfaces.IHookObject;
import com.lody.virtual.client.interfaces.Injectable;

import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;

/**
 * @author Lody
 *
 */
public final class PatchManager {

	private static final String TAG = PatchManager.class.getSimpleName();

	private Map<Class<?>, Injectable> injectTable = new HashMap<>(12);

	private PatchManager() {
	}

	public static PatchManager getInstance() {
		return PatchManagerHolder.sPatchManager;
	}

	void injectAll() throws Throwable {
		for (Injectable injectable : injectTable.values()) {
			injectable.inject();
		}
		// XXX: Lazy inject the Instrumentation,
		// It is important in many cases.
		addPatch(AppInstrumentation.getDefault());
	}

    /**
	 * @return 是否已经初始化
	 */
	public boolean isInit() {
		return PatchManagerHolder.sInit;
	}


	public void init() throws Throwable {
		if (PatchManagerHolder.sInit) {
			throw new IllegalStateException("PatchManager Has been initialized.");
		}
		injectInternal();
		PatchManagerHolder.sInit = true;

	}

	private void injectInternal() throws Throwable {
		if (VirtualCore.get().isMainProcess()) {
			return;
		}
		if (VirtualCore.get().isServerProcess()) {
			addPatch(new ActivityManagerPatch());
			addPatch(new PackageManagerPatch());
			return;
		}
		if (VirtualCore.get().isVAppProcess()) {
			addPatch(new LibCorePatch());
			addPatch(new ActivityManagerPatch());
			addPatch(new PackageManagerPatch());
			// ## End
			addPatch(HCallbackHook.getDefault());
			addPatch(new ISmsPatch());
			addPatch(new ISubPatch());
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
			addPatch(new AppWidgetManagerPatch());
			addPatch(new AccountManagerPatch());
			addPatch(new AudioManagerPatch());
			addPatch(new SearchManagerPatch());
			addPatch(new ContentServicePatch());

			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR2) {
				addPatch(new VibratorPatch());
				addPatch(new WifiManagerPatch());
				addPatch(new BluetoothPatch());
			}
			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
				addPatch(new UserManagerPatch());
			}

			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
				addPatch(new DisplayPatch());
			}
			if (Build.VERSION.SDK_INT >= LOLLIPOP) {
				addPatch(new PersistentDataBlockServicePatch());
				addPatch(new InputMethodManagerPatch());
				addPatch(new MmsPatch());
				addPatch(new SessionManagerPatch());
				addPatch(new JobPatch());
				addPatch(new RestrictionPatch());
			}
			if (Build.VERSION.SDK_INT >= KITKAT) {
				addPatch(new AlarmManagerPatch());
				addPatch(new AppOpsManagerPatch());
				addPatch(new MediaRouterServicePatch());
			}
			if (Build.VERSION.SDK_INT >= LOLLIPOP_MR1) {
				addPatch(new GraphicsStatsPatch());
			}
			if (Build.VERSION.SDK_INT >= M) {
				addPatch(new NetworkManagementPatch());
			}
            addPatch(new ConnectivityPatch());
		}
	}

	private void addPatch(Injectable injectable) {
		injectTable.put(injectable.getClass(), injectable);
	}

	public <T extends Injectable> T findPatch(Class<T> clazz) {
		// noinspection unchecked
		return (T) injectTable.get(clazz);
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
		if (patch != null && patch instanceof PatchDelegate) {
			// noinspection unchecked
			return (H) ((PatchDelegate) patch).getHookDelegate();
		}
		return null;
	}

	private static final class PatchManagerHolder {
		private static PatchManager sPatchManager = new PatchManager();
		private static boolean sInit;
	}

}