package io.virtualapp.sys;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.List;

import io.virtualapp.R;

/**
 * author: weishu on 18/3/16.
 */
public class ShareBridgeActivity extends AppCompatActivity {
    private SharedAdapter mAdapter;
    private List<ListItem> mShareComponents = null;

    private List<ListItem> getCommonComponents(Intent intent) {
        Context context = getApplicationContext();
        List<ListItem> mCommonComponents = new ArrayList<>();
        Resources r = getResources();
        ListItem listItem;
        listItem = new ListItem(
                context.getPackageName(),
                InstallerActivity.class.getName(),
                r.getString(R.string.app_installer_label),
                r.getDrawable(R.mipmap.ic_launcher),
                ContextType.APP,
                Intent.FLAG_ACTIVITY_NEW_TASK,
                intent.getClipData().getItemAt(0).getUri());
        mCommonComponents.add(listItem);
        return mCommonComponents;
    }

    private enum ContextType {
        APP,
        VXP
    }

    private static class ListItem {
        private ContextType contextType;
        private PackageManager pm = null;
        private String label = null;
        private Drawable icon = null;

        private String packageName;
        private String activityName;
        private ResolveInfo resolveInfo;

        private Uri data;
        private Integer flag;

        public String getLabel() {
            if (label != null) return label;
            if (pm == null) throw new IllegalArgumentException();
            label = String.valueOf(resolveInfo.loadLabel(pm));
            return label;
        }

        public Drawable getIcon() {
            if (icon != null) return icon;
            if (pm == null) throw new IllegalArgumentException();
            icon = resolveInfo.loadIcon(pm);
            return icon;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getActivityName() {
            return activityName;
        }

        public Uri getData() {
            return data;
        }

        public Integer getFlag() {
            return flag;
        }

        public ContextType getContextType() {
            return contextType;
        }

        private ListItem(String packageName, String activityName, String label, Drawable icon, ContextType contextType) {
            this.packageName = packageName;
            this.activityName = activityName;
            this.label = label;
            this.icon = icon;
            this.contextType = contextType;
        }

        private ListItem(String packageName, String activityName, String label, Drawable icon, ContextType contextType, Integer flag, Uri data) {
            this(packageName, activityName, label, icon, contextType);
            this.flag = flag;
            this.data = data;
        }

        private ListItem(ResolveInfo resolveInfo, PackageManager pm) {
            this(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name, null, null, ContextType.VXP);
            this.resolveInfo = resolveInfo;
            this.pm = pm;
        }

        @Override
        public String toString() {
            return "ListItem{" +
                    "pm=" + pm +
                    ", label='" + label + '\'' +
                    ", icon=" + icon +
                    ", packageName='" + packageName + '\'' +
                    ", activityName='" + activityName + '\'' +
                    ", resolveInfo=" + resolveInfo +
                    ", data=" + data +
                    ", flag=" + flag +
                    ", contextType=" + contextType +
                    '}';
        }
    }

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
            List<ResolveInfo> resolveInfoList = VPackageManager.get().
                    queryIntentActivities(new Intent(Intent.ACTION_SEND), type, 0, 0); // multi-user?
            mShareComponents = getCommonComponents(intent);
            PackageManager pm = getPackageManager();
            for (ResolveInfo resolveInfo : resolveInfoList) {
                mShareComponents.add(new ListItem(resolveInfo, pm));
            }
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
                Intent t = new Intent(intent);
                ListItem item = mAdapter.getItem(position);
                t.setComponent(new ComponentName(item.getPackageName(), item.getActivityName()));
                if (item.getFlag() != null) {
                    t.addFlags(item.getFlag());
                }
                if (item.getData() != null) {
                    t.setData(item.getData());
                }
                if (item.getContextType() == ContextType.APP) {
                    getApplicationContext().startActivity(t);
                } else if (item.getContextType() == ContextType.VXP) {
                    VActivityManager.get().startActivity(t, 0);
                }
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
        public ListItem getItem(int position) {
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

            ListItem item = getItem(position);
            try {
                holder.label.setText(item.getLabel());
            } catch (Throwable e) {
                holder.label.setText(R.string.package_state_unknown);
            }
            try {
                holder.icon.setImageDrawable(item.getIcon());
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
