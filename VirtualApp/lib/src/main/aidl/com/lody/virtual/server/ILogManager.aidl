// ILogManager.aidl
package com.lody.virtual.server;

interface ILogManager {

    void reportLog(int level, in String tag, in String msg);

}
