package io.virtualapp.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.launcher3.LauncherFiles;
import com.google.android.apps.nexuslauncher.NexusLauncherActivity;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.DeviceUtil;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.MD5Utils;
import com.lody.virtual.helper.utils.VLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.settings.SettingsActivity;
import io.virtualapp.update.VAVersionService;

import static io.virtualapp.XApp.XPOSED_INSTALLER_PACKAGE;

/**
 * @author weishu
 * @date 18/2/9.
 */

public class NewHomeActivity extends NexusLauncherActivity {

    private static final String SHOW_DOZE_ALERT_KEY = "SHOW_DOZE_ALERT_KEY";
    private static final String WALLPAPER_FILE_NAME = "wallpaper.png";

    private Handler mUiHandler;
    private boolean mDirectlyBack = false;
    private boolean checkXposedInstaller = true;

    public static void goHome(Context context) {
        Intent intent = new Intent(context, NewHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        showMenuKey();
        mUiHandler = new Handler(getMainLooper());
        alertForMeizu();
        alertForDoze();
        mDirectlyBack = sharedPreferences.getBoolean(SettingsActivity.DIRECTLY_BACK_KEY, false);
    }

    private void installXposed() {
        boolean isXposedInstalled = false;
        try {
            isXposedInstalled = VirtualCore.get().isAppInstalled(XPOSED_INSTALLER_PACKAGE);
            File oldXposedInstallerApk = getFileStreamPath("XposedInstaller_1_31.apk");
            if (oldXposedInstallerApk.exists()) {
                VirtualCore.get().uninstallPackage(XPOSED_INSTALLER_PACKAGE);
                oldXposedInstallerApk.delete();
                isXposedInstalled = false;
                Log.d(TAG, "remove xposed installer success!");
            }
        } catch (Throwable e) {
            VLog.d(TAG, "remove xposed install failed.", e);
        }

        if (!isXposedInstalled) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage(getResources().getString(R.string.prepare_xposed_installer));
            dialog.show();

            VUiKit.defer().when(() -> {
                File xposedInstallerApk = getFileStreamPath("XposedInstaller_5_8.apk");
                if (!xposedInstallerApk.exists()) {
                    InputStream input = null;
                    OutputStream output = null;
                    try {
                        input = getApplicationContext().getAssets().open("XposedInstaller_3.1.5.apk_");
                        output = new FileOutputStream(xposedInstallerApk);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = input.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }
                    } catch (Throwable e) {
                        VLog.e(TAG, "copy file error", e);
                    } finally {
                        FileUtils.closeQuietly(input);
                        FileUtils.closeQuietly(output);
                    }
                }

                if (xposedInstallerApk.isFile() && !DeviceUtil.isMeizuBelowN()) {
                    try {
                        if ("8537fb219128ead3436cc19ff35cfb2e".equals(MD5Utils.getFileMD5String(xposedInstallerApk))) {
                            VirtualCore.get().installPackage(xposedInstallerApk.getPath(), InstallStrategy.TERMINATE_IF_EXIST);
                        } else {
                            VLog.w(TAG, "unknown Xposed installer, ignore!");
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }).then((v) -> {
                dismissDialog(dialog);
            }).fail((err) -> {
                dismissDialog(dialog);
            });
        }
    }

    private static void dismissDialog(ProgressDialog dialog) {
        if (dialog == null) {
            return;
        }
        try {
            dialog.dismiss();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkXposedInstaller) {
            checkXposedInstaller = false;
            installXposed();
        }
        // check for update
        new Handler().postDelayed(() ->
                VAVersionService.checkUpdate(getApplicationContext(), false), 1000);

        // check for wallpaper
        setWallpaper();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            onSettingsClicked();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public Activity getActivity() {
        return this;
    }

    public Context getContext() {
        return this;
    }

    @Override
    public void onClickAddWidgetButton(View view) {
        onAddAppClicked();
    }

    private void onAddAppClicked() {
        ListAppActivity.gotoListApp(this);
    }

    private void onSettingsClicked() {
        startActivity(new Intent(NewHomeActivity.this, SettingsActivity.class));
    }

    @Override
    public void onClickSettingsButton(View v) {
        onSettingsClicked();
    }

    @Override
    protected void onClickAllAppsButton(View v) {
        onSettingsClicked();
    }

    @Override
    public void startVirtualActivity(Intent intent, Bundle options, int usedId) {
        String packageName = intent.getPackage();
        if (TextUtils.isEmpty(packageName)) {
            ComponentName component = intent.getComponent();
            if (component != null) {
                packageName = component.getPackageName();
            }
        }
        if (packageName == null) {
            try {
                startActivity(intent);
                return;
            } catch (Throwable ignored) {
                // ignore
            }
        }
        boolean result = LoadingActivity.launch(this, packageName, usedId);
        if (!result) {
            throw new ActivityNotFoundException("can not launch activity for :" + intent);
        }
        if (mDirectlyBack) {
            finish();
        }
    }

    private void alertForMeizu() {
        if (!DeviceUtil.isMeizuBelowN()) {
            return;
        }
        boolean isXposedInstalled = VirtualCore.get().isAppInstalled(XPOSED_INSTALLER_PACKAGE);
        if (isXposedInstalled) {
            return;
        }
        mUiHandler.postDelayed(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.meizu_device_tips_title)
                    .setMessage(R.string.meizu_device_tips_content)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    })
                    .create();
            try {
                alertDialog.show();
            } catch (Throwable ignored) {
            }
        }, 2000);
    }

    private void alertForDoze() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager == null) {
            return;
        }
        boolean showAlert = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SHOW_DOZE_ALERT_KEY, true);
        if (!showAlert) {
            return;
        }
        String packageName = getPackageName();
        boolean ignoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName);
        if (!ignoringBatteryOptimizations) {

            mUiHandler.postDelayed(() -> {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.alert_for_doze_mode_title)
                        .setMessage(R.string.alert_for_doze_mode_content)
                        .setPositiveButton(R.string.alert_for_doze_mode_yes, (dialog, which) -> {
                            try {
                                startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName())));
                            } catch (ActivityNotFoundException ignored) {
                                // ActivityNotFoundException on some devices.
                                try {
                                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                                } catch (Throwable e) {
                                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                                            .edit().putBoolean(SHOW_DOZE_ALERT_KEY, false).apply();
                                }
                            } catch (Throwable e) {
                                PreferenceManager.getDefaultSharedPreferences(getActivity())
                                        .edit().putBoolean(SHOW_DOZE_ALERT_KEY, false).apply();
                            }
                        })
                        .setNegativeButton(R.string.alert_for_doze_mode_no, (dialog, which) ->
                                PreferenceManager.getDefaultSharedPreferences(getActivity())
                                        .edit().putBoolean(SHOW_DOZE_ALERT_KEY, false).apply())
                        .create();
                try {
                    alertDialog.show();
                } catch (Throwable ignored) {
                    ignored.printStackTrace();
                }
            }, 1000);
        }
    }

    private void setWallpaper() {
        File wallpaper = getFileStreamPath(WALLPAPER_FILE_NAME);
        if (wallpaper == null || !wallpaper.exists() || wallpaper.isDirectory()) {
            setOurWallpaper(getResources().getDrawable(R.drawable.home_bg));
        } else {
            long start = SystemClock.elapsedRealtime();
            Drawable d;
            try {
                d = BitmapDrawable.createFromPath(wallpaper.getPath());
            } catch (Throwable e) {
                Toast.makeText(getApplicationContext(), R.string.wallpaper_too_big_tips, Toast.LENGTH_SHORT).show();
                return;
            }
            long cost = SystemClock.elapsedRealtime() - start;
            if (cost > 200) {
                Toast.makeText(getApplicationContext(), R.string.wallpaper_too_big_tips, Toast.LENGTH_SHORT).show();
            }
            if (d == null) {
                setOurWallpaper(getResources().getDrawable(R.drawable.home_bg));
            } else {
                setOurWallpaper(d);
            }
        }
    }

    private void showMenuKey() {
        try {
            Method setNeedsMenuKey = Window.class.getDeclaredMethod("setNeedsMenuKey", int.class);
            setNeedsMenuKey.setAccessible(true);
            int value = WindowManager.LayoutParams.class.getField("NEEDS_MENU_SET_TRUE").getInt(null);
            setNeedsMenuKey.invoke(getWindow(), value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
