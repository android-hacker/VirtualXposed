package com.lody.virtual.client.hook.proxies.shortcut;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.PersistableBundle;

import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;

import java.lang.reflect.Method;
import java.util.List;

import mirror.android.content.pm.IShortcutService;
import mirror.android.content.pm.ParceledListSlice;

/**
 * @author Lody
 */
public class ShortcutServiceStub extends BinderInvocationProxy {


    public ShortcutServiceStub() {
        super(IShortcutService.Stub.asInterface, "shortcut");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getManifestShortcuts"));
        // TODO: 18/3/3 Support dynamic shortcut ?
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getDynamicShortcuts"));
        addMethodProxy(new ReplacePkgAndShortcutListMethodProxy("setDynamicShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("addDynamicShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("createShortcutResultIntent"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("disableShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("enableShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getRemainingCallCount"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getRateLimitResetTime"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getIconMaxDimensions"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getMaxShortcutCountPerActivity"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("reportShortcutUsed"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("onApplicationActive"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("removeAllDynamicShortcuts"));

        addMethodProxy(new ReplaceCallingPkgMethodProxy("getPinnedShortcuts"));
        addMethodProxy(new ReplacePkgAndShortcutMethodProxy("requestPinShortcut"));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void replaceShortcutInfo(ShortcutInfo shortcutInfo, String hostPackage, PackageManager pm) {
        if (shortcutInfo == null) {
            return;
        }

        mirror.android.content.pm.ShortcutInfo.mPackageName.set(shortcutInfo, hostPackage);
        try {
            Drawable applicationIcon = pm.getApplicationIcon(hostPackage);
            Icon icon = Icon.createWithBitmap(((BitmapDrawable) applicationIcon).getBitmap());
            mirror.android.content.pm.ShortcutInfo.mIcon.set(shortcutInfo, icon);
        } catch (Throwable ignored) {
        }

        Intent[] intents = mirror.android.content.pm.ShortcutInfo.mIntents.get(shortcutInfo);

        if (intents != null) {
            int length = intents.length;
            Intent[] swap = new Intent[length];

            PersistableBundle[] persistableBundles = mirror.android.content.pm.ShortcutInfo.mIntentPersistableExtrases.get(shortcutInfo);
            if (persistableBundles == null) {
                persistableBundles = new PersistableBundle[length];
            }

            for (int i = 0; i < length; i++) {
                Intent intent = intents[i];
                PersistableBundle persistableBundle = persistableBundles[i];
                if (persistableBundle == null) {
                    persistableBundle = new PersistableBundle();
                }

                Intent shortcutIntent = new Intent();
                shortcutIntent.setClassName(hostPackage, Constants.SHORTCUT_PROXY_ACTIVITY_NAME);
                shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);

                persistableBundle.putString("_VA_|_uri_", intent.toUri(0));
                persistableBundle.putInt("_VA_|_user_id_", 0);
                swap[i] = shortcutIntent;
            }

            System.arraycopy(swap, 0, intents, 0, length);
            mirror.android.content.pm.ShortcutInfo.mIntentPersistableExtrases.set(shortcutInfo, persistableBundles);
        }
    }

    private static class ReplacePkgAndShortcutListMethodProxy extends ReplaceCallingPkgMethodProxy {
        ReplacePkgAndShortcutListMethodProxy(String name) {
            super(name);
        }

        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {

            List<ShortcutInfo> shortcutList = findFirstShortcutList(args);
            if (shortcutList != null) {
                String hostPkg = getHostPkg();
                for (ShortcutInfo shortcutInfo : shortcutList) {
                    replaceShortcutInfo(shortcutInfo, hostPkg, getPM());
                }
            }

            return super.beforeCall(who, method, args);
        }

        @TargetApi(Build.VERSION_CODES.N_MR1)
        private List<ShortcutInfo> findFirstShortcutList(Object... args) {
            if (args == null) {
                return null;
            }
            for (Object arg : args) {
                if (arg.getClass().isAssignableFrom(ParceledListSlice.TYPE)) {
                    return ParceledListSliceCompat.getList(arg);
                }
            }
            return null;
        }
    }

    private static class ReplacePkgAndShortcutMethodProxy extends ReplaceCallingPkgMethodProxy {

        ReplacePkgAndShortcutMethodProxy(String name) {
            super(name);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {
            ShortcutInfo shortcutInfo = findFirstShortcutInfo(args);
            replaceShortcutInfo(shortcutInfo, getHostPkg(), getPM());

            return super.beforeCall(who, method, args);
        }

        @TargetApi(Build.VERSION_CODES.N_MR1)
        private ShortcutInfo findFirstShortcutInfo(Object[] args) {
            if (args == null) {
                return null;
            }
            for (Object arg : args) {
                if (arg.getClass() == mirror.android.content.pm.ShortcutInfo.TYPE) {
                    return (ShortcutInfo) arg;
                }
            }
            return null;
        }
    }
}
