package com.lody.virtual.client.filter;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.helper.utils.VLog;

/**
 * Class:
 * Created by andy on 16-8-1.
 * TODO:
 */
public class IntentFilter extends IIntentFilter.Stub {
  public static IIntentFilter intentFilter = null;

  @Override
  public Intent filter(Intent intent) throws RemoteException {
    return intentFilter != null ? intentFilter.filter(intent) : intent;
  }

  @Override
  public IBinder getCallBack() throws RemoteException {
    return intentFilter.asBinder();
  }

  @Override
  public void setCallBack(IBinder callback) throws RemoteException {
    intentFilter = IntentFilter.asInterface(callback);
  }
}
