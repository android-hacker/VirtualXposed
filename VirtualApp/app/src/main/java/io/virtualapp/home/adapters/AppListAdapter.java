package io.virtualapp.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.virtualapp.R;
import io.virtualapp.home.models.AppModel;

/**
 * @author Lody
 */
public class AppListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<AppModel> models;

    public AppListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setModels(List<AppModel> models) {
        this.models = models;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return models == null ? 0 : models.size();
    }

    @Override
    public Object getItem(int position) {
        return models.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppModel model = models.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_app, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.iconView.setImageDrawable(model.icon);
        viewHolder.nameView.setText(model.name);
        return convertView;
    }

    class ViewHolder {
        private ImageView iconView;
        private TextView nameView;

        public ViewHolder(View itemView) {
            iconView = (ImageView) itemView.findViewById(R.id.item_app_icon);
            nameView = (TextView) itemView.findViewById(R.id.item_app_name);
        }
    }
}
