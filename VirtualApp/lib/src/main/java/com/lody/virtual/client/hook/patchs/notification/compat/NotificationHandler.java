package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.lody.virtual.R;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.XLog;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by 247321453 on 2016/7/12.
 */
public class NotificationHandler {
    //    private static final boolean DRAW_NOTIFICATION = true;
    private int notification_min_height, notification_max_height, notification_mid_height;
    private int notification_panel_width;
    private int notification_side_padding;
    private int notification_padding;
    private final NotificationLayoutCompat mNotificationLayoutCompat;
    private final NotificationActionCompat mNotificationActionCompat;
    private static final String TAG = NotificationHandler.class.getSimpleName();
    /** 双开不处理 */
    public static boolean DOPEN_NOT_DEAL = false;
    /** 系统样式的通知栏不处理 */
    public static boolean SYSTEM_NOTIFICATION = false;

    private void init(Context context) {
        mNotificationActionCompat.init(context);
        if (notification_panel_width == 0) {
            Context systemUi = null;
            try {
                systemUi = context.createPackageContext("com.android.systemui", Context.CONTEXT_IGNORE_SECURITY);
            } catch (PackageManager.NameNotFoundException e) {
            }
            //notification_row_min_height
            //notification_row_max_height
            if (Build.VERSION.SDK_INT <= 19) {
                notification_side_padding = 0;
            } else {
                notification_side_padding = getDimem(context, systemUi, "notification_side_padding", R.dimen.notification_side_padding);
            }
            notification_panel_width = getDimem(context, systemUi, "notification_panel_width", R.dimen.notification_panel_width);
            if (notification_panel_width <= 0) {
                notification_panel_width = context.getResources().getDisplayMetrics().widthPixels;
            }
            notification_min_height = 0;// getDimem(context,systemUi, "notification_row_min_height", 0);
            if (notification_min_height == 0) {
                notification_min_height = getDimem(context, systemUi, "notification_min_height", R.dimen.notification_min_height);
            }
            notification_max_height = getDimem(context, systemUi, "notification_max_height", R.dimen.notification_max_height);
            notification_mid_height = getDimem(context, systemUi, "notification_mid_height", R.dimen.notification_mid_height);
            notification_padding = getDimem(context, systemUi, "notification_padding", R.dimen.notification_padding);
            //notification_collapse_second_card_padding
        }
    }

    static NotificationHandler sNotificationHandler;

    public static NotificationHandler getInstance() {
        if (sNotificationHandler == null) {
            synchronized (NotificationHandler.class) {
                if (sNotificationHandler == null) {
                    sNotificationHandler = new NotificationHandler();
                }
            }
        }
        return sNotificationHandler;
    }

    private NotificationHandler() {
        mNotificationLayoutCompat = new NotificationLayoutCompat();
        mNotificationActionCompat = new NotificationActionCompat();
    }

    private int getDimem(Context context, Context sysContext, String name, int defId) {
        if (sysContext != null) {
            int id = sysContext.getResources().getIdentifier(name, "dimen", "com.android.systemui");
            if (id != 0) {
                try {
                    int i = Math.round(sysContext.getResources().getDimension(id));
                } catch (Exception e) {

                }
            }
        }
//        XLog.w(TAG, "use my dimen:" + name);
        return defId == 0 ? 0 : Math.round(context.getResources().getDimension(defId));
    }

