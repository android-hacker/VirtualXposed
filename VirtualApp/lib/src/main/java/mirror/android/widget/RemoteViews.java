package mirror.android.widget;

import android.content.pm.ApplicationInfo;

import java.util.ArrayList;

import mirror.RefClass;
import mirror.RefObject;

/**
 * @author Lody
 */

public class RemoteViews {
    public static Class<?> TYPE = RefClass.load(RemoteViews.class, android.widget.RemoteViews.class);
    public static RefObject<ApplicationInfo> mApplication;
    public static RefObject<ArrayList<Object>> mActions;
    public static RefObject<String> mPackage;
}
