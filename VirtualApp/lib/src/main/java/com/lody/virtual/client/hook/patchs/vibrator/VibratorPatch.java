package com.lody.virtual.client.hook.patchs.vibrator;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.binders.VibratorBinderDelegate;

import java.lang.reflect.Method;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 * @see android.os.Vibrator
 */
public class VibratorPatch extends PatchDelegate<VibratorBinderDelegate> {

    @Override
    protected VibratorBinderDelegate createHookDelegate() {
        return new VibratorBinderDelegate();
    }

    @Override
    public void inject() throws Throwable {
        getHookDelegate().replaceService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onBindHooks() {
        addHook(new ReplaceCallingPkgHook("vibrate") {
            @Override
            public boolean beforeCall(Object who, Method method, Object... args) {
                if (args[0] instanceof Integer) {
                    args[0] = getRealUid();
                }
                return super.beforeCall(who, method, args);
            }
        });
        addHook(new ReplaceCallingPkgHook("vibratePattern") {
            @Override
            public boolean beforeCall(Object who, Method method, Object... args) {
                if (args[0] instanceof Integer) {
                    args[0] = getRealUid();
                }
                return super.beforeCall(who, method, args);
            }
        });
    }

    @Override
    public boolean isEnvBad() {
        return getHookDelegate() != ServiceManager.getService.call(Context.VIBRATOR_SERVICE);
    }

}
