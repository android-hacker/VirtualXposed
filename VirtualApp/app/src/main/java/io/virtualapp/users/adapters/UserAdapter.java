package io.virtualapp.users.adapters;

import android.content.pm.UserInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import java.util.List;

import io.virtualapp.R;

/**
 * @author Lody
 */

public class UserAdapter extends SwipeMenuAdapter<UserAdapter.ViewHolder> {

    private final List<UserInfo> userList;

    public UserAdapter(List<UserInfo> userList) {
        this.userList = userList;
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
    }

    @Override
    public ViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        return new ViewHolder(realContentView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserInfo userInfo = userList.get(position);
        holder.tvName.setText(userInfo.name);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_title);
        }
    }
}
