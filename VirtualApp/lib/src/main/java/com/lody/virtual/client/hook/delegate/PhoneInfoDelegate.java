package com.lody.virtual.client.hook.delegate;


import com.lody.virtual.client.hook.base.DelegateResult;

public interface PhoneInfoDelegate {
    DelegateResult<String> getDeviceId(String oldDeviceId);
}
