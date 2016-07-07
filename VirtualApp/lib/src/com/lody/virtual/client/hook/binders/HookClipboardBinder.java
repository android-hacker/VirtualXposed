package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.content.Context;
import android.content.IClipboard;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookClipboardBinder extends HookBinder<IClipboard> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.CLIPBOARD_SERVICE);
	}

	@Override
	protected IClipboard createInterface(IBinder baseBinder) {
		return IClipboard.Stub.asInterface(baseBinder);
	}
}
