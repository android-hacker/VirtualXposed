package com.lody.virtual.helper.utils;

import java.io.File;

public class ResourcesUtils {

    public static void chmod(File dir) {
        try {
            Runtime.getRuntime().exec("chmod -R 755 " + dir.getAbsolutePath()).waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
