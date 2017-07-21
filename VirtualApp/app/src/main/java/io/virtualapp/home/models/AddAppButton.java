package io.virtualapp.home.models;

import android.content.Context;
import android.graphics.drawable.Drawable;

import io.virtualapp.R;

/**
 * @author Lody
 */

public class AddAppButton implements AppData {

    private String name;
    private Drawable icon;

    public AddAppButton(Context context) {
        name = context.getResources().getString(R.string.add_app);
        icon = context.getResources().getDrawable(R.drawable.ic_add_circle);
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public boolean isFirstOpen() {
        return false;
    }

    @Override
    public Drawable getIcon() {
        return icon;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canReorder() {
        return false;
    }

    @Override
    public boolean canLaunch() {
        return false;
    }

    @Override
    public boolean canDelete() {
        return false;
    }

    @Override
    public boolean canCreateShortcut() {
        return false;
    }
}
