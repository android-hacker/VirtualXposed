package com.lody.virtual.client.hook.proxies.telephony;

import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.helper.utils.Mark;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
class MethodProxies {

    @Mark("fake device id.")
    static class GetDeviceId extends ReplaceCallingPkgMethodProxy {

        public GetDeviceId() {
            super("getDeviceId");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return getDeviceInfo().deviceId;
        }
    }
}
