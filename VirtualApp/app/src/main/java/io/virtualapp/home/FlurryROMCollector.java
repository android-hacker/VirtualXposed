package io.virtualapp.home;

import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.lody.virtual.client.natives.NativeMethods;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import me.weishu.epic.art.Epic;

/**
 * @author Lody
 */
public class FlurryROMCollector {

    private static final String TAG = FlurryROMCollector.class.getSimpleName();

    public static void startCollect() {
        Log.i(TAG, "start collect...");
        NativeMethods.init();
        if (NativeMethods.gCameraNativeSetup == null) {
            reportCameraNativeSetup();
        }
        Map<String, Integer> offsetInfo = Epic.collectOffsetInfo();
        if (!offsetInfo.isEmpty()) {
            reportOffsetInfo(offsetInfo);
        }
        Log.i(TAG, "end collect...");
    }

    private static void reportOffsetInfo(Map<String, Integer> info) {
        Map<String, String> toReport = new HashMap<>();
        CustomEvent methodOffset = new CustomEvent("methodOffset");
        for (String key : info.keySet()) {
            toReport.put(key, String.valueOf(info.get(key)));
        }
        addRomInfo(toReport);
        Answers.getInstance().logCustom(methodOffset);
    }

    private static void reportCameraNativeSetup() {
        for (Method method : Camera.class.getDeclaredMethods()) {
            if ("native_setup".equals(method.getName())) {
                CustomEvent cameraSetup = new CustomEvent("camera::native_setup");
                Map<String, String> methodDetails = createLogContent("method_details", Reflect.getMethodDetails(method));
                for (String key : methodDetails.keySet()) {
                    cameraSetup.putCustomAttribute(key, methodDetails.get(key));
                }
                Answers.getInstance().logCustom(cameraSetup);
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
