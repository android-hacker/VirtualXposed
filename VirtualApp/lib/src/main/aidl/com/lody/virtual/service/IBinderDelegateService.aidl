// IBinderDelegateService.aidl
package com.lody.virtual.service;

import android.content.ComponentName;

interface IBinderDelegateService {

   ComponentName getComponent();

   IBinder getService();

}