    public void dealNotificationIcon(int iconId, String packageName, Object... args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Notification) {
                Notification notification = (Notification) args[i];//nobug
                final Context pluginContext = VirtualCore.getCore().getContext().createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
                args[i] = new RemoteViewsCompat(pluginContext, mNotificationActionCompat, notification).getNotification();
                break;
            }
        }
    }

    /***
     * @param hostContext
     * @param packageName
     * @param args
     * @return -1 失败，>=0是成功。>0是系统样式（通知栏的icon），0是自定义样式
     * @throws Exception
     */
    public int dealNotification(Context hostContext, String packageName, Object... args) throws Exception {
        init(hostContext);
        int id = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Integer) {
                id = (Integer) args[i];
            }
            if (args[i] instanceof Notification) {
                Notification notification = (Notification) args[i];//nobug
                if (isPluginNotification(notification)) {
//                    XLog.d("kk", "deal id=" + id + ", " + notification);
                    //双开模式，icon还是va的
                    if (DOPEN_NOT_DEAL) {
                        if (VirtualCore.getCore().isOutsideInstalled(packageName)) {
                            //替换状态栏icon
                            notification.icon = hostContext.getApplicationInfo().icon;
                            //绘制icon
                            mNotificationActionCompat.builderNotificationIcon(notification, notification.icon, VirtualCore.getCore().getResources(packageName));
                            return 0;
                        }
                    }
                    if (mNotificationActionCompat.shouldBlock(notification)) {
//                        //自定义布局通知栏
                        Notification notification1 = replaceNotification(hostContext, packageName, notification, false);
                        if (notification1 == null) {
                            return -1;
                        }
                        args[i] = notification1;
                        return 0;
                    } else {
                        //这里要修改原生的通知，是否也和上面一样的处理？
                        final int icon = notification.icon;
                        if (SYSTEM_NOTIFICATION) {
                            //处理图片,icon,application
                            mNotificationActionCompat.hackNotification(notification);
                        } else {
                            Notification notification1 = replaceNotification(hostContext, packageName, notification, true);
                            if (notification1 != null) {
                                args[i] = notification1;
                            } else {
                                mNotificationActionCompat.hackNotification(notification);
                            }
                        }
                        return icon;
                    }
                } else {
                    XLog.w("kk", "mod notification");
                }
            }
        }
        return -1;
    }

    private boolean isPluginNotification(Notification notification) {
        if (notification == null) {
            return false;
        }


        if (notification.contentView != null && !isHostPackageName(notification.contentView.getPackage())) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (notification.tickerView != null && !isHostPackageName(notification.tickerView.getPackage())) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (notification.bigContentView != null
                    && !isHostPackageName(notification.bigContentView.getPackage())) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (notification.headsUpContentView != null
                    && !isHostPackageName(notification.headsUpContentView.getPackage())) {
                return true;
            }
            if (notification.publicVersion != null
                    && notification.publicVersion.contentView != null
                    && !isHostPackageName(notification.publicVersion.contentView.getPackage())) {
                return true;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.graphics.drawable.Icon icon = notification.getSmallIcon();
            if (icon != null) {
                try {
                    Object mString1Obj = Reflect.on(icon).get("mString1");
                    if (mString1Obj instanceof String) {
                        String mString1 = ((String) mString1Obj);
                        if (!isHostPackageName(mString1)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.graphics.drawable.Icon icon = notification.getLargeIcon();
            if (icon != null) {
                try {
                    Object mString1Obj = Reflect.on(icon).get("mString1");
                    if (mString1Obj instanceof String) {
                        String mString1 = ((String) mString1Obj);
                        if (!isHostPackageName(mString1)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        try {
            Bundle mExtras = Reflect.on(notification).get("extras");
            for (String s : mExtras.keySet()) {
                if (mExtras.get(s) != null && mExtras.get(s) instanceof ApplicationInfo) {
                    ApplicationInfo applicationInfo = (ApplicationInfo) mExtras.get(s);
                    if (applicationInfo != null) {
                        return !isHostPackageName(applicationInfo.packageName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isHostPackageName(String pkg) {
        return VirtualCore.getCore().isHostPackageName(pkg);
    }

    /***
     * @param context      cxt
     * @param packageName  通知栏包名
     * @param notification 通知栏
     * @param systemId     系统样式？
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    private Notification replaceNotification(Context context, String packageName, Notification notification, boolean systemId) throws PackageManager.NameNotFoundException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final ContextWrapperCompat pluginContext = new ContextWrapperCompat(context, packageName);
        //build
        RemoteViewsCompat remoteViewsCompat = new RemoteViewsCompat(pluginContext, mNotificationActionCompat, notification);
        RemoteViews contentView = remoteViewsCompat.getRemoteViews();
        //大通知栏
        boolean isBig = remoteViewsCompat.isBigRemoteViews();
        if (contentView == null) {
            return null;
        }
        //icon
        if (Build.VERSION.SDK_INT >= 23) {
            mNotificationActionCompat.setNotificationIconImageView(context, contentView, notification.icon);
        }
        //通过id设置icon的view？
        Map<Integer, PendingIntent> clickIntents = getClickIntents(contentView);
        //如果就一个点击事件，没必要用复杂view
        final int layoutId;
        if (systemId) {
            layoutId = R.layout.custom_notification_lite_datetime;
        } else if (clickIntents == null || clickIntents.size() == 0) {
            layoutId = R.layout.custom_notification_lite;
        } else {
            layoutId = R.layout.custom_notification;
        }
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
        if (systemId) {
            setDateTime(remoteViews, remoteViewsCompat, notification.when);
        }
        //绘制图
        final Bitmap bmp = createBitmap(pluginContext, contentView, isBig, systemId);
        if (bmp == null) {
            XLog.e(TAG, "bmp is null,contentView=" + contentView);
        }
        remoteViews.setImageViewBitmap(R.id.im_main, bmp);
//        builder.setContent(remoteViews);
        //点击事件
        if (layoutId == R.layout.custom_notification) {
            //2个View
            new NotificationPendIntent(
                    createView(context, remoteViews, isBig, false),
                    createView(pluginContext, contentView, isBig, false),
                    clickIntents)
                    .set(remoteViews);
        }
        mNotificationActionCompat.hackNotification(notification);
        Notification notification1 = RemoteViewsCompat.clone(pluginContext, notification);
        if (Build.VERSION.SDK_INT >= 16 && isBig) {
            notification1.bigContentView = remoteViews;
        } else {
            notification1.contentView = remoteViews;
        }
        return notification1;
    }

    private void setDateTime(RemoteViews remoteViews, RemoteViewsCompat remoteViewsCompat, long time) {
        if (remoteViewsCompat.hasDateTime()) {
            int color = remoteViewsCompat.getColor();
            float size = remoteViewsCompat.getSize();
            if (color != 0) {
                remoteViews.setTextColor(R.id.time, color);
            } else {
                remoteViews.setTextColor(R.id.time, Color.GREEN);
            }
            if (Build.VERSION.SDK_INT >= 16) {
                if (size > 0) {
                    remoteViews.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, size);
                }
                if (remoteViewsCompat.getPaddingRight() >= 0) {
                    remoteViews.setViewPadding(R.id.time, 0, 0, remoteViewsCompat.getPaddingRight(), 0);
                }
            }
            remoteViews.setLong(R.id.time, "setTime", time);
        } else {
            remoteViews.setViewVisibility(R.id.time, View.INVISIBLE);
        }
    }

    /**
     * id和点击事件intent
     */
    private Map<Integer, PendingIntent> getClickIntents(RemoteViews remoteViews) {
        if (remoteViews == null) return null;
        Object mActionsObj = Reflect.on(remoteViews).get("mActions");
        Map<Integer, PendingIntent> map = new HashMap<>();
        if (mActionsObj instanceof Collection) {
            Collection mActions = (Collection) mActionsObj;
            Iterator iterable = mActions.iterator();
            while (iterable.hasNext()) {
                Object object = iterable.next();
                if (object != null) {
                    String action = null;
                    try {
                        action = Reflect.on(object).call("getActionName").get();
                    } catch (Exception e) {
                        action = object.getClass().getSimpleName();
                    }
                    if ("SetOnClickPendingIntent".equalsIgnoreCase(action)) {
                        int id = Reflect.on(object).get("viewId");
                        PendingIntent intent = Reflect.on(object).get("pendingIntent");
                        map.put(id, intent);
                    }
                }
            }
        }
        return map;
    }

    private View createView(final Context context, RemoteViews remoteViews, boolean isBig, boolean systemId) {
        if (remoteViews == null) return null;
        //notification_min_height 64
        //notification_max_height 256
        //notification_mid_height 128
        //notification_divider_height 2
        //standard_notification_panel_width 416
        //notification_panel_width match_parent -1px
        //notification_side_padding 8
        //notification_padding 4
        //TODO 需要适配
        int height = isBig ? notification_max_height : notification_min_height;
        int width = mNotificationLayoutCompat.getNotificationWidth(context, notification_panel_width, height, notification_side_padding);
        ViewGroup frameLayout = new FrameLayout(context);
        View view1 = remoteViews.apply(context, frameLayout);
//        XLog.d(TAG, "sp=" + notification_side_padding + ",w=" + width + ",h=" + height);
        View mCache;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        if (Build.VERSION.SDK_INT >= 23) {
            if (!systemId) {
                mCache = view1;
            } else {
                mCache = frameLayout;
                frameLayout.addView(view1, params);
            }
        } else {
            mCache = frameLayout;
            frameLayout.addView(view1, params);
        }
        if (view1 instanceof ViewGroup) {
            fixTextView((ViewGroup) view1);
        }
        if (!isBig) {
            mCache.layout(0, 0, width, height);
            mCache.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            mCache.layout(0, 0, mCache.getMeasuredWidth(), mCache.getMeasuredHeight());
        } else {
            mCache.layout(0, 0, width, height);
            mCache.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            mCache.layout(0, 0, mCache.getMeasuredWidth(), mCache.getMeasuredHeight());
        }
        //打印actions
//        logActions(remoteViews, view1);
        return mCache;
    }

    private void fixTextView(ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                if(isSingleLine(tv)){
                    tv.setSingleLine(false);
                    tv.setMaxLines(1);
                }
            } else if (v instanceof ViewGroup) {
                fixTextView((ViewGroup) v);
            }
        }
    }

    private boolean isSingleLine(TextView textView) {
        boolean singleLine = false;
        try {
            singleLine = Reflect.on(textView).get("mSingleLine");
        } catch (Exception e) {
            singleLine = (textView.getInputType() & EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
        }
        return singleLine;
    }

//    @SuppressWarnings("ResourceType")
//    private void logActions(RemoteViews remoteViews, View view) {
//        View test = view.findViewById(2131558602);
//        if (test != null) {
//            if (test instanceof TextView) {
//                Rect rect = new Rect();
//                test.getHitRect(rect);
//                ((TextView) test).setText("hllo2222222222");
//                ((TextView) test).setTextColor(Color.RED);
//                ((TextView) test).setEllipsize(null);
//                ((TextView) test).setSingleLine(false);
//                ((TextView) test).setMaxLines(1);
//                test.setBackgroundColor(Color.BLUE);
//                XLog.i("kk", "find text 1:" + rect + ",text=" + ((TextView) test).getText());
//            }
//        }
//        test = view.findViewById(2131558606);
//        if (test != null) {
//            if (test instanceof TextView) {
//                Rect rect = new Rect();
//                test.getHitRect(rect);
//                ((TextView) test).setText("hello");
//                XLog.i("kk", "find text 2:" + rect + ",text=" + ((TextView) test).getText());
//            }
//        }
//
//        try {
//            Object mActionsObj = Reflect.on(remoteViews).get("mActions");
//            if (mActionsObj instanceof Collection) {
//                Collection mActions = (Collection) mActionsObj;
//                Iterator iterable = mActions.iterator();
//                while (iterable.hasNext()) {
//                    Object o = iterable.next();
//                    String action = Reflect.on(o).call("getActionName").get();
//                    int viewId = Reflect.on(o).get("viewId");
//                    if (action != null && action.startsWith("ReflectionActionsetText10")) {
//                        XLog.d("kk", "set " + viewId + ",action=" + action + ",value=" + Reflect.on(o).get("value"));
//                    }
//                }
//            }
//        } catch (Exception e) {
//
//        }
//    }

    private Bitmap createBitmap(final Context context, RemoteViews remoteViews, boolean isBig, boolean systemId) {
        View mCache = createView(context, remoteViews, isBig, systemId);
        mCache.setDrawingCacheEnabled(true);
        mCache.buildDrawingCache();
        mCache.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        return mCache.getDrawingCache();
    }
}
