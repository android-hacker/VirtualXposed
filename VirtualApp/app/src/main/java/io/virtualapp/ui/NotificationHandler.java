package io.virtualapp.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.INotificationHandler;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.virtualapp.R;

/**
 * Created by 247321453 on 2016/7/12.
 */
public class NotificationHandler implements INotificationHandler {
    private static Map<Integer, String> sSystemLayoutResIds = new HashMap<Integer, String>(0);
    private static final boolean DRAW_NOTIFICATION = true;
    private int notification_min_height, notification_max_height, notification_mid_height;
    private int notification_panel_width;
    private int notification_side_padding;
    private int notification_padding;

    private void init(Context context) {
        if (sSystemLayoutResIds.size() == 0) {
            try {
                //read all com.android.internal.R
                Class clazz = Class.forName("com.android.internal.R$layout");
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    //public static final
                    if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                        try {
                            int id = field.getInt(null);
                            sSystemLayoutResIds.put(id, field.getName());
                        } catch (IllegalAccessException e) {
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        if (notification_panel_width == 0) {
            Context systemUi = null;
            try {
                systemUi = context.createPackageContext("com.android.systemui", Context.CONTEXT_IGNORE_SECURITY);
            } catch (PackageManager.NameNotFoundException e) {
            }
            notification_side_padding = getDimem(context, systemUi, "notification_side_padding", R.dimen.notification_side_padding);
            notification_panel_width = getDimem(context, systemUi, "notification_panel_width", R.dimen.notification_panel_width);
            if (notification_panel_width <= 0) {
                notification_panel_width = context.getResources().getDisplayMetrics().widthPixels;
            }
            notification_min_height = getDimem(context, systemUi, "notification_min_height", R.dimen.notification_min_height);
            notification_max_height = getDimem(context, systemUi, "notification_max_height", R.dimen.notification_max_height);
            notification_mid_height = getDimem(context, systemUi, "notification_mid_height", R.dimen.notification_mid_height);
            notification_padding = getDimem(context, systemUi, "notification_padding", R.dimen.notification_padding);
            //notification_collapse_second_card_padding
        }
    }

    public NotificationHandler() {
    }

    private int getDimem(Context context, Context sysContext, String name, int defId) {
        if (sysContext == null) {
            Log.w("kk", "get my");
            return Math.round(context.getResources().getDimension(defId));
        }
        int id = sysContext.getResources().getIdentifier(name, "dimen", "com.android.systemui");
        if (id != 0) {
            return Math.round(sysContext.getResources().getDimension(id));
        } else {
            Log.w("kk", "get my 2");
            return Math.round(context.getResources().getDimension(defId));
        }
    }

    @Override
    public boolean dealNotification(Context hostContext, String packageName, Object... args) throws Exception {
        init(hostContext);
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Notification) {
                Notification notification = (Notification) args[i];//nobug
                if (isPluginNotification(notification)) {
                    if (shouldBlock(notification)) {
                        //自定义布局通知栏
                        args[i] = replaceNotification(hostContext, packageName, notification);
                        return true;
                    } else {
                        //这里要修改通知。
                        hackNotification(notification);
                        return true;
                    }
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

    private boolean shouldBlockByRemoteViews(RemoteViews remoteViews) {
        if (remoteViews == null) {
//            Log.d("kk", "shouldBlockByRemoteViews is null");
            return false;
        } else if (sSystemLayoutResIds.containsKey(remoteViews.getLayoutId())) {
//            Log.d("kk", "shouldBlockByRemoteViews is systemId");
            return false;
        } else {
            return true;
        }
    }

    private boolean shouldBlock(Notification notification) {
        if (shouldBlockByRemoteViews(notification.contentView)) {
//            Log.d("kk", "shouldBlock contentView:" + notification.contentView.getClass().getName());
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (shouldBlockByRemoteViews(notification.tickerView)) {
//                Log.d("kk", "shouldBlock tickerView");
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (shouldBlockByRemoteViews(notification.bigContentView)) {
//                Log.d("kk", "shouldBlock bigContentView");
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (shouldBlockByRemoteViews(notification.headsUpContentView)) {
//                Log.d("kk", "shouldBlock headsUpContentView");
                return true;
            }
        }
        return false;
    }

    private Notification replaceNotification(Context context, String packageName, Notification notification) throws PackageManager.NameNotFoundException {
        Notification.Builder builder = new Notification.Builder(context);
        final Context pluginContext = VirtualCore.getCore().getContext().createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.graphics.drawable.Icon icon = notification.getSmallIcon();
            if (icon != null) {
                Bitmap bitmap = drawableToBitMap(icon.loadDrawable(VirtualCore.getCore().getContext()));
                if (bitmap != null) {
                    android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                    builder.setSmallIcon(newIcon);
                }
            }
            android.graphics.drawable.Icon icon2 = notification.getLargeIcon();
            if (icon2 != null) {
                Bitmap bitmap = drawableToBitMap(icon2.loadDrawable(VirtualCore.getCore().getContext()));
                if (bitmap != null) {
                    android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                    builder.setLargeIcon(newIcon);
                }
            }
        } else {
            if (!DRAW_NOTIFICATION) {
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = VirtualCore.getCore().getPackageManager().getApplicationInfo(packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                }
                if (applicationInfo != null) {
                    Drawable icon = VirtualCore.getCore().getPackageManager().getApplicationIcon(applicationInfo);
                    CharSequence title = VirtualCore.getCore().getPackageManager().getApplicationLabel(applicationInfo);
                    if (icon instanceof BitmapDrawable) {
                        builder.setLargeIcon(((BitmapDrawable) icon).getBitmap());
                    }
                    builder.setContentTitle(title);
                }
            } else {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
                //TODO 绘制图片，遍历action这只内容
                Bitmap bmp = null;
                try {
                    if (notification.contentView != null) {
                        bmp = createBitmap(notification.contentView, false);
                    } else {
                        bmp = createBitmap(notification.bigContentView, true);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                }
                remoteViews.setImageViewBitmap(R.id.im_main, bmp);
                //TODO 区域点击
                builder.setContent(remoteViews);
            }
            //com.android.internal.R.id.icon
            builder.setSmallIcon(context.getApplicationInfo().icon);
            builder.setContentIntent(notification.contentIntent);
            builder.setDeleteIntent(notification.deleteIntent);
            builder.setFullScreenIntent(notification.fullScreenIntent,
                    (notification.flags & Notification.FLAG_HIGH_PRIORITY) != 0);
        }
        Notification notification1;
        if (Build.VERSION.SDK_INT >= 16) {
            notification1 = builder.build();
        } else {
            notification1 = builder.getNotification();
        }
        notification1.flags = notification.flags;
        return notification1;
    }

    private Bitmap createBitmap(RemoteViews remoteViews, boolean isBig) throws PackageManager.NameNotFoundException {
        //notification_min_height 64
        //notification_max_height 256
        //notification_mid_height 128
        //notification_divider_height 2
        //standard_notification_panel_width 416
        //notification_panel_width match_parent -1px
        //notification_side_padding 8
        //notification_padding 4
        String aPackage = remoteViews.getPackage();
        final Context context = VirtualCore.getCore().getContext().createPackageContext(aPackage, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
//        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(remoteViews.getLayoutId(), null);
        //TODO 需要适配
        int sp =  notification_side_padding;// + notification_padding);
        int width = notification_panel_width - sp * 2;
        int height = isBig ? notification_max_height : notification_min_height;
        FrameLayout frameLayout = new FrameLayout(context);
        View view1 = remoteViews.apply(context, frameLayout, new RemoteViews.OnClickHandler() {
            @Override
            public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent) {
                //点击事件得另外处理，采用天气通的区域点击
                Log.i("kk", "click=" + pendingIntent.getIntent());
                return super.onClickHandler(view, pendingIntent, fillInIntent);
            }
        });

        //TODO 通知栏的宽高
        Object mActionsObj = Reflect.on(remoteViews).get("mActions");
        if (mActionsObj instanceof Collection) {
            Collection mActions = (Collection) mActionsObj;
            Resources resources = context.getResources();
            Iterator iterable = mActions.iterator();
            while (iterable.hasNext()) {
                Object object = iterable.next();
                //SetEmptyView
                //SetOnClickFillInIntent
                //SetPendingIntentTemplate
                //SetRemoteViewsAdapterList
                //SetRemoteViewsAdapterIntent
                //SetOnClickPendingIntent
                //SetDrawableParameters
                //ReflectionActionWithoutParams
                //BitmapReflectionAction

                //ReflectionAction
                //ViewGroupAction
                //TextViewDrawableAction
                //TextViewSizeAction
                //ViewPaddingAction
                //TextViewDrawableColorFilterAction
            }
        }

        Log.i("kk", "sp=" + sp + ",w=" + width + ",h=" + height);
        //736
        //128
        frameLayout.addView(view1);
        frameLayout.setDrawingCacheEnabled(true);
        frameLayout.buildDrawingCache(true);
        if (!isBig) {
            frameLayout.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            frameLayout.layout(0, 0, width, height);
        } else {
            frameLayout.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            frameLayout.layout(0, 0, width, height);
        }
        Bitmap bmp = frameLayout.getDrawingCache();
        Log.i("kk", "bmp=" + bmp);
        return bmp;
    }

    private void hackNotification(Notification notification) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        if (Build.VERSION.SDK_INT >= 21) {
            if (notification.contentView == null && notification.publicVersion != null) {
                notification.contentView = notification.publicVersion.contentView;
                notification.flags = notification.publicVersion.flags;
                notification.category = notification.publicVersion.category;
                notification.actions = notification.publicVersion.actions;
                notification.sound = notification.publicVersion.sound;
                Reflect.on(notification).set("mGroupKey", notification.publicVersion.getGroup());
            }
            hackNotificationCompat(notification);
            hackNotificationCompat(notification.publicVersion);
        } else {
            hackNotificationCompat(notification);
        }

    }

    private void hackNotificationCompat(Notification notification) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        //        remoteViews com.android.internal.R.layout.notification_template_material_media
        //        com.android.internal.R.layout.notification_template_material_big_media_narrow;
        //        com.android.internal.R.layout.notification_template_material_big_media;
        //        //getBaseLayoutResource
        //        R.layout.notification_template_material_base;
        //        //getBigBaseLayoutResource
        //        R.layout.notification_template_material_big_base;
        //        //getBigPictureLayoutResource
        //        R.layout.notification_template_material_big_picture;
        //        //getBigTextLayoutResource
        //        R.layout.notification_template_material_big_text;
        //        //getInboxLayoutResource
        //        R.layout.notification_template_material_inbox;
        //        //getActionLayoutResource
        //        R.layout.notification_material_action;
        //        //getActionTombstoneLayoutResource
        //        R.layout.notification_material_action_tombstone;
        if (notification != null) {
            notification.icon = VirtualCore.getCore().getContext().getApplicationInfo().icon;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                hackRemoteViews(notification.tickerView);
            }
            hackRemoteViews(notification.contentView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                hackRemoteViews(notification.bigContentView);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hackRemoteViews(notification.headsUpContentView);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.graphics.drawable.Icon icon = notification.getSmallIcon();
                if (icon != null) {
                    Bitmap bitmap = drawableToBitMap(icon.loadDrawable(VirtualCore.getCore().getContext()));
                    if (bitmap != null) {
                        android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                        Reflect.on(notification).set("mSmallIcon", newIcon);
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.graphics.drawable.Icon icon = notification.getLargeIcon();
                if (icon != null) {
                    Bitmap bitmap = drawableToBitMap(icon.loadDrawable(VirtualCore.getCore().getContext()));
                    if (bitmap != null) {
                        android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                        Reflect.on(notification).set("mLargeIcon", newIcon);
                    }
                }
            }
        }
    }

    private void hackRemoteViews(RemoteViews remoteViews) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        if (remoteViews != null && !VirtualCore.getCore().isHostPackageName(remoteViews.getPackage())) {
            Object mActionsObj = Reflect.on(remoteViews).get("mActions");
            if (mActionsObj instanceof Collection) {
                Collection mActions = (Collection) mActionsObj;
                String aPackage = remoteViews.getPackage();
                Resources resources = VirtualCore.getCore().getResources(aPackage);
                if (resources != null) {
                    Iterator iterable = mActions.iterator();
                    Class TextViewDrawableActionClass = null;
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            TextViewDrawableActionClass = Class.forName(RemoteViews.class.getName() + "$TextViewDrawableAction");
                        }
                    } catch (ClassNotFoundException e) {
                    }
                    Class ReflectionActionClass = Class.forName(RemoteViews.class.getName() + "$ReflectionAction");
                    while (iterable.hasNext()) {
                        Object action = iterable.next();
                        if (sSystemLayoutResIds.containsKey(remoteViews.getLayoutId())) {
                            if (ReflectionActionClass.isInstance(action)) {//???这里这样是对的么？
                                String methodName = Reflect.on(action).get("methodName");
                                //String methodName;,int type; Object value;
                                if ("setImageResource".equals(methodName)) { //setInt(viewId, "setImageResource", srcId);
                                    Object BITMAP = Reflect.on(action.getClass()).get("BITMAP");
                                    int resId = Reflect.on(action).get("value");
                                    Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
                                    Reflect.on(action).set("type", BITMAP);
                                    Reflect.on(action).set("value", bitmap);
                                    Reflect.on(action).set("methodName", "setImageBitmap");
                                } else if ("setImageURI".equals(methodName)) {//setUri(viewId, "setImageURI", uri);
                                    iterable.remove();   //TODO RemoteViews.setImageURI 其实应该适配的。
                                } else if ("setLabelFor".equals(methodName)) {
                                    iterable.remove();   //TODO RemoteViews.setLabelFor 其实应该适配的。
                                }
                            } else if (TextViewDrawableActionClass != null && TextViewDrawableActionClass.isInstance(action)) {
                                iterable.remove();
//                                if ("setTextViewCompoundDrawables".equals(methodName)) {
//                                    iterable.remove();   //TODO RemoteViews.setTextViewCompoundDrawables 其实应该适配的。
//                                } else if ("setTextViewCompoundDrawablesRelative".equals(methodName)) {
//                                    iterable.remove();   //TODO RemoteViews.setTextViewCompoundDrawablesRelative 其实应该适配的。
//                                }
                            }
                        } else {
                            //TODO 自定义布局
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Reflect.on(remoteViews).set("mApplication", VirtualCore.getCore().getContext().getApplicationInfo());
            } else {
                Reflect.on(remoteViews).set("mPackage", VirtualCore.getCore().getContext().getPackageName());
            }
        }
    }

    private Bitmap drawableToBitMap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
            return bitmapDrawable.getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }
}
