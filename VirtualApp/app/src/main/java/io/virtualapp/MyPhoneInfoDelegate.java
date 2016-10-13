package io.virtualapp;

import android.text.TextUtils;

import com.lody.virtual.client.hook.base.DelegateResult;
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;
import com.lody.virtual.os.VUserHandle;

public class MyPhoneInfoDelegate implements PhoneInfoDelegate {
    @Override
    public DelegateResult<String> getDeviceId(Object old) {
        if (old instanceof String) {
            String str = (String) old;
            String n = str.replace("0", "1");
            int userId = VUserHandle.myUserId();
            if (TextUtils.isDigitsOnly(str)) {
                int len = str.length();
                long l = Long.parseLong(str);
                l += (userId + 1);
                n = String.format("%0" + len + "d", l);
            }
            return new DelegateResult<String>(n);
        }
        return null;
    }
}
