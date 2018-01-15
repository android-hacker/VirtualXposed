package io.virtualapp.update;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.allenliu.versionchecklib.core.AVersionService;
import com.allenliu.versionchecklib.core.AllenChecker;
import com.allenliu.versionchecklib.core.VersionParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author weishu
 * @date 18/1/4.
 */

public class VAVersionService extends AVersionService {
    private static final String TAG = "VAVersionService";

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
                showVersionDialog(url, "VAExposed 更新啦: ", updateMessage);
            }
        } catch (JSONException e) {
            Log.e(TAG, "version info parse error!!", e);
        } catch (Throwable e) {
            Log.e(TAG, "check version failed:", e);
        }
    }

    public static void checkUpdate(Application context) {
        VersionParams.Builder builder = new VersionParams.Builder()
                .setRequestUrl(CHECK_VERION_URL)
                .setShowDownloadingDialog(false)
                .setService(VAVersionService.class);

        AllenChecker.startVersionCheck(context, builder.build());
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
