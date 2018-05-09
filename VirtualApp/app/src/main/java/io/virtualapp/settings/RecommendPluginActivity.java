package io.virtualapp.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VActivity;

/**
 * author: weishu on 2018/5/9.
 */

public class RecommendPluginActivity extends VActivity {

    private List<PluginInfo> mData = new ArrayList<>();
    private PluginAdapter mAdapter;
    private ProgressDialog mLoadingDialog;

    private static final String ADDRESS = "http://vaexposed.weishu.me/plugin.json";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setTitle("Loading");

        ListView mListView = findViewById(R.id.list);
        mAdapter = new PluginAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(findViewById(R.id.empty_view));

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                PluginInfo item = mAdapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(item.link));
                startActivity(intent);
            } catch (Throwable ignored) {
                ignored.printStackTrace();
            }
        });

        loadRecommend();
    }

    private void loadRecommend() {
        try {
            mLoadingDialog.show();
        } catch (Throwable ignored) {
        }

        defer().when(() -> {
            JSONArray jsonArray = null;

            URL url = new URL(ADDRESS);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            if(connection.getResponseCode() == 200){
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                jsonArray = new JSONArray(sb.toString());
                br.close();
            }

            connection.disconnect();
            return jsonArray;
        }).done(jsonArray -> {
            mLoadingDialog.dismiss();

            if (jsonArray == null) {
                return;
            }
            mData.clear();

            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                PluginInfo info = new PluginInfo();
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    info.name = jsonObject.getString("name");
                    info.desc = jsonObject.getString("desc");
                    info.link = jsonObject.getString("link");
                } catch (JSONException e) {
                    continue;
                }
                mData.add(info);
            }

            mAdapter.notifyDataSetChanged();
        }).fail((v) -> {
            mLoadingDialog.dismiss();
        });
    }

    static class PluginInfo {
        String name;
        String desc;
        String link;
    }

    static class ViewHolder {
        TextView title;
        TextView summary;

        View root;

        public ViewHolder(Context context, ViewGroup parent) {
            root = LayoutInflater.from(context).inflate(R.layout.item_plugin_recommend, parent, false);
            title = root.findViewById(R.id.item_plugin_name);
            summary = root.findViewById(R.id.item_plugin_summary);
        }
    }

    class PluginAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public PluginInfo getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(RecommendPluginActivity.this, parent);
                convertView = holder.root;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            PluginInfo info = getItem(position);
            holder.title.setText(info.name);
            holder.summary.setText(info.desc);

            return convertView;
        }
    }
}
