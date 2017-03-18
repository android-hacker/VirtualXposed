package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class GetIntentForIntentSender extends Hook {

    @Override
    public String getName() {
        return "getIntentForIntentSender";
    }

    @Override
    public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
        Intent intent = (Intent) super.afterCall(who, method, args, result);
        if (intent != null && intent.hasExtra("_VA_|_intent_")) {
            return intent.getParcelableExtra("_VA_|_intent_");
        }
        return intent;
    }
}
