package com.lc.interceptor.client.hook.patch.interceptor.telephony;

import android.os.Bundle;
import android.telephony.gsm.GsmCellLocation;

import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Method;

/**
 * 获取单个基站信息
 * Created by legency on 2016/8/21.
 */
public class Interceptor_GetCellLocation extends BaseInterceptorTelephony {

    @Override
    public String getName() {
        return "getCellLocation";
    }

    @Override
    public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
        modifyFakeGsmCellLocationBundle((Bundle) result);
        return super.afterCall(who, method, args, result);
    }


    private GsmCellLocation createFakeGsmCellLocation() {
        GsmCellLocation gsmCellLocation = new GsmCellLocation();
        gsmCellLocation.setLacAndCid(9500, 101010691);
        Reflect.on(gsmCellLocation).call("setPsc", -1);
        return gsmCellLocation;
    }

    private Bundle createFakeGsmCellLocationBundle() {
        Bundle bundle = new Bundle();
        modifyFakeGsmCellLocationBundle(bundle);
        return bundle;
    }

    private Bundle modifyFakeGsmCellLocationBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        bundle.getInt("lac", 9500);
        bundle.getInt("cid", 101010691);
        bundle.getInt("psc", -1);
        return bundle;
    }

    @Override
    public boolean isOnHookConsumed() {
        return false;
    }
}
