package com.lody.virtual.client.hook.patchs.phonesubinfo;


import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.DelegateResult;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;

import java.lang.reflect.Method;

class Hook_GetDeviceId extends ReplaceCallingPkgHook {

    public Hook_GetDeviceId(){
        super("getDeviceId");
    }

    @Override
    public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
        if (VirtualCore.get().getPhoneInfoDelegate() != null) {
            DelegateResult<String> o = VirtualCore.get().getPhoneInfoDelegate().getDeviceId((String) result);
            if (o != null) {
                result = o.getValue();
            }
        }
        return super.afterCall(who, method, args, result);
    }
}