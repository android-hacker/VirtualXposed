package com.lody.virtual.client.hook.binders;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.ICameraService;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.HookBinder;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class HookCameraBinder extends HookBinder<ICameraService> {

    @Override
    protected IBinder queryBaseBinder() {
        return ServiceManager.getService(Context.CAMERA_SERVICE);
    }

    @Override
    protected ICameraService createInterface(IBinder baseBinder) {
        return ICameraService.Stub.asInterface(baseBinder);
    }
}
