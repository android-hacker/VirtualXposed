package io.virtualapp.home.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.MultiplePackageAppData;
import io.virtualapp.widgets.LabelView;
import io.virtualapp.widgets.LauncherIconView;

/**
 * @author Lody
 */
public class LaunchpadAdapter extends RecyclerView.Adapter<LaunchpadAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<AppData> mList;
    private SparseIntArray mColorArray = new SparseIntArray();
    private OnAppClickListener mAppClickListener;

    public LaunchpadAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void add(AppData model) {
        int insertPos = mList.size() - 1;
        mList.add(insertPos, model);
        notifyItemInserted(insertPos);
    }

    public void replace(int index, AppData data) {
        mList.set(index, data);
        notifyItemChanged(index);
    }

    public void remove(AppData data) {
        if (mList.remove(data)) {
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_launcher_app, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppData data = mList.get(position);
        holder.color = getColor(position);
        holder.iconView.setImageDrawable(data.getIcon());
        holder.nameView.setText(data.getName());
        if (data.isFirstOpen() && !data.isLoading()) {
            holder.firstOpenDot.setVisibility(View.VISIBLE);
        } else {
            holder.firstOpenDot.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setBackgroundColor(holder.color);
        holder.itemView.setOnClickListener(v -> {
            if (mAppClickListener != null) {
                mAppClickListener.onAppClick(position, data);
            }
        });
        if (data instanceof MultiplePackageAppData) {
            MultiplePackageAppData multipleData = (MultiplePackageAppData) data;
            holder.spaceLabelView.setVisibility(View.VISIBLE);
            holder.spaceLabelView.setText(multipleData.userId + 1 + "");
        } else {
            holder.spaceLabelView.setVisibility(View.INVISIBLE);
        }
        if (data.isLoading()) {
            startLoadingAnimation(holder.iconView);
        } else {
            holder.iconView.setProgress(100, false);
        }
    }

    private void startLoadingAnimation(LauncherIconView iconView) {
        iconView.setProgress(40, true);
        VUiKit.defer().when(() -> {
            try {
                Thread.sleep(900L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).done((res) -> iconView.setProgress(80, true));
    }

    private int getColor(int position) {
        int color = mColorArray.get(position);
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
            mColorArray.put(position, color);
        }
        return color;
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public List<AppData> getList() {
        return mList;
    }

    public void setList(List<AppData> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setAppClickListener(OnAppClickListener mAppClickListener) {
        this.mAppClickListener = mAppClickListener;
    }

    public void moveItem(int pos, int targetPos) {
        AppData model = mList.remove(pos);
        mList.add(targetPos, model);
        notifyItemMoved(pos, targetPos);
    }

    public void refresh(AppData model) {
        int index = mList.indexOf(model);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    public interface OnAppClickListener {
        void onAppClick(int position, AppData model);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public int color;
        LauncherIconView iconView;
        TextView nameView;
        LabelView spaceLabelView;
        View firstOpenDot;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = (LauncherIconView) itemView.findViewById(R.id.item_app_icon);
            nameView = (TextView) itemView.findViewById(R.id.item_app_name);
            spaceLabelView = (LabelView) itemView.findViewById(R.id.item_app_space_idx);
            firstOpenDot = itemView.findViewById(R.id.item_first_open_dot);
        }
    }
}
