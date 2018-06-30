package com.lody.virtual.client.hook.providers;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.MethodBox;
import com.lody.virtual.client.ipc.VPackageManager;

import java.lang.reflect.InvocationTargetException;

/**
 * @author weishu
 * @date 2018/6/29.
 */
class XPrivacyLuaHook extends SettingsProviderHook {

    Uri XLUA = Uri.parse("content://eu.faircode.xlua");

    XPrivacyLuaHook(Object base) {
        super(base);
    }

    @Override
    public Cursor query(MethodBox methodBox, Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder, Bundle originQueryArgs) throws InvocationTargetException {
        if (projection != null && projection.length > 0 &&
                projection[0] != null && projection[0].startsWith("xlua.")) {
            try {
                return VirtualCore.get().getContext().getContentResolver().query(XLUA, projection, selection, selectionArgs, sortOrder);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        return super.query(methodBox, url, projection, selection, selectionArgs, sortOrder, originQueryArgs);
    }


    @Override
    public Bundle call(MethodBox methodBox, String method, String arg, Bundle extras) throws InvocationTargetException {
        if ("xlua".equals(method)) {
            if ("getVersion".equals(arg)) {
                Bundle bundle = new Bundle();
                try {
                    int versionCode = VPackageManager.get().getPackageInfo("eu.faircode.xlua", 0, 0).versionCode;
                    bundle.putInt("version", versionCode);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return bundle;
            } else {
                return VirtualCore.get().getContext().getContentResolver().call(XLUA, method, arg, extras);
            }
        }
        return super.call(methodBox, method, arg, extras);
    }
}
