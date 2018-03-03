package com.lody.virtual.client.hook.proxies.shortcut;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.PersistableBundle;

import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;

import java.lang.reflect.Method;

import mirror.android.content.pm.IShortcutService;

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
        addMethodProxy(new ReplaceCallingPkgMethodProxy("setDynamicShortcuts"));
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

        addMethodProxy(new ReplaceCallingPkgMethodProxy("getPinnedShortcuts"));
        addMethodProxy(new ReplacePkgAndShortcutMethodProxy("requestPinShortcut"));
    }

    private static class ReplacePkgAndShortcutMethodProxy extends ReplaceCallingPkgMethodProxy {

        public ReplacePkgAndShortcutMethodProxy(String name) {
            super(name);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {
            Object shortcutInfo = findFirstShortcutInfo(args);
            if (shortcutInfo == null) {
                return false;
            }
            mirror.android.content.pm.ShortcutInfo.mPackageName.set(shortcutInfo, getHostPkg());
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
                    shortcutIntent.setClassName(getHostPkg(), Constants.SHORTCUT_PROXY_ACTIVITY_NAME);
                    shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);

                    persistableBundle.putString("_VA_|_uri_", intent.toUri(0));
                    persistableBundle.putInt("_VA_|_user_id_", 0);
                    swap[i] = shortcutIntent;
                }

                System.arraycopy(swap, 0, intents, 0, length);
                mirror.android.content.pm.ShortcutInfo.mIntentPersistableExtrases.set(shortcutInfo, persistableBundles);
            }
            return super.beforeCall(who, method, args);
        }

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
