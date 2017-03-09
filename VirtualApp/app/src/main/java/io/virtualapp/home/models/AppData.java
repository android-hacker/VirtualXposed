package io.virtualapp.home.models;

import android.graphics.drawable.Drawable;
import android.os.Parcelable;

/**
 * @author Lody
 */

public interface AppData extends Parcelable {

    void mark(int tag);

    void unMark(int tag);

    boolean isMarked(int tag);

    boolean isLoading();

    boolean isFirstOpen();

    Drawable getIcon();

    String getName();

    boolean canReorder();

    boolean canLaunch();

    boolean canDelete();

    boolean canCreateShortcut();
}
