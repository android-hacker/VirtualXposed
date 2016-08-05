package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.content.Context;
import android.media.IAudioService;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 */

public class HookAudioBinder extends HookBinder<IAudioService> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.AUDIO_SERVICE);
	}

	@Override
	protected IAudioService createInterface(IBinder baseBinder) {
		return IAudioService.Stub.asInterface(baseBinder);
	}
}
