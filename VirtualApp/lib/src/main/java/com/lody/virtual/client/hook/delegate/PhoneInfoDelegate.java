package com.lody.virtual.client.hook.delegate;

public interface PhoneInfoDelegate {

    /***
     * 根据虚拟userId返回不同的DeviceId
     * @param oldDeviceId 当前的DeviceId
     * @param vuserId 虚拟用户id
     */
    String getDeviceId(String oldDeviceId,int vuserId);

    /***
     * 根据虚拟userId返回不同的BluetoothAddress
     * @param oldAddress 当前的BluetoothAddress
     * @param vuserId 虚拟用户id
     */
    String getBluetoothAddress(String oldAddress,int vuserId);

    /***
     * 根据虚拟userId返回不同的mac
     * @param oldAddress 当前的mac
     * @param vuserId 虚拟用户id
     */
    String getMacAddress(String oldAddress,int vuserId);
}
