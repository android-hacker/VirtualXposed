package com.lody.virtual.client.hook.patchs.vibrator;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;

import java.lang.reflect.Method;

import mirror.com.android.internal.os.IVibratorService;

/**
 * @author Lody
 *
 * @see android.os.Vibrator
 */
public class VibratorPatch extends PatchBinderDelegate {

    public VibratorPatch() {
        super(IVibratorService.Stub.TYPE, Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onBindHooks() {
        //Samsung
        addHook(new ReplaceCallingPkgHook("vibrateMagnitude") {
            @Override
            public boolean beforeCall(Object who, Method method, Object... args) {
                if (args[0] instanceof Integer) {
                    args[0] = getRealUid();
                }
                return super.beforeCall(who, method, args);
            }
        });
        //Samsung
        addHook(new ReplaceCallingPkgHook("vibratePatternMagnitude") {
            @Override
            public boolean beforeCall(Object who, Method method, Object... args) {
                if (args[0] instanceof Integer) {
                    args[0] = getRealUid();
                }
                return super.beforeCall(who, method, args);
            }
        });
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
}
