package com.lody.virtual.helper.compat;

import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.InvocationTargetException;

public class SystemPropertiesCompat {

    private static Class<?> sClass;

    public SystemPropertiesCompat() {
    }

    private static Class getSystemPropertiesClass() throws ClassNotFoundException {
        if (sClass == null) {
            sClass = Class.forName("android.os.SystemProperties");
        }
        return sClass;
    }

    private static String getInner(String key, String defaultValue)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, ClassNotFoundException {
        Class clazz = getSystemPropertiesClass();
        return (String) Reflect.on(clazz).call("get", key, defaultValue).get();
    }

    public static String get(String key, String defaultValue) {
        try {
            return getInner(key, defaultValue);
        } catch (Exception var3) {
            var3.printStackTrace();
            return defaultValue;
        }
    }

}
