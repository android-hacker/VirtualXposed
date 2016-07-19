package com.lody.virtual.client.hook.patchs.dropbox;

import android.content.Context;
import android.os.ServiceManager;

import com.android.internal.os.IDropBoxManagerService;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookDropBoxBinder;

/**
 * @author Lody
 */
@Patch({
        Hook_GetNextEntry.class,
})
public class DropBoxManagerPatch extends PatchObject<IDropBoxManagerService, HookDropBoxBinder> {

    @Override
    protected HookDropBoxBinder initHookObject() {
        return new HookDropBoxBinder();
    }

    @Override
    public void inject() throws Throwable {
        getHookObject().injectService(Context.DROPBOX_SERVICE);
    }

    @Override
    public boolean isEnvBad() {
        return getHookObject() != ServiceManager.getService(Context.DROPBOX_SERVICE);
    }
}
