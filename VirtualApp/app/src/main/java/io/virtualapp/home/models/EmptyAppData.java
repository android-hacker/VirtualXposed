package io.virtualapp.home.models;

import android.graphics.drawable.Drawable;
import android.os.Parcel;

/**
 * @author Lody
 */

public class EmptyAppData implements AppData {

    public static final Creator<EmptyAppData> CREATOR = new Creator<EmptyAppData>() {
        @Override
        public EmptyAppData createFromParcel(Parcel source) {
            return new EmptyAppData();
        }

        @Override
        public EmptyAppData[] newArray(int size) {
            return new EmptyAppData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

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
