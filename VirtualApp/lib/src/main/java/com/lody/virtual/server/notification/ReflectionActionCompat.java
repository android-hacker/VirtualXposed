package com.lody.virtual.server.notification;

import android.widget.RemoteViews;


/* package */ class ReflectionActionCompat {
    private static Class ReflectionActionClass;
    private static final String ReflectionAction = "ReflectionAction";

    static final int TAG = 2;

    static final int BOOLEAN = 1;
    static final int BYTE = 2;
    static final int SHORT = 3;
    static final int INT = 4;
    static final int LONG = 5;
    static final int FLOAT = 6;
    static final int DOUBLE = 7;
    static final int CHAR = 8;
    static final int STRING = 9;
    static final int CHAR_SEQUENCE = 10;
    static final int URI = 11;
    // BITMAP actions are never stored in the list of actions. They are only used locally
    // to implement BitmapReflectionAction, which eliminates duplicates using BitmapCache.
    static final int BITMAP = 12;
    static final int BUNDLE = 13;
    static final int INTENT = 14;
    static final int COLOR_STATE_LIST = 15;
    static final int ICON = 16;

    static {
        try {
            ReflectionActionClass = Class.forName(RemoteViews.class.getName() + "$" + ReflectionAction);
        } catch (ClassNotFoundException e) {
        }
    }

    static boolean isInstance(Object object) {
        return ReflectionActionClass != null && ReflectionActionClass.isInstance(object);
    }
}
