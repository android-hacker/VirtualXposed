package io.virtualapp.sys;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.EncodeUtils;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.remote.InstalledAppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.AppInfoLite;

/**
 * author: weishu on 18/3/19.
 */
public class InstallerActivity extends AppCompatActivity {

    private TextView mTips;
    private Button mLeft;
    private Button mRight;
    private ProgressBar mProgressBar;
    private TextView mProgressText;

    private int mInstallCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_install);

        mTips = (TextView) findViewById(R.id.installer_text);
        mLeft = (Button) findViewById(R.id.installer_left_button);
        mRight = (Button) findViewById(R.id.installer_right_button);
        mProgressBar = (ProgressBar) findViewById(R.id.installer_loading);
        mProgressText = (TextView) findViewById(R.id.installer_progress_text);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        // do nothing.
        if (mInstallCount > 0) {

        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }

        ArrayList<AppInfoLite> dataList = intent.getParcelableArrayListExtra(VCommends.EXTRA_APP_INFO_LIST);
        if (dataList == null) {
            handleSystemIntent(intent);
        } else {
            handleSelfIntent(dataList);
        }
    }

    private void handleSelfIntent(ArrayList<AppInfoLite> appList) {
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

                addApp(info);
            }
            if (showTip) {
                Toast.makeText(this, R.string.large_app_install_tips, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void addApp(AppInfoLite appInfoLite) {
        Installd.addApp(appInfoLite, new Installd.UpdateListener() {
            @Override
            public void update(AppData model) {
                runOnUiThread(() -> {
                            if (model.isInstalling()) {
                                mProgressText.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.VISIBLE);
                                mProgressText.setText(getResources().getString(R.string.add_app_installing_tips, model.getName()));
                            } else if (model.isLoading()) {
                                mProgressText.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.VISIBLE);
                                mProgressText.setText(getResources().getString(R.string.add_app_loading_tips, model.getName()));
                            } else {
                                mInstallCount--;
                                if (mInstallCount <= 0) {
                                    mInstallCount = 0;
                                    // only dismiss when the app is the last to install.
                                    mProgressText.setText(getResources().getString(R.string.add_app_loading_complete, model.getName()));
                                    mProgressText.postDelayed(() -> {
                                        mProgressBar.setVisibility(View.GONE);

                                        mRight.setVisibility(View.VISIBLE);
                                        mRight.setText(R.string.install_complete);
                                        mRight.setOnClickListener((vv) -> finish());
                                    }, 500);
                                }
                            }
                        }
                );
            }

            @Override
            public void fail(String msg) {
                if (msg == null) {
                    msg = "Unknown";
                }

                mProgressText.setText(getResources().getString(R.string.install_fail, msg));
                mProgressText.postDelayed(() -> {
                    mProgressBar.setVisibility(View.GONE);
                    mRight.setVisibility(View.VISIBLE);
                    mRight.setText(R.string.install_complete);
                    mRight.setOnClickListener((vv) -> finish());
                }, 500);
            }
        });
    }

    private boolean dealUpdate(List<AppInfoLite> appList) {
        if (appList == null || appList.size() != 1) {
            return false;
        }
        AppInfoLite appInfoLite = appList.get(0);
        if (appInfoLite == null) {
            return false;
        }

        List<String> magicApps = Arrays.asList(EncodeUtils.decode("Y29tLmxiZS5wYXJhbGxlbA=="), // com.lbe.parallel
                EncodeUtils.decode("Y29tLnFpaG9vLm1hZ2lj"), // com.qihoo.magic
                EncodeUtils.decode("Y29tLmRvdWJsZW9wZW4=")); // com.doubleopen

        if (magicApps.contains(appInfoLite.packageName)) {
            Toast.makeText(VirtualCore.get().getContext(), R.string.install_self_eggs, Toast.LENGTH_SHORT).show();
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
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.multi_version_tip_title)
                    .setMessage(getResources().getString(R.string.multi_version_tips_content, currentVersion, toInstalledVersion))
                    .setPositiveButton(R.string.multi_version_multi, (dialog, which) -> {
                        addApp(appInfoLite);
                    })
                    .setNegativeButton(multiVersionUpdate, ((dialog, which) -> {
                        appInfoLite.disableMultiVersion = true;
                        addApp(appInfoLite);
                    }))
                    .create();
            alertDialog.show();
        } catch (Throwable ignored) {
            return false;
        }
        return true;
    }

    private void handleSystemIntent(Intent intent) {

        Context context = VirtualCore.get().getContext();
        String path;
        try {
            path = FileUtils.getFileFromUri(context, intent.getData());
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = context.getPackageManager().getPackageArchiveInfo(path, PackageManager.GET_META_DATA);
            pkgInfo.applicationInfo.sourceDir = path;
            pkgInfo.applicationInfo.publicSourceDir = path;
        } catch (Exception e) {
            // Ignore
        }
        if (pkgInfo == null) {
            finish();
            return;
        }

        boolean isXposed = pkgInfo.applicationInfo.metaData != null
                && pkgInfo.applicationInfo.metaData.containsKey("xposedmodule");

        InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(pkgInfo.packageName, 0);

        String tipsText;
        String rightString;
        String leftString = getResources().getString(android.R.string.cancel);

        PackageManager packageManager = getPackageManager();
        if (packageManager == null) {
            finish();
            return;
        }
        PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(path, 0);
        if (packageArchiveInfo == null) {
            finish();
            return;
        }
        String toInstalledVersion = packageArchiveInfo.versionName;
        int toInstalledVersionCode = packageArchiveInfo.versionCode;
        CharSequence label = packageArchiveInfo.packageName;

        if (installedAppInfo != null) {
            String currentVersion;
            int currentVersionCode;

            PackageInfo applicationInfo = installedAppInfo.getPackageInfo(0);
            if (applicationInfo == null) {
                finish();
                return;
            }
            currentVersion = applicationInfo.versionName;
            currentVersionCode = applicationInfo.versionCode;

            label = applicationInfo.applicationInfo.loadLabel(packageManager);

            String multiVersionUpdate = getResources().getString(currentVersionCode == toInstalledVersionCode ? R.string.multi_version_cover : (
                    currentVersionCode < toInstalledVersionCode ? R.string.multi_version_upgrade : R.string.multi_version_downgrade
            ));

            tipsText = getResources().getString(R.string.install_package_version_tips, currentVersion, toInstalledVersion);
            rightString = multiVersionUpdate;

        } else {
            tipsText = getResources().getString(R.string.install_package, label);
            rightString = getResources().getString(R.string.install);
        }

        final CharSequence apkName = label;
        mTips.setText(tipsText);
        mLeft.setText(leftString);
        mRight.setText(rightString);

        mLeft.setOnClickListener(v -> finish());
        mRight.setOnClickListener(v -> {

            mProgressBar.setVisibility(View.VISIBLE);
            mTips.setVisibility(View.GONE);
            mLeft.setVisibility(View.GONE);
            mRight.setEnabled(false);

            VUiKit.defer().when(() -> {
                return VirtualCore.get().installPackage(path, InstallStrategy.UPDATE_IF_EXIST);
            }).done((res) -> {
                // install success
                mTips.setVisibility(View.GONE);
                mProgressText.setVisibility(View.VISIBLE);
                mProgressText.setText(getResources().getString(R.string.add_app_loading_complete, apkName));
                mProgressBar.setVisibility(View.GONE);
                mRight.setEnabled(true);
                mRight.setText(res.isSuccess ? getResources().getString(R.string.install_complete) :
                        getResources().getString(R.string.install_fail, res.error));
                mRight.setOnClickListener((vv) -> finish());
            }).fail((res) -> {
                String msg = res.getMessage();
                if (msg == null) {
                    msg = "Unknown";
                }
                mProgressText.setVisibility(View.VISIBLE);
                mProgressText.setText(getResources().getString(R.string.install_fail, msg));
                mRight.setEnabled(true);
                mProgressBar.setVisibility(View.GONE);
                mRight.setText(android.R.string.ok);
                mRight.setOnClickListener((vv) -> finish());
            });
        });
    }
}
