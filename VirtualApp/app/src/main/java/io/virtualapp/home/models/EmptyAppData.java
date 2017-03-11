package io.virtualapp.home.models;

import android.graphics.drawable.Drawable;

/**
 * @author Lody
 */

public class EmptyAppData implements AppData {

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
        return null;
    }

    @Override
    public String getName() {
        return null;
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
