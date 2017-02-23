package com.lody.virtual.client.hook.delegate;

public interface PhoneInfoDelegate {

    String getDeviceId(String oldDeviceId,int vuserId);

    String getBluetoothAddress(String oldAddress,int vuserId);

    String getMacAddress(String oldAddress,int vuserId);
}
