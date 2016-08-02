package com.lody.virtual.client.hook.patchs.am;

import android.app.ApplicationThreadNative;
import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.internal.content.ReferrerIntent;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalActivityManager;
import com.lody.virtual.client.service.ServiceManagerNative;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.compat.IApplicationThreadCompat;
import com.lody.virtual.helper.proto.VActRedirectResult;
import com.lody.virtual.helper.proto.VRedirectActRequest;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.service.filter.IntentFilterService;
import com.lody.virtual.service.interfaces.IIntentFilterObserver;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * @author Lody
 */
/* package */ class Hook_StartActivity extends Hook {

  @Override
  public String getName() {
    return "startActivity";
  }

  @Override
  public boolean beforeHook(Object who, Method method, Object... args) {
    return super.beforeHook(who, method, args);
  }

  @Override
  public Object onHook(Object who, Method method, Object... args) throws Throwable {
    int intentIndex = ArrayUtils.indexOfFirst(args, Intent.class);
    int resultToIndex;
    if (Build.VERSION.SDK_INT <= 15) {
      resultToIndex = 5;
    } else if (Build.VERSION.SDK_INT <= 17) {
      resultToIndex = 3;
    } else if (Build.VERSION.SDK_INT <= 19) {
      resultToIndex = 4;
    } else {
      resultToIndex = 4;
    }
    IBinder resultTo = (IBinder) args[resultToIndex];
    args[intentIndex] = filterIntent((Intent) args[intentIndex]);
    final Intent targetIntent = (Intent) args[intentIndex];
    ActivityInfo targetActInfo = VirtualCore.getCore().resolveActivityInfo(targetIntent);
    if (targetActInfo == null) {
      return method.invoke(who, args);
    }
    String packageName = targetActInfo.packageName;
    if (!VirtualCore.getCore().isAppInstalled(packageName)) {
      return method.invoke(who, args);
    }
    // Create Redirect Request
    VRedirectActRequest req = new VRedirectActRequest(targetActInfo, targetIntent.getFlags());
    req.fromHost = !VirtualCore.getCore().isVAppProcess();
    req.resultTo = resultTo;
    // Get Request Result
    VActRedirectResult result = LocalActivityManager.getInstance().redirectTargetActivity(req);
    if (result == null || result.justReturn) {
      return 0;
    }
    if (result.newIntentToken != null) {
      IApplicationThread appThread = ApplicationThreadNative.asInterface(result.targetClient);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        ReferrerIntent referrerIntent = new ReferrerIntent(targetIntent, packageName);
        IApplicationThreadCompat.scheduleNewIntent(appThread,
                Collections.singletonList(referrerIntent),
                result.newIntentToken);
      } else {
        IApplicationThreadCompat.scheduleNewIntent(appThread,
                Collections.singletonList(targetIntent),
                result.newIntentToken);
      }
      return 0;
    }
    // Workaround: issue #33 START
    if (result.replaceToken != null) {
      args[resultToIndex] = result.replaceToken;
    }
    // Workaround: issue #33 END
    ActivityInfo selectStubActInfo = result.stubActInfo;
    if (selectStubActInfo == null) {
      return method.invoke(who, args);
    }
    ActivityInfo callerActInfo = null;
    if (resultTo != null) {
      callerActInfo = LocalActivityManager.getInstance().getActivityInfo(resultTo);
    }
    // Mapping
    Intent stubIntent = new Intent();
    stubIntent.setClassName(selectStubActInfo.packageName, selectStubActInfo.name);
    stubIntent.setFlags(result.flags);
    stubIntent.putExtra(ExtraConstants.EXTRA_TARGET_INTENT, targetIntent);
    stubIntent.putExtra(ExtraConstants.EXTRA_STUB_ACT_INFO, selectStubActInfo);
    stubIntent.putExtra(ExtraConstants.EXTRA_TARGET_ACT_INFO, targetActInfo);
    if (callerActInfo != null) {
      stubIntent.putExtra(ExtraConstants.EXTRA_CALLER, callerActInfo);
    }
    args[intentIndex] = stubIntent;
    return method.invoke(who, args);
  }

  public Intent filterIntent(Intent intent) {
    IBinder intentFilterBinder = ServiceManagerNative.getService(ServiceManagerNative.INTENT_FILTER_MANAGER);
    IIntentFilterObserver intentFilter = IntentFilterService.getService(intentFilterBinder);
    if (intentFilter != null) {
      try {
        return new Intent(intentFilter.filter(intent));
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }

    return intent;
  }

  @Override
  public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
    return super.afterHook(who, method, args, result);
  }
}
