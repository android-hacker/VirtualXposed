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
import io.virtualapp.home.models.PackageAppData;
import io.virtualapp.widgets.LauncherIconView;
import io.virtualapp.widgets.ShadowProperty;
import io.virtualapp.widgets.ShadowViewDrawable;

import static io.virtualapp.widgets.ViewHelper.dip2px;

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

    public void add(PackageAppData model) {
        mList.add(model);
        notifyItemInserted(mList.size() - 1);
    }

    public void replace(int index, PackageAppData model) {
        mList.set(index, model);
        notifyItemChanged(index);
    }

    public void remove(PackageAppData model) {
        int index = mList.indexOf(model);
        if (index >= 0) {
            mList.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_launcher_app, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppData model = mList.get(position);
        holder.color = getColor(position);
        holder.iconView.setImageDrawable(model.getIcon());
        holder.nameView.setText(model.getName());
        if (model.isFirstOpen() && !model.isLoading()) {
            holder.firstOpenDot.setVisibility(View.VISIBLE);
        } else {
            holder.firstOpenDot.setVisibility(View.INVISIBLE);
        }
        ShadowProperty sp = new ShadowProperty()
                .setShadowColor(0x77000000)
                .setShadowDy(dip2px(0.5f))
                .setShadowRadius(dip2px(1))
                .setShadowSide(ShadowProperty.ALL);
        holder.shadow = new ShadowViewDrawable(sp, holder.color, 0, 0);
        holder.itemView.setBackgroundColor(holder.color);
        holder.itemView.setOnClickListener(v -> {
            if (mAppClickListener != null) {
                mAppClickListener.onAppClick(position, model);
            }
        });
        if (model.isLoading()) {
            startLoadingAnimation(holder.iconView);
        } else {
            holder.iconView.setProgress(100, false);
            if (model.isMarked(AppData.SHIMMER_NOT_SHOW)) {
                model.unMark(AppData.SHIMMER_NOT_SHOW);
                holder.iconView.startShimmer();
            }
        }
    }

    private void startLoadingAnimation(LauncherIconView iconView) {
        iconView.setProgress(50, true);
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

    public void refresh(PackageAppData model) {
        int index = mList.indexOf(model);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    public interface OnAppClickListener {
        void onAppClick(int position, AppData model);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ShadowViewDrawable shadow;
        public int color;
        LauncherIconView iconView;
        TextView nameView;
        View firstOpenDot;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = (LauncherIconView) itemView.findViewById(R.id.item_app_icon);
            nameView = (TextView) itemView.findViewById(R.id.item_app_name);
            firstOpenDot = itemView.findViewById(R.id.item_first_open_dot);
        }
    }
}
