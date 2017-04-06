package io.virtualapp.delegate;

import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;


/**
 * Fake the Device ID.
 */
public class MyPhoneInfoDelegate implements PhoneInfoDelegate {

    @Override
    public String getDeviceId(String oldDeviceId, int userId) {
        return oldDeviceId;
    }

    @Override
    public String getBluetoothAddress(String oldAddress, int userId) {
        return oldAddress;
    }

    @Override
    public String getMacAddress(String oldAddress, int userId) {
        return oldAddress;
    }
}
