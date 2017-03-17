package com.lody.virtual.client.hook.providers;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;

import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import mirror.android.content.IContentProvider;

/**
 * @author Lody
 */

public class ProviderHook implements InvocationHandler {

    private static final Map<String, HookFetcher> PROVIDER_MAP = new HashMap<>();

    static {
        PROVIDER_MAP.put("settings", new HookFetcher() {
            @Override
            public ProviderHook fetch(boolean external, IInterface provider) {
                return new SettingsProviderHook(provider);
            }
        });
        PROVIDER_MAP.put("downloads", new HookFetcher() {
            @Override
            public ProviderHook fetch(boolean external, IInterface provider) {
                return new DownloadProviderHook(provider);
            }
        });
    }

    protected final Object mBase;

    public ProviderHook(Object base) {
        this.mBase = base;
    }

    private static HookFetcher fetchHook(String authority) {
        HookFetcher fetcher = PROVIDER_MAP.get(authority);
        if (fetcher == null) {
            fetcher = new HookFetcher() {
                @Override
                public ProviderHook fetch(boolean external, IInterface provider) {
                    if (external) {
                        return new ExternalProviderHook(provider);
                    }
                    return new InternalProviderHook(provider);
                }
            };
        }
        return fetcher;
    }

    private static IInterface createProxy(IInterface provider, ProviderHook hook) {
        if (provider == null || hook == null) {
            return null;
        }
        return (IInterface) Proxy.newProxyInstance(provider.getClass().getClassLoader(), new Class[]{
                IContentProvider.TYPE,
        }, hook);
    }

    public static IInterface createProxy(boolean external, String authority, IInterface provider) {
        if (provider instanceof Proxy && Proxy.getInvocationHandler(provider) instanceof ProviderHook) {
            return provider;
        }
        ProviderHook.HookFetcher fetcher = ProviderHook.fetchHook(authority);
        if (fetcher != null) {
            ProviderHook hook = fetcher.fetch(external, provider);
            IInterface proxyProvider = ProviderHook.createProxy(provider, hook);
            if (proxyProvider != null) {
                provider = proxyProvider;
            }
        }
        return provider;
    }

    public Bundle call(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {

        return (Bundle) method.invoke(mBase, args);
    }

    public Uri insert(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {

        return (Uri) method.invoke(mBase, args);
    }

    public Cursor query(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return (Cursor) method.invoke(mBase, args);
    }

    public String getType(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return (String) method.invoke(mBase, args);
    }

    public int bulkInsert(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return (int) method.invoke(mBase, args);
    }

    public int delete(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return (int) method.invoke(mBase, args);
    }

    public int update(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return (int) method.invoke(mBase, args);
    }

    public ParcelFileDescriptor openFile(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return (ParcelFileDescriptor) method.invoke(mBase, args);
    }

    public AssetFileDescriptor openAssetFile(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return (AssetFileDescriptor) method.invoke(mBase, args);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            processArgs(method, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            String name = method.getName();
            if ("call".equals(name)) {
                return call(method, args);
            } else if ("insert".equals(name)) {
                return insert(method, args);
            } else if ("getType".equals(name)) {
                return getType(method, args);
            } else if ("insert".equals(name)) {
                return insert(method, args);
            } else if ("delete".equals(name)) {
                return bulkInsert(method, args);
            } else if ("delete".equals(name)) {
                return bulkInsert(method, args);
            } else if ("bulkInsert".equals(name)) {
                return bulkInsert(method, args);
            } else if ("update".equals(name)) {
                return update(method, args);
            } else if ("openFile".equals(name)) {
                return openFile(method, args);
            } else if ("openAssetFile".equals(name)) {
                return openAssetFile(method, args);
            } else if ("query".equals(name)) {
                return query(method, args);
            }
            return method.invoke(mBase, args);
        } catch (Throwable e) {
            VLog.d("ProviderHook", "call: %s (%s) with error", method.getName(), Arrays.toString(args));
            if (e instanceof InvocationTargetException) {
                throw e.getCause();
            }
            throw e;
        }
    }

    protected void processArgs(Method method, Object... args) {

    }

    public interface HookFetcher {
        ProviderHook fetch(boolean external, IInterface provider);
    }
}
