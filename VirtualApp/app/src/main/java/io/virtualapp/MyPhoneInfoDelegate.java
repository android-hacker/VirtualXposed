package io.virtualapp;

import android.text.TextUtils;

import com.lody.virtual.client.hook.base.DelegateResult;
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;
import com.lody.virtual.os.VUserHandle;


/**
 * Fake the Device ID.
 */
class MyPhoneInfoDelegate implements PhoneInfoDelegate {

    @Override
    public DelegateResult<String> getDeviceId(String oldDeviceId) {
        String n = oldDeviceId.replace("0", "1");
        int userId = VUserHandle.myUserId();
        if (TextUtils.isDigitsOnly(oldDeviceId)) {
            int len = oldDeviceId.length();
            long l = Long.parseLong(oldDeviceId);
            l += (userId + 1);
            n = String.format("%0" + len + "d", l);
        }
        return new DelegateResult<String>(n);
    }

}
