package com.lody.virtual.client.hook.proxies.telephony;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

class MethodProxies {

    static class GetDeviceId extends ReplaceCallingPkgMethodProxy {

        public GetDeviceId() {
            super("getDeviceId");
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            if (VirtualCore.get().getPhoneInfoDelegate() != null) {
                String res = VirtualCore.get().getPhoneInfoDelegate().getDeviceId((String) result, getAppUserId());
                if (res != null) {
                    return res;
                }
            }
            return super.afterCall(who, method, args, result);
        }
    }
}
