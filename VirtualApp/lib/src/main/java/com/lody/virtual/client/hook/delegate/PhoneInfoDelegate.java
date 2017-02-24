package com.lody.virtual.client.hook.delegate;

public interface PhoneInfoDelegate {

    /***
     * Fake the Device ID.
     *
     * @param oldDeviceId old DeviceId
     * @param userId      user
     */
    String getDeviceId(String oldDeviceId, int userId);

    /***
     * Fake the BluetoothAddress
     *
     * @param oldBluetoothAddress old BluetoothAddress
     * @param userId              user
     */
    String getBluetoothAddress(String oldBluetoothAddress, int userId);

    /***
     * Fake the macAddress
     *
     * @param oldMacAddress old MacAddress
     * @param userId        user
     */
    String getMacAddress(String oldMacAddress, int userId);
}
