package com.lc.interceptor;
import com.lc.interceptor.IObjectWrapper;
import com.lc.interceptor.ICallBody;

interface IInterceptorCallManager {
    IObjectWrapper call(in ICallBody iCallBody);
}