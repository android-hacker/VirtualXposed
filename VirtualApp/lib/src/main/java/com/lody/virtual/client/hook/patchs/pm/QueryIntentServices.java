package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;

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
 */
@SuppressWarnings("unchecked")
/* package */ class QueryIntentServices extends Hook {

    @Override
    public String getName() {
        return "queryIntentServices";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        boolean slice = ParceledListSliceCompat.isReturnParceledListSlice(method);
        int userId = VUserHandle.myUserId();
        List<ResolveInfo> appResult = VPackageManager.get().queryIntentServices((Intent) args[0],
                (String) args[1], (Integer) args[2], userId);
        Object _hostResult = method.invoke(who, args);
        List<ResolveInfo> hostResult = slice ? ParceledListSlice.getList.call(_hostResult)
                : (List) _hostResult;
        Iterator<ResolveInfo> iterator = hostResult.iterator();
        while (iterator.hasNext()) {
            ResolveInfo info = iterator.next();
            if (info == null || info.serviceInfo == null || !isVisiblePackage(info.serviceInfo.applicationInfo)) {
                iterator.remove();
            }
        }
        appResult.addAll(hostResult);
        if (slice) {
            return ParceledListSliceCompat.create(appResult);
        }
        return appResult;
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
