// IDeviceInfoManager.aidl
package com.lody.virtual.server;

import com.lody.virtual.remote.VDeviceInfo;

interface IDeviceInfoManager {

    VDeviceInfo getDeviceInfo(int userId);

    void updateDeviceInfo(int userId, in VDeviceInfo info);

}
