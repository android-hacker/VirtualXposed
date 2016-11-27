package io.virtualapp;

import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;


/**
 * Fake the Device ID.
 */
class MyPhoneInfoDelegate implements PhoneInfoDelegate {

    @Override
    public String getDeviceId(String oldDeviceId) {
        return oldDeviceId;
    }

    @Override
    public String getBluetoothAddress(String oldAddress) {
        return oldAddress;
    }

}
