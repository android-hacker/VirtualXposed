package com.lody.virtual.client.hook.patchs.am;

import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.utils.BitmapUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.app.ActivityManagerNative#broadcastIntent(IApplicationThread,
 *      Intent, String, IIntentReceiver, int, String, Bundle, String[], int,
 *      Bundle, boolean, boolean, int)
 */
/* package */ class Hook_BroadcastIntent extends Hook {

	@Override
	public String getName() {
		return "broadcastIntent";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args[1] instanceof Intent) {
			Intent intent = (Intent) args[1];
			handleIntent(intent);
		}
		if (args[7] instanceof String) {
			args[7] = VirtualCore.getPermissionBroadcast();
		} else if (args[7] instanceof String[]) {
			args[7] = new String[]{VirtualCore.getPermissionBroadcast()};
		}
		return method.invoke(who, args);
	}

	private void handleIntent(final Intent intent) {
		final String action = intent.getAction();
		if (Constants.ACTION_INSTALL_SHORTCUT.equals(action)) {
			handleInstallShortcutIntent(intent);
		} else if (Constants.ACTION_UNINSTALL_SHORTCUT.equals(action)) {
			handleUninstallShortcutIntent(intent);
		} else {
			ComponentName cn = intent.getComponent();
			if (cn != null && VirtualCore.getCore().isAppInstalled(cn.getPackageName())
					&& !TextUtils.isEmpty(cn.getClassName()) && !TextUtils.isEmpty(cn.getPackageName())
					&& TextUtils.isEmpty(intent.getAction())) {
				String name = cn.getClassName();
				if (name.startsWith(".")) {
					name = cn.getPackageName() + cn.getClassName();
				}
				intent.setComponent(null);
				intent.setAction(VirtualCore.getReceiverAction(cn.getPackageName(), name));
			}
		}
	}

	private void handleInstallShortcutIntent(Intent intent) {
		Intent shortcut = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		if (shortcut != null) {
			ComponentName component = shortcut.resolveActivity(VirtualCore.getPM());
			if (component != null) {
				String pkg = component.getPackageName();
				if (isAppPkg(pkg)) {
					Intent newShortcutIntent = new Intent();
					newShortcutIntent.setClassName(getHostPkg(), Constants.SHORTCUT_PROXY_ACTIVITY_NAME);
					newShortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
					newShortcutIntent.putExtra(ExtraConstants.EXTRA_TARGET_INTENT, shortcut);
					newShortcutIntent.putExtra(ExtraConstants.EXTRA_TARGET_URI, shortcut.toUri(0));
					intent.removeExtra(Intent.EXTRA_SHORTCUT_INTENT);
					intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, newShortcutIntent);

					// 将icon替换为以Bitmap方式绘制的Shortcut Icon
					Intent.ShortcutIconResource icon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
					if (icon != null && !TextUtils.equals(icon.packageName, getHostPkg())) {
						try {
							Resources resources = VirtualCore.getCore().getResources(pkg);
							if (resources != null) {
								int resId = resources.getIdentifier(icon.resourceName, "drawable", pkg);
								if (resId > 0) {
									Drawable iconDrawable = resources.getDrawable(resId);
									Bitmap newIcon = BitmapUtils.drawableToBitMap(iconDrawable);
									if (newIcon != null) {
										intent.removeExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
										intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, newIcon);
									}
								}
							}
						} catch (Throwable e) {
							e.printStackTrace();
							// Ignore
						}
					}
				}
			}
		}
	}

	private void handleUninstallShortcutIntent(Intent intent) {
		Intent shortcut = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		if (shortcut != null) {
			ComponentName componentName = shortcut.resolveActivity(getPM());
			if (componentName != null && isAppPkg(componentName.getPackageName())) {
				Intent newShortcutIntent = new Intent();
				newShortcutIntent.putExtra(ExtraConstants.EXTRA_TARGET_URI, shortcut);
				newShortcutIntent.setClassName(getHostPkg(), Constants.SHORTCUT_PROXY_ACTIVITY_NAME);
				newShortcutIntent.removeExtra(Intent.EXTRA_SHORTCUT_INTENT);
				intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, newShortcutIntent);
			}
		}
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
