package com.lc.interceptor.client.hook.patch.interceptor.telephony;

/**
 * 获取当前的连接类型
 * Created by legency on 2016/8/20.
 */
public class Interceptor_GetActivePhoneTypeForSubscriber extends BaseInterceptorTelephony {
    @Override
    public String getName() {
        return "getActivePhoneTypeForSubscriber";
    }

}
