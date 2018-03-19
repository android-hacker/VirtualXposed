package io.virtualapp.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.view.View;
import android.widget.Toast;

import com.google.android.apps.nexuslauncher.NexusLauncherActivity;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.DeviceUtil;
import com.lody.virtual.remote.InstalledAppInfo;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.virtualapp.R;
import io.virtualapp.VApp;
import io.virtualapp.VCommends;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.settings.SettingsActivity;
import io.virtualapp.update.VAVersionService;

/**
 * @author weishu
 * @date 18/2/9.
 */

public class NewHomeActivity extends NexusLauncherActivity implements HomeContract.HomeView {

    private static final String SHOW_DOZE_ALERT_KEY = "SHOW_DOZE_ALERT_KEY";
    private static final String WALLPAPER_FILE_NAME = "wallpaper.png";

    private HomeContract.HomePresenter mPresenter;
    private Handler mUiHandler;
    private int mInstallCount = 0;

    public static void goHome(Context context) {
        Intent intent = new Intent(context, NewHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUiHandler = new Handler(getMainLooper());
        getHotseat().setAddAppClickListener(v -> onAddAppClicked());
        getHotseat().setSettingClickListener(v -> onSettingsClicked());

        new HomePresenterImpl(this).start();

        alertForMeizu();
        alertForDoze();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check for update
        new Handler().postDelayed(() ->
                VAVersionService.checkUpdate(getApplicationContext(), false), 1000);

        // check for wallpaper
        setWallpaper();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void setPresenter(HomeContract.HomePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showGuide() {
        // no-op
    }

    @Override
    public void refreshLauncherItem(AppData model) {
        refreshLoadingDialog(model);
    }

    @Override
    public void askInstallGms() {
        // no-op
    }

    @Override
    public void onClickAddWidgetButton(View view) {
        // Add App
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
        // super.onClickSettingsButton(v);
        onSettingsClicked();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!(resultCode == RESULT_OK && data != null)) {
            return;
        }
        List<AppInfoLite> appList = data.getParcelableArrayListExtra(VCommends.EXTRA_APP_INFO_LIST);
        if (appList != null) {
            boolean showTip = false;
            int size = appList.size();
            mInstallCount = size;

            if (dealUpdate(appList)) {
                return;
            }

            for (int i = 0; i < size; i++) {
                AppInfoLite info = appList.get(i);
                if (new File(info.path).length() > 1024 * 1024 * 24) {
                    showTip = true;
                }
                mPresenter.addApp(info);
            }
            if (showTip) {
                Toast.makeText(this, R.string.large_app_install_tips, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private boolean dealUpdate(List<AppInfoLite> appList) {
        if (appList == null || appList.size() != 1) {
            return false;
        }
        AppInfoLite appInfoLite = appList.get(0);
        if (appInfoLite == null) {
            return false;
        }
        if (appInfoLite.disableMultiVersion) {
            return false;
        }
        InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(appInfoLite.packageName, 0);
        if (installedAppInfo == null) {
            return false;
        }
        String currentVersion;
        String toInstalledVersion;
        int currentVersionCode;
        int toInstalledVersionCode;
        PackageManager packageManager = getPackageManager();
        if (packageManager == null) {
            return false;
        }
        try {
            PackageInfo applicationInfo = installedAppInfo.getPackageInfo(0);
            currentVersion = applicationInfo.versionName;
            currentVersionCode = applicationInfo.versionCode;

            PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(appInfoLite.path, 0);
            toInstalledVersion = packageArchiveInfo.versionName;
            toInstalledVersionCode = packageArchiveInfo.versionCode;

            String multiVersionUpdate = getResources().getString(currentVersionCode == toInstalledVersionCode ? R.string.multi_version_cover : (
                    currentVersionCode < toInstalledVersionCode ? R.string.multi_version_upgrade : R.string.multi_version_downgrade
            ));
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.multi_version_tip_title)
                    .setMessage(getResources().getString(R.string.multi_version_tips_content, currentVersion, toInstalledVersion))
                    .setPositiveButton(R.string.multi_version_multi, (dialog, which) -> {
                        mPresenter.addApp(appInfoLite);
                    })
                    .setNegativeButton(multiVersionUpdate, ((dialog, which) -> {
                        appInfoLite.disableMultiVersion = true;
                        mPresenter.addApp(appInfoLite);
                    }))
                    .create();
            alertDialog.show();
        } catch (Throwable ignored) {
            return false;
        }
        return true;
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
        LoadingActivity.launch(this, packageName, usedId);
    }

    private LoadingDialog mLoadingDialog;

    private void refreshLoadingDialog(AppData model) {
        runOnUiThread(() -> {
            if (mLoadingDialog == null) {
                mLoadingDialog = new LoadingDialog(NewHomeActivity.this);
                mUiHandler.postDelayed(() -> mLoadingDialog.dismiss(), TimeUnit.MINUTES.toMillis(6));
            }
            if (model.isInstalling()) {
                mLoadingDialog.setTitle(getResources().getString(R.string.add_app_installing_tips, model.getName()));
                mLoadingDialog.show();
                mUiHandler.postDelayed(() -> mLoadingDialog.startLoading(), 30);
            } else if (model.isLoading()) {
                mLoadingDialog.setTitle(getResources().getString(R.string.add_app_loading_tips, model.getName()));
                mLoadingDialog.show();
                mUiHandler.postDelayed(() -> mLoadingDialog.startLoading(), 30);
            } else {
                mInstallCount--;
                if (mInstallCount <= 0) {
                    mInstallCount = 0;
                    // only dismiss when the app is the last to install.
                    mLoadingDialog.setTitle(getResources().getString(R.string.add_app_laoding_complete, model.getName()));
                    mLoadingDialog.stopLoading();
                    mUiHandler.postDelayed(() -> mLoadingDialog.dismiss(), 500);
                }
            }
        });
    }

    private void alertForMeizu() {
        if (!DeviceUtil.isMeizuBelowN()) {
            return;
        }
        boolean isXposedInstalled = VirtualCore.get().isAppInstalled(VApp.XPOSED_INSTALLER_PACKAGE);
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
            Drawable d = BitmapDrawable.createFromPath(wallpaper.getPath());
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
}
