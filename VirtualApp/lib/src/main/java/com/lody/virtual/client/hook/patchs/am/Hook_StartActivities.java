package com.lody.virtual.client.hook.patchs.am;

import android.app.IApplicationThread;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 * @see android.app.IActivityManager#startActivities(IApplicationThread, String,
 * Intent[], String[], IBinder, Bundle, int)
 */
/* package */ class Hook_StartActivities extends Hook_BaseStartActivity {

  @Override
  public String getName() {
    return "startActivities";
  }

  @Override
  public Object onHook(Object who, Method method, Object... args) throws Throwable {
    super.onHook(who, method, args);

    int intentArrayIndex = ArrayUtils.indexOfFirst(args, Intent[].class);
    Intent[] intents = (Intent[]) args[intentArrayIndex];
    for (int N = 0; N < intents.length; N++) {
      ActivityUtils.replaceIntent(null, intents, N);
    }
    return method.invoke(who, args);
  }
}
