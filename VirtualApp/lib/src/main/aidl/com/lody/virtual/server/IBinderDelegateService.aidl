// IBinderDelegateService.aidl
package com.lody.virtual.server;

import android.content.ComponentName;

interface IBinderDelegateService {

   ComponentName getComponent();

   IBinder getService();

}
