package com.lc.interceptor.service;

import android.util.Log;

import com.lc.interceptor.ICallBody;
import com.lc.interceptor.IInterceptorCallManager;
import com.lc.interceptor.IObjectWrapper;
import com.lc.interceptor.service.providers.ConnectivityProvider;
import com.lc.interceptor.service.providers.TelephonyManagerProvider;
import com.lc.interceptor.service.providers.base.InterceptorDataProvider;
import com.lc.interceptor.service.providers.LocationManagerProvider;
import com.lc.interceptor.service.providers.WifiManagerProvider;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lichen:) on 2016/9/9.
 */
public class VInterceptorService extends IInterceptorCallManager.Stub {

    public static final String TAG = VInterceptorService.class.getName();
    public static VInterceptorService sService = new VInterceptorService();
    private Map<Class<?>, InterceptorDataProvider> dataProviders = new HashMap<>(12);

    public static VInterceptorService get() {
        return sService;
    }

    public VInterceptorService() {
        init();
    }

    private void init() {
        add(new ConnectivityProvider());
        add(new LocationManagerProvider());
        add(new WifiManagerProvider());
        add(new TelephonyManagerProvider());
    }

    private void add(InterceptorDataProvider provider) {
        if (dataProviders.containsKey(provider.getDelegatePatch())) {
            Log.e(TAG, provider.getDelegatePatch().getName() + " is already added");
        } else {
            dataProviders.put(provider.getDelegatePatch(), provider);
        }
    }

    @Override
    public IObjectWrapper call(ICallBody iCall) {
        return dispatchCall(iCall);
    }

    private IObjectWrapper dispatchCall(ICallBody call) {
        InterceptorDataProvider interceptorDataProvider = null;
        try {
            //todo reflection may has bad performance   package name maybe alternative
            Class<?> clazz = Class.forName(call.module);
            interceptorDataProvider = dataProviders.get(clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (interceptorDataProvider == null) {
            Log.e(TAG, call.module + " provider not found");
            return null;
        }
        Object object;
        //三种方式分发 寻找方法 可以优化为 先找名字的所有方法 按优先级匹配
        //1.直接call
        try {
            object = Reflect.on(interceptorDataProvider).call(call.method, call.args).get();
            return wrap(object);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        //2.包裹 为 Object[] call requestLocationUpdates 这种多版本的
        try {
            Object[] arg = {call.args};
            object = Reflect.on(interceptorDataProvider).call(call.method, arg).get();
            return wrap(object);
        } catch (Exception e2) {
//            e2.printStackTrace();
        }
        // 寻找同名直接return的
        try {
            object = tryUseNameAsMethod(call, interceptorDataProvider);
            return wrap(object);
        } catch (Exception e1) {
//            e1.printStackTrace();
        }
        Log.e(TAG, call.module + " call " + call.method + " failed");
        return wrap(null);
    }

    private IObjectWrapper wrap(Object object) {
        if (object instanceof InterceptorDataProvider) {
            //Reflect get the original object when void returns
            return null;
        }
        return new IObjectWrapper(object);
    }


    private Object tryUseNameAsMethod(ICallBody call, InterceptorDataProvider interceptorDataProvider) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method[] declaredMethods = interceptorDataProvider.getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getName().equals(call.method)) {
                method.setAccessible(true);
                return method.invoke(interceptorDataProvider);
            }
        }
        throw new NoSuchMethodException(call.method + " not found same name method");
    }
}
