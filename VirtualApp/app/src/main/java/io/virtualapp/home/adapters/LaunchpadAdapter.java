package io.virtualapp.home.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.virtualapp.R;
import io.virtualapp.home.models.AppModel;
import io.virtualapp.widgets.ShadowProperty;
import io.virtualapp.widgets.ShadowViewDrawable;

import static io.virtualapp.widgets.ViewHelper.dip2px;

/**
 * @author Lody
 */
public class LaunchpadAdapter extends RecyclerView.Adapter<LaunchpadAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<AppModel> mList;
    private SparseIntArray colorArray = new SparseIntArray();

    public LaunchpadAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void add(AppModel model) {
        mList.add(model);
        notifyItemInserted(mList.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_launcher_app, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppModel model = mList.get(position);
        holder.iconView.setImageDrawable(model.icon);
        holder.nameView.setText(model.name);
        holder.color = getColor(position);
        ShadowProperty sp = new ShadowProperty()
                .setShadowColor(0x77000000)
                .setShadowDy(dip2px(0.5f))
                .setShadowRadius(dip2px(1))
                .setShadowSide(ShadowProperty.ALL);
        holder.shadow = new ShadowViewDrawable(sp, holder.color, 0, 0);
        holder.itemView.setBackgroundColor(holder.color);
    }

    private int getColor(int position) {
        int color = colorArray.get(position);
        if (color == 0) {
            int type = position % 3;
            int row = position / 3;
            int rowType = row % 3;
            if (rowType == 0) {
                if (type == 0) {
                    color = mInflater.getContext().getResources().getColor(R.color.desktopColorA);
                } else if (type == 1) {
                    color = mInflater.getContext().getResources().getColor(R.color.desktopColorB);
                } else {
                    color = mInflater.getContext().getResources().getColor(R.color.desktopColorC);
                }
            } else if (rowType == 1) {
                if (type == 0) {
                    color = mInflater.getContext().getResources().getColor(R.color.desktopColorB);
                } else if (type == 1) {
                    color = mInflater.getContext().getResources().getColor(R.color.desktopColorC);
                } else {
                    color = mInflater.getContext().getResources().getColor(R.color.desktopColorA);
                }
            } else {
                if (type == 0) {
                    color = mInflater.getContext().getResources().getColor(R.color.desktopColorC);
                } else if (type == 1) {
                    color = mInflater.getContext().getResources().getColor(R.color.desktopColorA);
                } else {
                    color = mInflater.getContext().getResources().getColor(R.color.desktopColorB);
                }
            }
            colorArray.put(position, color);
        }
        return color;
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public List<AppModel> getList() {
        return mList;
    }

    public void setList(List<AppModel> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void moveItem(int pos, int targetPos) {
        AppModel model = mList.remove(pos);
        mList.add(targetPos, model);
        notifyItemMoved(pos, targetPos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ShadowViewDrawable shadow;
        public int color;
        ImageView iconView;
        TextView nameView;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(R.id.item_app_icon);
            nameView = (TextView) itemView.findViewById(R.id.item_app_name);
        }
    }
}
