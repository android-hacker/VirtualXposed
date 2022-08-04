package io.virtualapp.gms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.remote.InstallResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.utils.DialogUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author weishu
 * @date 2018/6/9.
 */
public class FakeGms {

    private static final String TAG = "FakeGms";

    private static final String GMS_CONFIG_URL = "http://vaexposed.weishu.me/gms.json";

    private static final String GMS_PKG = "com.google.android.gms";
    private static final String GSF_PKG = "com.google.android.gsf";
    private static final String STORE_PKG = "com.android.vending";
    private static final String FAKE_GAPPS_PKG = "com.thermatk.android.xf.fakegapps";

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void uninstallGms(Activity activity) {
        if (activity == null) {
            return;
        }

        AlertDialog failDialog = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                .setTitle(R.string.uninstall_gms_title)
                .setMessage(R.string.uninstall_gms_content)
                .setPositiveButton(R.string.uninstall_gms_ok, ((dialog1, which1) -> {
                    ProgressDialog dialog = new ProgressDialog(activity);
                    dialog.show();
                    VUiKit.defer().when(() -> {
                        VirtualCore.get().uninstallPackage(GMS_PKG);
                        VirtualCore.get().uninstallPackage(GSF_PKG);
                        VirtualCore.get().uninstallPackage(STORE_PKG);
                        VirtualCore.get().uninstallPackage(FAKE_GAPPS_PKG);
                    }).then((v) -> {
                        dialog.dismiss();
                        AlertDialog hits = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                                .setTitle(R.string.uninstall_gms_title)
                                .setMessage(R.string.uninstall_gms_success)
                                .setPositiveButton(android.R.string.ok, null)
                                .create();
                        DialogUtil.showDialog(hits);

                    }).fail((v) -> {
                        dialog.dismiss();
                    });

                }))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        DialogUtil.showDialog(failDialog);
    }

    public static boolean isAlreadyInstalled(Context context) {
        if (context == null) {
            return false;
        }

        boolean alreadyInstalled = true;
        if (!VirtualCore.get().isAppInstalled(GMS_PKG)) {
            alreadyInstalled = false;
        }
        if (!VirtualCore.get().isAppInstalled(GSF_PKG)) {
            alreadyInstalled = false;
        }
        if (!VirtualCore.get().isAppInstalled(STORE_PKG)) {
            alreadyInstalled = false;
        }
        if (!VirtualCore.get().isAppInstalled(FAKE_GAPPS_PKG)) {
            alreadyInstalled = false;
        }
        return alreadyInstalled;
    }

