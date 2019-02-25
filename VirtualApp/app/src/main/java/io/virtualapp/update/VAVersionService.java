package io.virtualapp.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.allenliu.versionchecklib.core.AVersionService;
import com.allenliu.versionchecklib.core.AllenChecker;
import com.allenliu.versionchecklib.core.VersionParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.virtualapp.R;

/**
 * @author weishu
 * @date 18/1/4.
 */

public class VAVersionService extends AVersionService {
    private static final String TAG = "VAVersionService";

    private static final long CHECK_INTERVAL = TimeUnit.HOURS.toMillis(1);

    private static final String KEY_SHOW_TIP = "show_tips";

    private static long sLastCheckTime;

    static {
        AllenChecker.init(false);
    }

    public static final String CHECK_VERION_URL = "http://vaexposed.weishu.me/update.json";

    @Override
    public void onResponses(AVersionService service, String response) {
        try {
            JSONObject versionInfo = new JSONObject(response);
//            {
//                url: "download url",
//                versionCode: 3,
//                updateMessage: "Android 7.0"
//            }
            String url = versionInfo.getString("url");
            int versionCode = versionInfo.getInt("versionCode");
            String updateMessage = versionInfo.getString("updateMessage");

            int currentVersion = getCurrentVersionCode(this);
            if (currentVersion < versionCode) {
                showVersionDialog(url, getResources().getString(R.string.new_version_detected), updateMessage);
            } else {
                boolean showTip = versionParams != null && versionParams.getParamBundle() != null
                        && versionParams.getParamBundle().getBoolean(KEY_SHOW_TIP, false);
                if (showTip) {
                    Toast.makeText(getApplicationContext(), R.string.version_is_latest, Toast.LENGTH_SHORT).show();
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "version info parse error!!", e);
        } catch (Throwable e) {
            Log.e(TAG, "check version failed:", e);
        } finally {
            stopSelf();
        }
    }

    public static void checkUpdateImmediately(Context context, boolean showTip) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_SHOW_TIP, showTip);

        VersionParams.Builder builder = new VersionParams.Builder()
                .setRequestUrl(CHECK_VERION_URL)
                .setShowDownloadingDialog(false)
                .setParamBundle(bundle)
                .setService(VAVersionService.class);

        AllenChecker.startVersionCheck(context, builder.build());
    }

    public static void checkUpdate(Context context, boolean showTip) {
        long now = SystemClock.elapsedRealtime();
        if (now - sLastCheckTime > CHECK_INTERVAL) {
            checkUpdateImmediately(context, showTip);
            sLastCheckTime = now;
        }
    }

    private static int getCurrentVersionCode(Context context) {
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return -1;
    }
}
