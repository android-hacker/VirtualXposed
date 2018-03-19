package io.virtualapp.sys;

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

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.remote.InstalledAppInfo;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;

/**
 * author: weishu on 18/3/19.
 */
public class InstallerActivity extends AppCompatActivity {

    private TextView mTips;
    private Button mLeft;
    private Button mRight;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_install);

        mTips = (TextView) findViewById(R.id.installer_text);
        mLeft = (Button) findViewById(R.id.installer_left_button);
        mRight = (Button) findViewById(R.id.installer_right_button);
        mProgressBar = (ProgressBar) findViewById(R.id.installer_loading);

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
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }

        Context context = VirtualCore.get().getContext();
        String path = FileUtils.getFileFromUri(context, intent.getData());
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
        String toInstalledVersion = packageArchiveInfo.versionName;
        int toInstalledVersionCode = packageArchiveInfo.versionCode;
        CharSequence label;
        try {
            label = packageArchiveInfo.applicationInfo.loadLabel(packageManager);
        } catch (Throwable e) {
            label = packageArchiveInfo.packageName;
        }

        if (installedAppInfo != null) {
            String currentVersion;
            int currentVersionCode;

            PackageInfo applicationInfo = installedAppInfo.getPackageInfo(0);
            currentVersion = applicationInfo.versionName;
            currentVersionCode = applicationInfo.versionCode;

            String multiVersionUpdate = getResources().getString(currentVersionCode == toInstalledVersionCode ? R.string.multi_version_cover : (
                    currentVersionCode < toInstalledVersionCode ? R.string.multi_version_upgrade : R.string.multi_version_downgrade
            ));

            tipsText = getResources().getString(R.string.install_package_version_tips, currentVersion, toInstalledVersion);
            rightString = multiVersionUpdate;

        } else {
            tipsText = getResources().getString(R.string.install_package, label);
            rightString = getResources().getString(R.string.install);
        }

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
                mProgressBar.setVisibility(View.GONE);
                mRight.setEnabled(true);
                mRight.setText(res.isSuccess ? R.string.install_complete : R.string.install_fail);
                mRight.setOnClickListener((vv) -> finish());
            }).fail((res) -> {
                mTips.setVisibility(View.VISIBLE);
                mTips.setText(R.string.install_fail);
                mRight.setEnabled(true);
                mProgressBar.setVisibility(View.GONE);
                mRight.setText(android.R.string.ok);
                mRight.setOnClickListener((vv) -> finish());
            });
        });
    }
}
