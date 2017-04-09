package com.lody.virtual.client.hook.proxies.input;

import android.view.inputmethod.EditorInfo;

import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

class MethodProxies {

    static class StartInput extends MethodProxy {

        @Override
        public String getMethodName() {
            return "startInput";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (args.length > 2 && args[2] instanceof EditorInfo) {
                EditorInfo attribute = (EditorInfo) args[2];
                attribute.packageName = getHostPkg();
            }
            return method.invoke(who, args);
        }

    }

    static class WindowGainedFocus extends MethodProxy {

        private Boolean noEditorInfo = null;
        private int editorInfoIndex = -1;

        @Override
        public String getMethodName() {
            return "windowGainedFocus";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (noEditorInfo == null) {
                editorInfoIndex = ArrayUtils.indexOfFirst(args, EditorInfo.class);
                noEditorInfo = editorInfoIndex == -1;
            }
            if (!noEditorInfo) {
                EditorInfo attribute = (EditorInfo) args[editorInfoIndex];
                if (attribute != null) {
                    attribute.packageName = getHostPkg();
                }
            }
            return method.invoke(who, args);
        }

    }
}
