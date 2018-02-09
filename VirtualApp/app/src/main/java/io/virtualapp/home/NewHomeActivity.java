package io.virtualapp.home;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.apps.nexuslauncher.NexusLauncherActivity;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.about.AboutActivity;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.AppInfoLite;

/**
 * @author weishu
 * @date 18/2/9.
 */

public class NewHomeActivity extends NexusLauncherActivity implements HomeContract.HomeView {

    private HomeContract.HomePresenter mPresenter;
    private Handler mUiHandler;
    private int mInstallCount = 0;
    private long mInstallStartTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUiHandler = new Handler(getMainLooper());
        getHotseat().setAddAppClickListener(v -> onAddAppClicked());
        getHotseat().setSettingClickListener(v -> onSettingsClicked());

        new HomePresenterImpl(this).start();
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
    public void showBottomAction() {
        // no-op
    }

    @Override
    public void hideBottomAction() {
        // no-op
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void loadFinish(List<AppData> appModels) {

    }

    @Override
    public void loadError(Throwable err) {

    }

    @Override
    public void showGuide() {
        // no-op
    }

    @Override
    public void addAppToLauncher(AppData model) {
        refreshLoadingDialog(model);
    }

    @Override
    public void removeAppToLauncher(AppData model) {

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
        startActivity(new Intent(NewHomeActivity.this, AboutActivity.class));
    }

    @Override
    public void onClickSettingsButton(View v) {
        // super.onClickSettingsButton(v);
        onSettingsClicked();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            List<AppInfoLite> appList = data.getParcelableArrayListExtra(VCommends.EXTRA_APP_INFO_LIST);
            if (appList != null) {
                boolean showTip = false;
                int size = appList.size();
                mInstallCount = size;
                mInstallStartTime = SystemClock.elapsedRealtime();
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
            throw new RuntimeException("can not found package name for:" + intent);
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
                mLoadingDialog.setTitle(getResources().getString(R.string.add_app_loading_tips, model.getName()));
                mLoadingDialog.show();
                mUiHandler.postDelayed(() -> mLoadingDialog.startLoading(), 30);
            } else if (model.isLoading()) {
                mLoadingDialog.setTitle(getResources().getString(R.string.add_app_installing_tips, model.getName()));
                mLoadingDialog.show();
                mUiHandler.postDelayed(() -> mLoadingDialog.startLoading(), 30);
            } else {
                mInstallCount--;
                if (mInstallCount <= 0) {
                    mInstallCount = 0;
                    // only dismiss when the app is the last to install.
                    mLoadingDialog.setTitle(getResources().getString(R.string.add_app_laoding_complete, model.getName()));
                    mLoadingDialog.stopLoading();
                    mUiHandler.postDelayed(() -> mLoadingDialog.dismiss(), 300);
                }
            }
        });
    }
}
