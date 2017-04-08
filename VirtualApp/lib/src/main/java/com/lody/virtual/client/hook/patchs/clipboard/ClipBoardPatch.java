package com.lody.virtual.client.hook.patchs.clipboard;

import android.content.Context;
import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.helper.compat.BuildCompat;

import mirror.android.content.ClipboardManager;
import mirror.android.content.ClipboardManagerOreo;

/**
 * @author Lody
 * @see ClipboardManager
 */
public class ClipBoardPatch extends PatchBinderDelegate {

    public ClipBoardPatch() {
        super(getInterface(), Context.CLIPBOARD_SERVICE);
    }

    private static IInterface getInterface() {
        if (BuildCompat.isOreo()) {
            android.content.ClipboardManager cm = (android.content.ClipboardManager)
                    VirtualCore.get().getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            return ClipboardManagerOreo.mService.get(cm);
        } else {
            return ClipboardManager.getService.call();
        }
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addHook(new ReplaceLastPkgHook("getPrimaryClip"));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            addHook(new ReplaceLastPkgHook("setPrimaryClip"));
            addHook(new ReplaceLastPkgHook("getPrimaryClipDescription"));
            addHook(new ReplaceLastPkgHook("hasPrimaryClip"));
            addHook(new ReplaceLastPkgHook("addPrimaryClipChangedListener"));
            addHook(new ReplaceLastPkgHook("removePrimaryClipChangedListener"));
            addHook(new ReplaceLastPkgHook("hasClipboardText"));
        }
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        if (BuildCompat.isOreo()) {
            android.content.ClipboardManager cm = (android.content.ClipboardManager)
                    VirtualCore.get().getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipboardManagerOreo.mService.set(cm, getHookDelegate().getProxyInterface());
        } else {
            ClipboardManager.sService.set(getHookDelegate().getProxyInterface());
        }
    }
}
