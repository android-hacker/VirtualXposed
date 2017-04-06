package io.virtualapp.home;

import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryEventRecordStatus;
import com.lody.virtual.client.natives.NativeMethods;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */
public class FlurryROMCollector {

    private static final String TAG = FlurryROMCollector.class.getSimpleName();

    public static void startCollect() {
        Log.d(TAG, "start collect...");
        NativeMethods.init();
        if (NativeMethods.gCameraNativeSetup == null) {
            reportCameraNativeSetup();
        }
        Log.d(TAG, "end collect...");
    }


    private static void reportCameraNativeSetup() {
        for (Method method : Camera.class.getDeclaredMethods()) {
            if ("native_setup".equals(method.getName())) {
                FlurryEventRecordStatus status =
                        FlurryAgent.logEvent("camera::native_setup", createLogContent("method_details", Reflect.getMethodDetails(method)));
                Log.d(TAG, "report CNS: " + status);
                break;
            }
        }
    }

    private static Map<String, String> createLogContent(String tag, String value) {
        Map<String, String> content = new HashMap<>(3);
        addRomInfo(content);
        content.put(tag, value);
        return content;
    }


    private static void addRomInfo(Map<String, String> content) {
        content.put("device", Build.DEVICE);
        content.put("brand", Build.BRAND);
        content.put("manufacturer", Build.MANUFACTURER);
        content.put("display", Build.DISPLAY);
        content.put("model", Build.MODEL);
        content.put("protect", Build.PRODUCT);
        content.put("sdk_version", "API-" + Build.VERSION.SDK_INT);
    }
}
