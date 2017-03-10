package io.virtualapp.home.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.PackageAppData;
import io.virtualapp.widgets.DragSelectRecyclerViewAdapter;

/**
 * @author Lody
 */
public class CloneAppListAdapter extends DragSelectRecyclerViewAdapter<CloneAppListAdapter.ViewHolder> {

    private static final int TYPE_FOOTER = -2;
    private final View mFooterView;
    private LayoutInflater mInflater;
    private List<AppData> mAppList;
    private ItemEventListener mItemEventListener;

    public CloneAppListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        mFooterView = new View(context);
        StaggeredGridLayoutManager.LayoutParams params = new StaggeredGridLayoutManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, VUiKit.dpToPx(context, 60)
        );
        params.setFullSpan(true);
        mFooterView.setLayoutParams(params);

    }

    public void setOnItemClickListener(ItemEventListener mItemEventListener) {
        this.mItemEventListener = mItemEventListener;
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
        if (viewType == TYPE_FOOTER) {
            return new ViewHolder(mFooterView);
        }
        return new ViewHolder(mInflater.inflate(R.layout.item_clone_app, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOOTER) {
            return;
        }
        super.onBindViewHolder(holder, position);
        PackageAppData model = (PackageAppData) mAppList.get(position);
        holder.iconView.setImageDrawable(model.icon);
        holder.nameView.setText(model.name);
        if (isIndexSelected(position)) {
            holder.itemView.setAlpha(1f);
            holder.appCheckView.setImageResource(R.drawable.ic_check);
        } else {
            holder.itemView.setAlpha(0.65f);
            holder.appCheckView.setImageResource(R.drawable.ic_no_check);
        }
        holder.itemView.setOnClickListener(v -> {
            mItemEventListener.onItemClick(model, position);
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    protected boolean isIndexSelectable(int index) {
        return mItemEventListener.isSelectable(index);
    }

    @Override
    public int getItemCount() {
        return mAppList == null ? 1 : mAppList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return super.getItemViewType(position);
    }

    public AppData getItem(int index) {
        return mAppList.get(index);
    }

    public interface ItemEventListener {

        void onItemClick(AppData appData, int position);

        boolean isSelectable(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconView;
        private TextView nameView;
        private ImageView appCheckView;

        ViewHolder(View itemView) {
            super(itemView);
            if (itemView != mFooterView) {
                iconView = (ImageView) itemView.findViewById(R.id.item_app_icon);
                nameView = (TextView) itemView.findViewById(R.id.item_app_name);
                appCheckView = (ImageView) itemView.findViewById(R.id.item_app_checked);
            }
        }
    }
}
