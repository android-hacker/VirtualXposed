package com.lody.virtual.client.hook.patchs.pm;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import mirror.android.content.pm.ParceledListSlice;

/**
 * @author Lody
 *         <p>
 *         <p>
 *         Android 4.4+
 */
@SuppressWarnings("unchecked")
@TargetApi(Build.VERSION_CODES.KITKAT)
/* package */ class QueryIntentContentProviders extends Hook {

    @Override
    public String getName() {
        return "queryIntentContentProviders";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        boolean slice = ParceledListSliceCompat.isReturnParceledListSlice(method);
        int userId = VUserHandle.myUserId();
        List<ResolveInfo> appResult = VPackageManager.get().queryIntentContentProviders((Intent) args[0], (String) args[1],
                (Integer) args[2], userId);
        Object _hostResult = method.invoke(who, args);
        List<ResolveInfo> hostResult = slice ? ParceledListSlice.getList.call(_hostResult)
                : (List) _hostResult;
        Iterator<ResolveInfo> iterator = hostResult.iterator();
        while (iterator.hasNext()) {
            ResolveInfo info = iterator.next();
            if (info == null || info.providerInfo == null || !isVisiblePackage(info.providerInfo.applicationInfo)) {
                iterator.remove();
            }
        }
        appResult.addAll(hostResult);
        if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
            return ParceledListSliceCompat.create(appResult);
        }
        return appResult;
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
