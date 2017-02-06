package com.lody.virtual.client.hook.patchs.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.utils.BitmapUtils;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class BroadcastIntent extends Hook {

    @Override
    public String getName() {
        return "broadcastIntent";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        Intent intent = (Intent) args[1];
        String type = (String) args[2];
        intent.setDataAndType(intent.getData(), type);
        if (VirtualCore.get().getComponentDelegate() != null) {
            VirtualCore.get().getComponentDelegate().onSendBroadcast(intent);
        }
        Intent newIntent = handleIntent(intent);
        if (newIntent != null) {
            args[1] = newIntent;
        } else {
            return 0;
        }

        if (args[7] instanceof String || args[7] instanceof String[]) {
            // clear the permission
            args[7] = null;
        }
        return method.invoke(who, args);
    }


    private Intent handleIntent(final Intent intent) {
        final String action = intent.getAction();
        if ("android.intent.action.CREATE_SHORTCUT".equals(action)
                || "com.android.launcher.action.INSTALL_SHORTCUT".equals(action)) {

            return StubManifest.ENABLE_INNER_SHORTCUT ? handleInstallShortcutIntent(intent) : null;

        } else if ("com.android.launcher.action.UNINSTALL_SHORTCUT".equals(action)) {

            handleUninstallShortcutIntent(intent);

        } else {
            return ComponentUtils.redirectBroadcastIntent(intent, VUserHandle.myUserId());
        }
        return intent;
    }

    private Intent handleInstallShortcutIntent(Intent intent) {
        Intent shortcut = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (shortcut != null) {
            ComponentName component = shortcut.resolveActivity(VirtualCore.getPM());
            if (component != null) {
                String pkg = component.getPackageName();
                Intent newShortcutIntent = new Intent();
                newShortcutIntent.setClassName(getHostPkg(), Constants.SHORTCUT_PROXY_ACTIVITY_NAME);
                newShortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
                newShortcutIntent.putExtra("_VA_|_intent_", shortcut);
                newShortcutIntent.putExtra("_VA_|_uri_", shortcut.toUri(0));
                newShortcutIntent.putExtra("_VA_|_user_id_", VUserHandle.myUserId());
                intent.removeExtra(Intent.EXTRA_SHORTCUT_INTENT);
                intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, newShortcutIntent);

                Intent.ShortcutIconResource icon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                if (icon != null && !TextUtils.equals(icon.packageName, getHostPkg())) {
                    try {
                        Resources resources = VirtualCore.get().getResources(pkg);
                        if (resources != null) {
                            int resId = resources.getIdentifier(icon.resourceName, "drawable", pkg);
                            if (resId > 0) {
                                Drawable iconDrawable = resources.getDrawable(resId);
                                Bitmap newIcon = BitmapUtils.drawableToBitmap(iconDrawable);
                                if (newIcon != null) {
                                    intent.removeExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, newIcon);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return intent;
    }

    private void handleUninstallShortcutIntent(Intent intent) {
        Intent shortcut = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (shortcut != null) {
            ComponentName componentName = shortcut.resolveActivity(getPM());
            if (componentName != null) {
                Intent newShortcutIntent = new Intent();
                newShortcutIntent.putExtra("_VA_|_uri_", shortcut);
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
