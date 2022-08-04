package io.virtualapp.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VirtualStorageManager;
import com.lody.virtual.helper.ArtDexOptimizer;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.remote.InstalledAppInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.glide.GlideUtils;

/**
 * @author weishu
 * @date 18/2/15.
 */

public class AppManageActivity extends VActivity {

    private ListView mListView;
    private List<AppManageInfo> mInstalledApps = new ArrayList<>();
    private AppManageAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new AppManageAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            AppManageInfo appManageInfo = mInstalledApps.get(position);
            showContextMenu(appManageInfo, view);
        });
        loadAsync();
    }

    private void loadAsync() {
        VUiKit.defer().when(this::loadApp).done((v) -> mAdapter.notifyDataSetChanged());
    }

    private void loadApp() {

        List<AppManageInfo> ret = new ArrayList<>();
        List<InstalledAppInfo> installedApps = VirtualCore.get().getInstalledApps(0);
        PackageManager packageManager = getPackageManager();
        for (InstalledAppInfo installedApp : installedApps) {
            int[] installedUsers = installedApp.getInstalledUsers();
            for (int installedUser : installedUsers) {
                AppManageInfo info = new AppManageInfo();
                info.userId = installedUser;
                ApplicationInfo applicationInfo = installedApp.getApplicationInfo(installedUser);
                info.name = applicationInfo.loadLabel(packageManager);
//                info.icon = applicationInfo.loadIcon(packageManager);  //Use Glide to load icon async
                info.pkgName = installedApp.packageName;
                info.path = applicationInfo.sourceDir;
                ret.add(info);
            }
        }
        mInstalledApps.clear();
        mInstalledApps.addAll(ret);
    }

    class AppManageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mInstalledApps.size();
        }

        @Override
        public AppManageInfo getItem(int position) {
            return mInstalledApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(AppManageActivity.this, parent);
                convertView = holder.root;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppManageInfo item = getItem(position);

            holder.label.setText(item.getName());

            if (VirtualCore.get().isOutsideInstalled(item.pkgName)) {
                GlideUtils.loadInstalledPackageIcon(getContext(), item.pkgName, holder.icon, android.R.drawable.sym_def_app_icon);
            } else {
                GlideUtils.loadPackageIconFromApkFile(getContext(), item.path, holder.icon, android.R.drawable.sym_def_app_icon);
            }

            holder.button.setOnClickListener(v -> showContextMenu(item, v));

            return convertView;
        }
    }

    private void showContextMenu(AppManageInfo appManageInfo, View anchor) {
        if (appManageInfo == null) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.inflate(R.menu.app_manage_menu);
        MenuItem redirectMenu = popupMenu.getMenu().findItem(R.id.action_redirect);

        try {
            final String packageName = appManageInfo.pkgName;
            final int userId = appManageInfo.userId;
            boolean virtualStorageEnable = VirtualStorageManager.get().isVirtualStorageEnable(packageName, userId);
            redirectMenu.setTitle(virtualStorageEnable ? R.string.app_manage_redirect_off : R.string.app_manage_redirect_on);
        } catch (Throwable e) {
            redirectMenu.setVisible(false);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_uninstall:
                    showUninstallDialog(appManageInfo, appManageInfo.getName());
                    break;
                case R.id.action_repair:
                    showRepairDialog(appManageInfo);
                    break;
                case R.id.action_redirect:
                    showStorageRedirectDialog(appManageInfo);
                    break;
            }
            return false;
        });
        try {
            popupMenu.show();
        } catch (Throwable ignored) {
        }
    }

    private void showRepairDialog(AppManageInfo item) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getResources().getString(R.string.app_manage_repairing));
        try {
            dialog.setCancelable(false);
            dialog.show();
        } catch (Throwable e) {
            return;
        }

        VUiKit.defer().when(() -> {
            NougatPolicy.fullCompile(getApplicationContext());

            String packageName = item.pkgName;
            String apkPath = item.path;

            if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(apkPath)) {
                return;
            }

            // 1. kill package
            VirtualCore.get().killApp(packageName, item.userId);

            // 2. backup the odex file
            File odexFile = VEnvironment.getOdexFile(packageName);
            if (odexFile.delete()) {
                try {
                    ArtDexOptimizer.compileDex2Oat(apkPath, odexFile.getPath());
                } catch (IOException e) {
                    throw new RuntimeException("compile failed.");
                }
            }
        }).done((v) -> {
            dismiss(dialog);
            showAppDetailDialog();
        }).fail((v) -> {
            dismiss(dialog);
            Toast.makeText(this, R.string.app_manage_repair_failed_tips, Toast.LENGTH_SHORT).show();
        });
    }

    private void showAppDetailDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(AppManageActivity.this)
                .setTitle(R.string.app_manage_repair_success_title)
                .setMessage(getResources().getString(R.string.app_manage_repair_success_content))
                .setPositiveButton(R.string.app_manage_repair_reboot_now, (dialog, which) -> {
                    String packageName = getPackageName();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", packageName, null));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .create();

        alertDialog.setCancelable(false);

        try {
            alertDialog.show();
        } catch (Throwable ignored) {
        }
    }

    private void showUninstallDialog(AppManageInfo item, CharSequence name) {
        AlertDialog alertDialog = new AlertDialog.Builder(AppManageActivity.this)
                .setTitle(com.android.launcher3.R.string.home_menu_delete_title)
                .setMessage(getResources().getString(com.android.launcher3.R.string.home_menu_delete_content, name))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    VirtualCore.get().uninstallPackageAsUser(item.pkgName, item.userId);
                    loadAsync();
                })
                .setNegativeButton(android.R.string.no, null)
                .create();
        try {
            alertDialog.show();
        } catch (Throwable ignored) {
        }
    }

    private void showStorageRedirectDialog(AppManageInfo item) {
        final String packageName = item.pkgName;
        final int userId = item.userId;
        boolean virtualStorageEnable;
        try {
            virtualStorageEnable = VirtualStorageManager.get().isVirtualStorageEnable(packageName, userId);
        } catch (Throwable e) {
            return;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(AppManageActivity.this)
                .setTitle(virtualStorageEnable ? R.string.app_manage_redirect_off : R.string.app_manage_redirect_on)
                .setMessage(getResources().getString(R.string.app_manage_redirect_desc))
                .setPositiveButton(virtualStorageEnable ? R.string.app_manage_redirect_off_confirm : R.string.app_manage_redirect_on_confirm,
                        (dialog, which) -> {
                            try {
                                VirtualStorageManager.get().setVirtualStorageState(packageName, userId, !virtualStorageEnable);
                            } catch (Throwable ignored) {
                            }
                        })
                .setNegativeButton(android.R.string.no, null)
                .create();
        try {
            alertDialog.show();
        } catch (Throwable ignored) {
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView label;
        ImageView button;

        View root;

        ViewHolder(Context context, ViewGroup parent) {
            root = LayoutInflater.from(context).inflate(R.layout.item_app_manage, parent, false);
            icon = root.findViewById(R.id.item_app_icon);
            label = root.findViewById(R.id.item_app_name);
            button = root.findViewById(R.id.item_app_button);
        }
    }

    static class AppManageInfo {
        CharSequence name;
        int userId;
        Drawable icon;
        String pkgName;
        String path;

        CharSequence getName() {
            if (userId == 0) {
                return name;
            } else {
                return name + "[" + (userId + 1) + "]";
            }
        }
    }

    private static void dismiss(Dialog dialog) {
        if (dialog == null) {
            return;
        }
        try {
            dialog.dismiss();
        } catch (Throwable ignored) {
        }
    }
}
