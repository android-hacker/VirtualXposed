// IIntentFilter.aidl
package com.lody.virtual.client.filter;

// Declare any non-default types here with import statements

interface IIntentFilter {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    Intent filter (in Intent intent);
    IBinder getCallBack();
    void setCallBack(IBinder callback);
}