    public static void installGms(Activity activity) {

        if (activity == null) {
            return;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                .setTitle(R.string.install_gms_title)
                .setMessage(R.string.install_gms_content)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    // show a loading dialog and start install gms.

                    ProgressDialog progressDialog = new ProgressDialog(activity);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    executorService.submit(() -> {
                        String failMsg = installGmsInternal(activity, progressDialog);
                        Log.i(TAG, "install gms result: " + failMsg);
                        try {
                            progressDialog.dismiss();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        if (failMsg == null) {
                            activity.runOnUiThread(() -> {
                                AlertDialog failDialog = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                                        .setTitle(R.string.install_gms_title)
                                        .setMessage(R.string.install_gms_success)
                                        .setPositiveButton(android.R.string.ok, null)
                                        .create();
                                DialogUtil.showDialog(failDialog);
                            });
                        } else {
                            activity.runOnUiThread(() -> {
                                AlertDialog failDialog = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                                        .setTitle(R.string.install_gms_fail_title)
                                        .setMessage(R.string.install_gms_fail_content)
                                        .setPositiveButton(R.string.install_gms_fail_ok, ((dialog1, which1) -> {
                                            try {
                                                Intent t = new Intent(Intent.ACTION_VIEW);
                                                t.setData(Uri.parse("https://github.com/android-hacker/VirtualXposed/wiki/Google-service-support"));
                                                activity.startActivity(t);
                                            } catch (Throwable ignored) {
                                                ignored.printStackTrace();
                                            }
                                        }))
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create();
                                DialogUtil.showDialog(failDialog);
                            });

                        }
                    });
                }))
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        DialogUtil.showDialog(alertDialog);
    }


    private static String installGmsInternal(Activity activity, ProgressDialog dialog) {
        File cacheDir = activity.getCacheDir();

        // 下载配置文件，得到各自的URL
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(GMS_CONFIG_URL)
                .build();

        updateMessage(activity, dialog, "Fetching gms config...");
        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            return "Download gms config failed, please check your network, error: 0";
        }

        if (!response.isSuccessful()) {
            return "Download gms config failed, please check your network, error: 1";
        }

        Log.i(TAG, "response success: " + response.code());
        if (200 != response.code()) {
            return "Download gms config failed, please check your network, error: 2";
        }

        updateMessage(activity, dialog, "Parsing gms config...");
        ResponseBody body = response.body();
        if (body == null) {
            return "Download gms config failed, please check your network, error: 3";
        }

        String string = null;
        try {
            string = body.string();
        } catch (IOException e) {
            return "Download gms config failed, please check your network, error: 4";
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(string);
        } catch (JSONException e) {
            return "Download gms config failed, please check your network, error: 5";
        }
        String gmsCoreUrl = null;
        try {
            gmsCoreUrl = jsonObject.getString("gms");
        } catch (JSONException e) {
            return "Download gms config failed, please check your network, error: 6";
        }
        String gmsServiceUrl = null;
        try {
            gmsServiceUrl = jsonObject.getString("gsf");
        } catch (JSONException e) {
            return "Download gms config failed, please check your network, error: 7";
        }
        String storeUrl = null;
        try {
            storeUrl = jsonObject.getString("store");
        } catch (JSONException e) {
            return "Download gms config failed, please check your network, error: 8";
        }
        String fakeGappsUrl = null;
        try {
            fakeGappsUrl = jsonObject.getString("fakegapps");
        } catch (JSONException e) {
            return "Download gms config failed, please check your network, error: 9";
        }

        String yalpStoreUrl = null;
        try {
            yalpStoreUrl = jsonObject.getString("yalp");
        } catch (JSONException e) {
            // ignore.
            Log.i(TAG, "Download gms config failed, please check your network");
        }

        updateMessage(activity, dialog, "config parse success!");

        File gmsCoreFile = new File(cacheDir, "gms.apk");
        File gmsServiceFile = new File(cacheDir, "gsf.apk");
        File storeFile = new File(cacheDir, "store.apk");
        File fakeGappsFile = new File(cacheDir, "fakegapps.apk");
        File yalpStoreFile = new File(cacheDir, "yalpStore.apk");

        // clear old files.
        if (gmsCoreFile.exists()) {
            gmsCoreFile.delete();
        }
        if (gmsServiceFile.exists()) {
            gmsServiceFile.delete();
        }
        if (storeFile.exists()) {
            storeFile.delete();
        }
        if (fakeGappsFile.exists()) {
            fakeGappsFile.delete();
        }

        boolean downloadResult = downloadFile(gmsCoreUrl, gmsCoreFile,
                (progress) -> updateMessage(activity, dialog, "download gms core..." + progress + "%"));
        if (!downloadResult) {
            return "Download gms config failed, please check your network, error: 10";
        }

        downloadResult = downloadFile(gmsServiceUrl, gmsServiceFile,
                (progress -> updateMessage(activity, dialog, "download gms service framework proxy.." + progress + "%")));

        if (!downloadResult) {
            return "Download gms config failed, please check your network, error: 11";
        }

        updateMessage(activity, dialog, "download gms store...");

        downloadResult = downloadFile(storeUrl, storeFile,
                (progress -> updateMessage(activity, dialog, "download gms store.." + progress + "%")));
        if (!downloadResult) {
            return "Download gms config failed, please check your network, error: 12";
        }

        downloadResult = downloadFile(fakeGappsUrl, fakeGappsFile,
                (progress -> updateMessage(activity, dialog, "download gms Xposed module.." + progress + "%")));
        if (!downloadResult) {
            return "Download gms config failed, please check your network, error: 13";
        }

        if (yalpStoreUrl != null) {
            downloadFile(yalpStoreUrl,yalpStoreFile,
                    (progress -> updateMessage(activity, dialog, "download yalp store.." + progress + "%")));
        }

        updateMessage(activity, dialog, "installing gms core");
        InstallResult installResult = VirtualCore.get().installPackage(gmsCoreFile.getAbsolutePath(), InstallStrategy.UPDATE_IF_EXIST);

        if (!installResult.isSuccess) {
            return "install gms core failed: " + installResult.error;
        }

        updateMessage(activity, dialog, "installing gms service framework...");
        installResult = VirtualCore.get().installPackage(gmsServiceFile.getAbsolutePath(), InstallStrategy.UPDATE_IF_EXIST);
        if (!installResult.isSuccess) {
            return "install gms service framework failed: " + installResult.error;
        }

        updateMessage(activity, dialog, "installing gms store...");
        installResult = VirtualCore.get().installPackage(storeFile.getAbsolutePath(), InstallStrategy.UPDATE_IF_EXIST);
        if (!installResult.isSuccess) {
            return "install gms store failed: " + installResult.error;
        }

        updateMessage(activity, dialog, "installing gms Xposed module...");
        installResult = VirtualCore.get().installPackage(fakeGappsFile.getAbsolutePath(), InstallStrategy.UPDATE_IF_EXIST);
        if (!installResult.isSuccess) {
            return "install gms xposed module failed: " + installResult.error;
        }

        if (yalpStoreFile.exists()) {
            updateMessage(activity, dialog, "installing yalp store...");
            VirtualCore.get().installPackage(yalpStoreFile.getAbsolutePath(), InstallStrategy.UPDATE_IF_EXIST);
        }

        // Enable the Xposed module.
        File dataDir = VEnvironment.getDataUserPackageDirectory(0, "de.robv.android.xposed.installer");
        File modulePath = VEnvironment.getPackageResourcePath(FAKE_GAPPS_PKG);
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
        // success!!!
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

    public interface DownloadListener {
        void onProgress(int progress);
    }

    public static boolean downloadFile(String url, File outFile, DownloadListener listener) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        FileOutputStream fos = null;
        try {
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                return false;
            }
            ResponseBody body = response.body();
            if (body == null) {
                return false;
            }
            long toal = body.contentLength();
            long sum = 0;

            InputStream inputStream = body.byteStream();
            fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(buffer)) >= 0) {
                fos.write(buffer, 0, count);
                sum += count;
                int progress = (int) ((sum * 1.0) / toal * 100);
                if (listener != null) {
                    listener.onProgress(progress);
                }
            }
            fos.flush();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
