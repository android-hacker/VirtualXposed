package com.lc.interceptor.client.hook.patch.interceptor.telephony;

import android.telephony.NeighboringCellInfo;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by lichen:) on 2016/8/22.
 */
public class Interceptor_GetNeighboringCellInfo extends BaseInterceptorTelephony {
    
    @Override
    public String getName() {
        return "getNeighboringCellInfo";
    }

    @Override
    public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
        modifyNeighboringCellInfo((List<NeighboringCellInfo>) result);
        return super.afterCall(who, method, args, result);
    }

    private void modifyNeighboringCellInfo(List<NeighboringCellInfo> result) {
        if (isEnable()) {
            result.clear();
        }
    }


    @Override
    public boolean isOnHookConsumed() {
        return false;
    }

}
