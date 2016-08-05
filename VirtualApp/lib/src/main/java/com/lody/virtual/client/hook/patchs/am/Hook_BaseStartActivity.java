package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.service.filter.IntentFilterService;
import com.lody.virtual.service.interfaces.IIntentFilterObserver;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Class:
 * Created by andy on 16-8-4.
 * TODO:
 */
public class Hook_BaseStartActivity extends Hook {
  @Override
  public String getName() {
    return "";
  }

  @Override
  public Object onHook(Object who, Method method, Object... args) throws Throwable {
    int indexOfIntent = ArrayUtils.indexOfFirst(args, Intent.class);
    if (indexOfIntent != -1)
      args[indexOfIntent] = filterIntent((Intent) args[indexOfIntent]);
    else {
      indexOfIntent = ArrayUtils.indexOfFirst(args, Intent[].class);
      if (indexOfIntent == -1)
        return null;

      Intent[] intents = (Intent[]) args[indexOfIntent];
      for (int N = 0; N < intents.length; N++) {
        intents[N] = filterIntent(intents[N]);
      }
    }

    return null;
  }

  public Intent filterIntent(Intent intent) {
    VLog.i("Andy", "startActivity -> before %s", Arrays.toString(new Object[]{this, intent}));

    if (Intent.ACTION_INSTALL_PACKAGE.equals(intent.getAction()) ||
            (Intent.ACTION_VIEW.equals(intent.getAction()) &&
                    "file".equals(intent.getScheme())) ||
            "application/vnd.android.package-archive".equals(intent.getType())) {
      intent.setAction(ServiceManagerNative.ACTION_INSTALL_PACKAGE);
    } else if (Intent.ACTION_UNINSTALL_PACKAGE.equals(intent.getAction()) ||
            Intent.ACTION_DELETE.equals(intent.getAction()) ||
            (Intent.ACTION_VIEW.equals(intent.getAction()) &&
                    "package".equals(intent.getScheme()))) {
      intent.setAction(ServiceManagerNative.ACTION_UNINSTALL_PACKAGE);
    }

    IBinder intentFilterBinder = ServiceManagerNative.getService(ServiceManagerNative.INTENT_FILTER_MANAGER);
    IIntentFilterObserver intentFilter = IntentFilterService.getService(intentFilterBinder);
    if (intentFilter != null) {
      try {
        Intent newIntent = new Intent(intentFilter.filter(intent));
        VLog.i("Andy", "startActivity -> after %s", Arrays.toString(new Object[]{this, intent}));
        return newIntent;
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }

    VLog.i("Andy", "startActivity -> after %s", Arrays.toString(new Object[]{this, intent}));
    return intent;
  }
}

