package io.virtualapp.home.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.virtualapp.R;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.PackageAppData;

/**
 * @author Lody
 */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<AppData> mAppList;
    private OnItemClickListener mListener;

    public AppListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public OnItemClickListener getListener() {
        return mListener;
    }

    public void setListener(OnItemClickListener mListener) {
        this.mListener = mListener;
    }

    public List<AppData> getList() {
        return mAppList;
    }

    public void setList(List<AppData> models) {
        this.mAppList = models;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_app, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> mListener.onItemClick(position));
        PackageAppData model = (PackageAppData) mAppList.get(position);
        holder.iconView.setImageDrawable(model.icon);
        holder.nameView.setText(model.name);
    }

    @Override
    public int getItemCount() {
        return mAppList == null ? 0 : mAppList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconView;
        private TextView nameView;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(R.id.item_app_icon);
            nameView = (TextView) itemView.findViewById(R.id.item_app_name);
        }
    }
}
