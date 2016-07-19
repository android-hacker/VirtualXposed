package com.lody.virtual.client.hook.patchs.dropbox;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see com.android.internal.os.IDropBoxManagerService#getNextEntry(String, long)
 *
 */
/*
    Caused by: java.lang.SecurityException: READ_LOGS permission required
               at android.os.Parcel.readException(Parcel.java:1620)
               at android.os.Parcel.readException(Parcel.java:1573)
               at com.android.internal.os.IDropBoxManagerService$Stub$Proxy.getNextEntry(IDropBoxManagerService.java:168)
               at android.os.DropBoxManager.getNextEntry(DropBoxManager.java:328)
 */
public class Hook_GetNextEntry extends Hook<DropBoxManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetNextEntry(DropBoxManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getNextEntry";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return null;
    }
}
