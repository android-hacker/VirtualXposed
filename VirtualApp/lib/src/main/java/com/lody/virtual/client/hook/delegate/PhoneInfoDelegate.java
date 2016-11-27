package com.lody.virtual.client.hook.delegate;

public interface PhoneInfoDelegate {

    String getDeviceId(String oldDeviceId);

    String getBluetoothAddress(String oldAddress);
}
