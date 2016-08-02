package com.lody.virtual.client.filter;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.service.ProviderCaller;
import com.lody.virtual.helper.compat.BundleCompat;
import com.lody.virtual.helper.component.BaseContentProvider;
import com.lody.virtual.helper.utils.Reflect;

import java.util.HashMap;

/**
 * Class:
 * Created by andy on 16-8-1.
 * TODO:
 */
public class ServiceContentProvider extends BaseContentProvider {
  public static Context context;
  public static String author = "com.virtual.service";
  public static HashMap<String, IBinder> mServiceCache = new HashMap<String, IBinder>();

  @Override
  public Bundle call(String method, String arg, Bundle extras) {
    Bundle bundle = new Bundle();
    BundleCompat.putBinder(bundle, "service", mServiceCache.get(method));
    return bundle;
  }

  @Override
  public boolean onCreate() {
    context = getContext();
    putService(IntentFilter.class);
    return super.onCreate();
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  public static void putService(Class clz) {
    if (clz != null) {
      IBinder binder = (IBinder) Reflect.on(clz).create().get();
      if (binder != null) {
        mServiceCache.put(clz.getSimpleName(), binder);
      }
    }
  }

  public static IBinder getBinder(String serviceName) {
    Bundle intentFilterBundle = ProviderCaller.call(ServiceContentProvider.author,
            VirtualCore.getCore().getContext(), serviceName, null, null);


    IBinder intentFilterBinder = null;
    if (intentFilterBundle != null) {
      intentFilterBinder = BundleCompat.getBinder(intentFilterBundle, "service");
    }

    return intentFilterBinder;
  }
}
