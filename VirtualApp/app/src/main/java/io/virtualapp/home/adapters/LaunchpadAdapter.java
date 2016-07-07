package io.virtualapp.home.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.home.models.AppModel;
import io.virtualapp.widgets.PagerAdapter;

/**
 * @author Lody
 */
public class LaunchpadAdapter extends PagerAdapter<AppModel> {

    public LaunchpadAdapter(Context context) {
        //noinspection unchecked
        super(context, new ArrayList<>(6));
    }

    public void setModels(List<AppModel> models) {
        this.mList = models;
    }


    @Override
    public int getItemLayoutId(int position, AppModel appModel) {
        return R.layout.item_launcher_app;
    }

    @Override
    public void onBindView(View view, AppModel appModel) {
        ImageView iconView = (ImageView) view.findViewById(R.id.item_app_icon);
        TextView nameView = (TextView) view.findViewById(R.id.item_app_name);
        iconView.setImageDrawable(appModel.icon);
        nameView.setText(appModel.name);
    }


}
