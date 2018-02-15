package io.virtualapp.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstalledAppInfo;

import java.util.ArrayList;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VActivity;

/**
 * @author weishu
 * @date 18/2/15.
 */

public class TaskManageActivity extends VActivity {

    private ListView mListView;
    private List<AppManageInfo> mInstalledApps = new ArrayList<>();
    private AppManageAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_app_manage_text);
        setContentView(R.layout.activity_app_list);
        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new AppManageAdapter();
        mListView.setAdapter(mAdapter);

        loadAsync();
    }

    private void loadAsync() {
        mInstalledApps.clear();
        defer().when(this::loadApp).done((v) -> mAdapter.notifyDataSetChanged());
    }

    private void loadApp() {

        List<InstalledAppInfo> installedApps = VirtualCore.get().getInstalledApps(0);
        PackageManager packageManager = getPackageManager();
        for (InstalledAppInfo installedApp : installedApps) {
            int[] installedUsers = installedApp.getInstalledUsers();
            for (int installedUser : installedUsers) {
                AppManageInfo info = new AppManageInfo();
                info.userId = installedUser;
                ApplicationInfo applicationInfo = installedApp.getApplicationInfo(installedUser);
                info.name = applicationInfo.loadLabel(packageManager);
                info.icon = applicationInfo.loadIcon(packageManager);
                info.pkgName = installedApp.packageName;
                mInstalledApps.add(info);
            }
        }
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
                holder = new ViewHolder(TaskManageActivity.this, parent);
                convertView = holder.root;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppManageInfo item = getItem(position);

            holder.button.setText(R.string.app_manage_uninstall);
            CharSequence name;
            if (item.userId == 0) {
                name = item.name;
            } else {
                name = item.name + "[" + (item.userId + 1) + "]";
            }

            holder.label.setText(name);

            if (item.icon == null) {
                holder.icon.setVisibility(View.GONE);
            } else {
                holder.icon.setImageDrawable(item.icon);
            }

            holder.button.setOnClickListener(v -> {
                AlertDialog alertDialog = new AlertDialog.Builder(TaskManageActivity.this)
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
            });

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView label;
        Button button;

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
    }
}
