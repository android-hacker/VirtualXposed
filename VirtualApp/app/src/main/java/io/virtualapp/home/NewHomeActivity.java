package io.virtualapp.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import com.google.android.apps.nexuslauncher.NexusLauncherActivity;
import com.lody.virtual.client.core.VirtualCore;

import java.io.File;
import java.util.List;

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

    //region ---------------package observer---------------
    private VirtualCore.PackageObserver mPackageObserver = new VirtualCore.PackageObserver() {
        @Override
        public void onPackageInstalled(String packageName) throws RemoteException {
            if (!isForground) {
                runOnUiThread(() -> mPresenter.dataChanged());
            }
        }

        @Override
        public void onPackageUninstalled(String packageName) throws RemoteException {
            if (!isForground) {
                runOnUiThread(() -> mPresenter.dataChanged());
            }
        }

        @Override
        public void onPackageInstalledAsUser(int userId, String packageName) throws RemoteException {
        }

        @Override
        public void onPackageUninstalledAsUser(int userId, String packageName) throws RemoteException {
        }
    };
    private boolean isForground = false;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getHotseat().setAddAppClickListener(v -> onAddAppClicked());
        getHotseat().setSettingClickListener(v -> onSettingsClicked());

        new HomePresenterImpl(this).start();
        VirtualCore.get().registerObserver(mPackageObserver);
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

    }

    @Override
    public void removeAppToLauncher(AppData model) {

    }

    @Override
    public void refreshLauncherItem(AppData model) {

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
                for (AppInfoLite info : appList) {
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
}
