package com.lody.virtual.helper.utils;

import android.util.Base64;

/**
 * @author weishu
 * @date 18/3/20.
 */
public class EncodeUtils {

    public static String decode(String base64) {
        return new String(Base64.decode(base64, 0));
    }
}
