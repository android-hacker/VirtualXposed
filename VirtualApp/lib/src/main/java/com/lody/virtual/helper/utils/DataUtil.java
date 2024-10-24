package com.lody.virtual.helper.utils;

public class DataUtil {

    /**
     * Since android 13(33), many types have changed to Long, and this function is suitable for this scenario
     * @param data
     * @return
     */
    public static int safeToInt(Object data) {
        if (data instanceof Long) {
            return ((Long) data).intValue();
        } else if (data instanceof Integer) {
            return (int) data;
        }
        return (int) data;
    }
}
