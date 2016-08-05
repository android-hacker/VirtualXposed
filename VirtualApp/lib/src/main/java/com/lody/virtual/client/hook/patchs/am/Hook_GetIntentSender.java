package com.lody.virtual.client.hook.patchs.am;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.KeepService;
import com.lody.virtual.helper.ExtraConstants;

import java.lang.reflect.Method;

/**
 * @author Lody
 * @see android.app.ActivityManagerNative#getIntentSender(int, String, IBinder,
 * String, int, Intent[], String[], int, Bundle, int)
 */
/* package */ class Hook_GetIntentSender extends Hook_BaseStartActivity {

  @Override
  public String getName() {
    return "getIntentSender";
  }

  @Override
  public Object onHook(Object who, Method method, Object... args) throws Throwable {

    if (args[1] instanceof String && isAppPkg((String) args[1])) {
      args[1] = getHostPkg();
    }
    Object intentOrIntents = args[5];
    int flags = (int) args[0];
    boolean replaced = false;
    if (intentOrIntents instanceof Intent) {
      Intent intent = (Intent) args[5];
      Intent proxyIntent = redirectIntentSender(flags, intent);
      if (proxyIntent != null) {
        args[5] = proxyIntent;
        replaced = true;
      }
    } else if (intentOrIntents instanceof Intent[]) {
      Intent[] intents = (Intent[]) args[5];
      int N = intents.length;
      while (N-- > 0) {
        Intent intent = intents[N];
        Intent proxyIntent = redirectIntentSender(flags, intent);
        if (proxyIntent != null) {
          intents[N] = proxyIntent;
          replaced = true;
        }
      }
    }
    if (replaced) {
      if (args.length > 7 && args[7] instanceof Integer) {
        args[7] = PendingIntent.FLAG_UPDATE_CURRENT;
      }
      args[0] = ActivityManager.INTENT_SENDER_SERVICE;
    }
    return method.invoke(who, args);
  }

  private Intent redirectIntentSender(int flags, Intent intent) {
    if (flags == ActivityManager.INTENT_SENDER_ACTIVITY
            || flags == ActivityManager.INTENT_SENDER_SERVICE) {
      ActivityInfo activityInfo = VirtualCore.getCore().resolveActivityInfo(intent);
      if (activityInfo == null || !isAppPkg(activityInfo.packageName)) {
        return null;
      }
      Intent newIntent = new Intent(getHostContext(), KeepService.class);
      newIntent.putExtra(ExtraConstants.EXTRA_FLAGS, flags);
      newIntent.putExtra(ExtraConstants.EXTRA_INTENT, intent);
      newIntent.putExtra(ExtraConstants.EXTRA_WHAT, ExtraConstants.WHAT_PENDING_INTENT);
      return newIntent;
    }
    return null;
  }

  @Override
  public boolean isEnable() {
    return isAppProcess();
  }

}
