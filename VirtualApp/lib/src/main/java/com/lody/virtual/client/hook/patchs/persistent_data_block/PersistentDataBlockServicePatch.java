package com.lody.virtual.client.hook.patchs.persistent_data_block;

import android.os.ServiceManager;
import android.service.persistentdata.IPersistentDataBlockService;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.hook.binders.HookPersistentDataBlockServiceBinder;

/**
 * @author Lody
 */

public class PersistentDataBlockServicePatch extends PatchObject<IPersistentDataBlockService, HookPersistentDataBlockServiceBinder> {

    @Override
    protected HookPersistentDataBlockServiceBinder initHookObject() {
        return new HookPersistentDataBlockServiceBinder();
    }

    @Override
    public void inject() throws Throwable {
        getHookObject().injectService(HookPersistentDataBlockServiceBinder.SERVICE_NAME);
    }

    @Override
    protected void applyHooks() {
        super.applyHooks();
        addHook(new ResultStaticHook("write", -1));
        addHook(new ResultStaticHook("read", new byte[0]));
        addHook(new ResultStaticHook("wipe", null));
        addHook(new ResultStaticHook("getDataBlockSize", 0));
        addHook(new ResultStaticHook("getMaximumDataBlockSize", 0));
        addHook(new ResultStaticHook("setOemUnlockEnabled", 0));
        addHook(new ResultStaticHook("getOemUnlockEnabled", false));
    }

    @Override
    public boolean isEnvBad() {
        return getHookObject() != ServiceManager.getService(HookPersistentDataBlockServiceBinder.SERVICE_NAME);
    }
}
