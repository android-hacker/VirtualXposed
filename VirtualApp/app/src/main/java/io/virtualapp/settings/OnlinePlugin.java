package io.virtualapp.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.remote.InstallResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.virtualapp.R;
import io.virtualapp.gms.FakeGms;
import io.virtualapp.home.LoadingActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static io.virtualapp.utils.DialogUtil.showDialog;

/**
 * @author weishu
 * @date 2018/7/5.
 */
public class OnlinePlugin {

    private static final String TAG = "OnlinePlugin";

    public static final String FILE_MANAGE_PACKAGE = "com.amaze.filemanager";
    public static final String FILE_MANAGE_URL = "http://vaexposed.weishu.me/amaze.json";

    public static final String PERMISSION_MANAGE_PACKAGE = "eu.faircode.xlua";
    public static final String PERMISSION_MANAGE_URL = "http://vaexposed.weishu.me/xlua.json";

    public static void openOrDownload(Activity context, String packageName, String url, String tips) {
        if (context == null || packageName == null) {
            return;
        }

        if (VirtualCore.get().isAppInstalled(packageName)) {
            LoadingActivity.launch(context, packageName, 0);
            return;
        }

        AlertDialog failDialog = new AlertDialog.Builder(context, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(tips)
                .setPositiveButton(android.R.string.ok, ((dialog1, which1) -> {
                    ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    Executors.newSingleThreadExecutor().submit(() -> {
                        String error = downloadAndInstall(context, progressDialog, url, packageName);
                        try {
                            progressDialog.dismiss();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        if (error == null) {
                            context.runOnUiThread(() -> {
                                LoadingActivity.launch(context, packageName, 0);
                            });
                        } else {
                            context.runOnUiThread(() -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show());
                        }

                    });

                }))
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        showDialog(failDialog);
    }

    private static String downloadAndInstall(Activity activity, ProgressDialog dialog, String url, String packageName) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        updateMessage(activity, dialog, "Prepare download...");
        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            return "Download failed, please check your network, error: 0";
        }

        if (!response.isSuccessful()) {
            return "Download failed, please check your network, error: 1";
        }

        Log.i(TAG, "response success: " + response.code());
        if (200 != response.code()) {
            return "Download failed, please check your network, error: 2";
        }

        updateMessage(activity, dialog, "Parsing config...");
        ResponseBody body = response.body();
        if (body == null) {
            return "Download failed, please check your network, error: 3";
        }

        String string;
        try {
            string = body.string();
        } catch (IOException e) {
            return "Download failed, please check your network, error: 4";
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(string);
        } catch (JSONException e) {
            return "Download failed, please check your network, error: 5";
        }
        String downloadLink;
        boolean isXposed;
        try {
            downloadLink = jsonObject.getString("url");
            isXposed = jsonObject.optBoolean("xposed", false);
        } catch (JSONException e) {
            return "Download failed, please check your network, error: 6";
        }

        File outFile = new File(activity.getCacheDir(), packageName + ".apk");
        FakeGms.downloadFile(downloadLink, outFile, progress -> updateMessage(activity, dialog, "download " + packageName + "..." + progress + "%"));

        updateMessage(activity, dialog, "installing " + packageName);

        InstallResult installResult = VirtualCore.get().installPackage(outFile.getAbsolutePath(), InstallStrategy.UPDATE_IF_EXIST);
        if (!installResult.isSuccess) {
            return "install " + packageName + " failed: " + installResult.error;
        }

        if (isXposed) {
            // Enable the Xposed module.
            updateMessage(activity, dialog, "enable " + packageName + " in Xposed Installer");

            File dataDir = VEnvironment.getDataUserPackageDirectory(0, "de.robv.android.xposed.installer");
            File modulePath = VEnvironment.getPackageResourcePath(packageName);
            File configDir = new File(dataDir, "exposed_conf" + File.separator + "modules.list");
            FileWriter writer = null;
            try {
                writer = new FileWriter(configDir, true);
                writer.append(modulePath.getAbsolutePath());
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        updateMessage(activity, dialog, " install success!!");
        SystemClock.sleep(300);

        return null;
    }

    private static void updateMessage(Activity activity, ProgressDialog dialog, String msg) {
        if (activity == null || dialog == null || TextUtils.isEmpty(msg)) {
            return;
        }
        Log.i(TAG, "update dialog message: " + msg);
        activity.runOnUiThread(() -> {
            dialog.setMessage(msg);
        });
    }
}
