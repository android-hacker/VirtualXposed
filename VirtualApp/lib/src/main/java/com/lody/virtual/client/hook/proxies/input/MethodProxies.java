package com.lody.virtual.client.hook.proxies.input;

import android.view.inputmethod.EditorInfo;

import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

class MethodProxies {

    static class StartInput extends StartInputOrWindowGainedFocus {

        @Override
        public String getMethodName() {
            return "startInput";
        }
    }

    static class WindowGainedFocus extends StartInputOrWindowGainedFocus {

        @Override
        public String getMethodName() {
            return "windowGainedFocus";
        }


    }

    static class StartInputOrWindowGainedFocus extends MethodProxy {


        @Override
        public String getMethodName() {
            return "startInputOrWindowGainedFocus";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            int editorInfoIndex = ArrayUtils.indexOfFirst(args, EditorInfo.class);
            if (editorInfoIndex != -1) {
                EditorInfo attribute = (EditorInfo) args[editorInfoIndex];
                attribute.packageName = getHostPkg();
            }
            return method.invoke(who, args);
        }
    }
}
