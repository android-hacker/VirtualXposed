package com.lody.virtual.client.hook.patchs.vibrator;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;

import java.lang.reflect.Method;

import mirror.com.android.internal.os.IVibratorService;

/**
 * @author Lody
 * @see android.os.Vibrator
 */
public class VibratorPatch extends PatchBinderDelegate {

    public VibratorPatch() {
        super(IVibratorService.Stub.asInterface, Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onBindHooks() {
        //Samsung  {
        addHook(new VibrateHook("vibrateMagnitude"));
        addHook(new VibrateHook("vibratePatternMagnitude"));
        // }
        addHook(new VibrateHook("vibrate"));
        addHook(new VibrateHook("vibratePattern"));
    }

    private final static class VibrateHook extends ReplaceCallingPkgHook {

        private VibrateHook(String name) {
            super(name);
        }

        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {
            if (args[0] instanceof Integer) {
                args[0] = getRealUid();
            }
            return super.beforeCall(who, method, args);
        }
    }
}
