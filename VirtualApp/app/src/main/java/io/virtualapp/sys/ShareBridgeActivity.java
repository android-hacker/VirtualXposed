package io.virtualapp.sys;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VPackageManager;

import java.util.List;

import io.virtualapp.R;

/**
 * author: weishu on 18/3/16.
 */
public class ShareBridgeActivity extends AppCompatActivity {
    private SharedAdapter mAdapter;
    private List<ResolveInfo> mShareComponents;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        intent.setComponent(null);

        if (!Intent.ACTION_SEND.equals(action)) {
            finish();
            return;
        }

        try {
            mShareComponents = VPackageManager.get().
                    queryIntentActivities(new Intent(Intent.ACTION_SEND), type, 0, 0); // multi-user?
        } catch (Throwable ignored) {
        }

        if (mShareComponents == null || mShareComponents.size() == 0) {
            finish();
            return;
        }

        setContentView(R.layout.activity_list);
        ListView mListView = (ListView) findViewById(R.id.list);
        mAdapter = new SharedAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                ResolveInfo item = mAdapter.getItem(position);
                Intent t = new Intent(intent);
                t.setComponent(new ComponentName(item.activityInfo.packageName, item.activityInfo.name));
                VActivityManager.get().startActivity(t, 0);
            } catch (Throwable e) {
                Toast.makeText(getApplicationContext(), R.string.shared_to_vxp_failed, Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private class SharedAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mShareComponents.size();
        }

        @Override
        public ResolveInfo getItem(int position) {
            return mShareComponents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(getActivity(), parent);
                convertView = holder.root;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ResolveInfo item = getItem(position);
            PackageManager packageManager = getPackageManager();
            try {
                holder.label.setText(item.loadLabel(packageManager));
            } catch (Throwable e) {
                holder.label.setText(R.string.package_state_unknown);
            }
            try {
                holder.icon.setImageDrawable(item.loadIcon(packageManager));
            } catch (Throwable e) {
                holder.icon.setImageDrawable(getResources().getDrawable(android.R.drawable.sym_def_app_icon));
            }

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView label;

        View root;

        ViewHolder(Context context, ViewGroup parent) {
            root = LayoutInflater.from(context).inflate(R.layout.item_share, parent, false);
            icon = root.findViewById(R.id.item_share_icon);
            label = root.findViewById(R.id.item_share_name);
        }
    }

    private Activity getActivity() {
        return this;
    }
}
