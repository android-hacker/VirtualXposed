package io.virtualapp.home.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.virtualapp.R;
import io.virtualapp.abs.ui.BaseAdapterPlus;
import io.virtualapp.home.models.LocationData;

public class AppLocationAdapter extends BaseAdapterPlus<LocationData> {
    public AppLocationAdapter(Context context) {
        super(context);
    }

    @Override
    protected View createView(int position, ViewGroup parent) {
        View view = inflate(R.layout.item_location_app, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    protected void attach(View view, LocationData item, int position) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.icon.setImageDrawable(item.icon);
        viewHolder.label.setText(item.name);
        if (item.location != null && item.mode != 0) {
            viewHolder.location.setText(item.location.latitude + "," + item.location.longitude);
        } else {
            viewHolder.location.setText("real location");
        }
    }

    static class ViewHolder extends BaseAdapterPlus.BaseViewHolder {
        public ViewHolder(View view) {
            super(view);
            icon = $(R.id.item_app_icon);
            label = $(R.id.item_app_name);
            location = $(R.id.item_location);
        }

        final ImageView icon;
        final TextView label, location;
    }
}
