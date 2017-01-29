// IIntentFilterObserver.aidl
package com.lody.virtual.server.interfaces;

// Declare any non-default types here with import statements

interface IIntentFilterObserver {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

     Intent filter(in Intent intent);
     void setCallBack(IBinder callBack);
     IBinder getCallBack();
}
