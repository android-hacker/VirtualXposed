package com.lody.virtual.client.hook.base;

import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class ReplaceSequencePkgHook extends StaticHook {

    private int sequence;

    public ReplaceSequencePkgHook(String name, int sequence) {
        super(name);
        this.sequence = sequence;
    }

    @Override
    public boolean beforeHook(Object who, Method method, Object... args) {
        HookUtils.replaceSequenceAppPkg(args, sequence);
        return super.beforeHook(who, method, args);
    }
}
