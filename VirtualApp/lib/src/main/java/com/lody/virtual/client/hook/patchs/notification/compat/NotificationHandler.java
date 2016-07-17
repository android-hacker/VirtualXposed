package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import com.lody.virtual.R;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppInfo;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.XLog;

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
    private static final String TAG = NotificationHandler.class.getName();

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
        if (sysContext == null) {
            Log.w("kk", "get my");
            return defId == 0 ? 0 : Math.round(context.getResources().getDimension(defId));
        }
        int id = sysContext.getResources().getIdentifier(name, "dimen", "com.android.systemui");
        if (id != 0) {
            return Math.round(sysContext.getResources().getDimension(id));
        } else {
            Log.w("kk", "get my 2");
            return defId == 0 ? 0 : Math.round(context.getResources().getDimension(defId));
        }
    }

    public boolean dealNotification(Context hostContext, String packageName, Object... args) throws Exception {
        init(hostContext);
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Notification) {
                Notification notification = (Notification) args[i];//nobug
                if (isPluginNotification(notification)) {
                    //双开模式，icon还是va的
//                    if(VirtualCore.getCore().isOutsideInstalled(packageName))
                    {
//                        //双开模式，貌似icon不太对
//                        notification.icon = hostContext.getApplicationInfo().icon;
//                        //23的icon
//                        mNotificationActionCompat.builderNotificationIcon(notification);
//                    }else {

                        //直接处理了
                        args[i] = replaceNotification(hostContext, packageName, notification);
//                    if (mNotificationActionCompat.shouldBlock(notification)) {
////                        //自定义布局通知栏
//                        args[i] = replaceNotification(hostContext, packageName, notification);
//                    } else {
////                        //这里要修改原生的通知，是否也和上面一样的处理？
//                        mNotificationActionCompat.hackNotification(notification);
//                    }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPluginNotification(Notification notification) {
        if (notification == null) {
            return false;
        }


        if (notification.contentView != null && !VirtualCore.getCore().isHostPackageName(notification.contentView.getPackage())) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (notification.tickerView != null && !VirtualCore.getCore().isHostPackageName(notification.tickerView.getPackage())) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (notification.bigContentView != null && !VirtualCore.getCore().isHostPackageName(notification.bigContentView.getPackage())) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (notification.headsUpContentView != null && !VirtualCore.getCore().isHostPackageName(notification.headsUpContentView.getPackage())) {
                return true;
            }
            if (notification.publicVersion != null && notification.publicVersion.contentView != null && !VirtualCore.getCore().isHostPackageName(notification.publicVersion.contentView.getPackage())) {
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
                        if (!VirtualCore.getCore().isHostPackageName(mString1)) {
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
                        if (!VirtualCore.getCore().isHostPackageName(mString1)) {
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
                        return !VirtualCore.getCore().isHostPackageName(applicationInfo.packageName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private Notification replaceNotification(Context context, String packageName, Notification notification) throws PackageManager.NameNotFoundException {
        final Context pluginContext = VirtualCore.getCore().getContext().createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        Context inflationContext = new ContextWrapperCompat(context, pluginContext);
        //build
        Notification.Builder builder = new Notification.Builder(context);
        //icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNotificationActionCompat.builderNotificationIcon(notification, builder);
        } else {
            builder.setSmallIcon(context.getApplicationInfo().icon);
        }
//        if (!DRAW_NOTIFICATION) {
//            //只是显示个简单通知栏
//            ApplicationInfo applicationInfo = null;
//            try {
//                applicationInfo = VirtualCore.getCore().getPackageManager().getApplicationInfo(packageName, 0);
//            } catch (PackageManager.NameNotFoundException e) {
//            }
//            if (applicationInfo != null) {
//                Drawable icon = VirtualCore.getCore().getPackageManager().getApplicationIcon(applicationInfo);
//                CharSequence title = VirtualCore.getCore().getPackageManager().getApplicationLabel(applicationInfo);
//                if (icon instanceof BitmapDrawable) {
//                    builder.setLargeIcon(((BitmapDrawable) icon).getBitmap());
//                }
//                builder.setContentTitle(title);
//            }
//        } else {

        RemoteViews contentView;
        //大通知栏
        boolean isBig;
        if (notification.contentView != null) {
            isBig = false;
            contentView = notification.contentView;
        } else {
            isBig = true;
            if (Build.VERSION.SDK_INT >= 16) {
                contentView = notification.bigContentView;
            } else {
                contentView = null;
            }
        }


        //双开模式下,直接调用原来的
        AppInfo appInfo = VirtualCore.getCore().findApp(packageName);

        if (appInfo != null && appInfo.isInstalled() && contentView != null) {
            try {
                ApplicationInfo applicationInfo = VirtualCore.getCore().getUnHookPackageManager().getApplicationInfo(packageName, 0);
                applicationInfo.packageName = VirtualCore.getCore().getHostPkg();
                Reflect.on(contentView).set("mApplication", applicationInfo);
                return notification;
            } catch (Exception e) {
                XLog.e(TAG, "error:" + e);
            }
        }

        Map<Integer, PendingIntent> clickIntents = getClickIntents(contentView);
        //如果就一个点击事件，没必要用复杂view
        int layoutId = (clickIntents == null || clickIntents.size() == 0) ?
                R.layout.custom_notification_lite :
                R.layout.custom_notification;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
        //绘制图
        Bitmap bmp = createBitmap(inflationContext, contentView, isBig);
        //测试用代码
//        Canvas canvas=new Canvas(bmp);
//        Paint paint=new Paint();
//        paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        paint.setColor(Color.RED);
//        canvas.drawRect(0,0,50,50,paint);
        remoteViews.setImageViewBitmap(R.id.im_main, bmp);
        builder.setContent(remoteViews);
//        }
        //com.android.internal.R.id.icon
        builder.setContentIntent(notification.contentIntent);
        builder.setDeleteIntent(notification.deleteIntent);
        builder.setFullScreenIntent(notification.fullScreenIntent,
                (notification.flags & Notification.FLAG_HIGH_PRIORITY) != 0);

        Notification notification1;
        if (Build.VERSION.SDK_INT >= 16) {
            notification1 = builder.build();
        } else {
            notification1 = builder.getNotification();
        }
        notification1.flags = notification.flags;
        return notification1;
    }

    /**
     * id和点击事件intent
     */
    private Map<Integer, PendingIntent> getClickIntents(RemoteViews remoteViews) {
        Object mActionsObj = Reflect.on(remoteViews).get("mActions");
        Map<Integer, PendingIntent> map = new HashMap<>();
        if (mActionsObj instanceof Collection) {
            Collection mActions = (Collection) mActionsObj;
            Iterator iterable = mActions.iterator();
            while (iterable.hasNext()) {
                Object object = iterable.next();
                if (object != null) {
                    String action = Reflect.on(object).call("getActionName").get();
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

    private Bitmap createBitmap(final Context context, RemoteViews remoteViews, boolean isBig) {
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
        int sp = (Build.VERSION.SDK_INT >= 21) ? notification_side_padding : 0;// + notification_padding);
        int height = isBig ? notification_max_height : notification_min_height;
        int width = mNotificationLayoutCompat.getNotificationWidth(context, notification_panel_width - sp * 2, height);
        ViewGroup frameLayout = new FrameLayout(context);
        View view1 = remoteViews.apply(context, frameLayout, new RemoteViews.OnClickHandler() {
            @Override
            public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent) {
                //点击事件得另外处理，采用天气通的区域点击
                XLog.i(TAG, "click=" + pendingIntent.getIntent());
                return super.onClickHandler(view, pendingIntent, fillInIntent);
            }
        });
        XLog.i(TAG, "sp=" + sp + ",w=" + width + ",h=" + height);
        Bitmap bmp;
        View mCache;
        if (Build.VERSION.SDK_INT >= 23) {
            mCache = view1;
        } else {
            mCache = frameLayout;
            frameLayout.addView(view1);
        }
        mCache.setDrawingCacheEnabled(true);
        mCache.buildDrawingCache(true);
        if (!isBig) {
            frameLayout.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            frameLayout.layout(0, 0, width, height);
        } else {
            frameLayout.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            frameLayout.layout(0, 0, width, height);
        }
        bmp = mCache.getDrawingCache();
        return bmp;
    }
}
