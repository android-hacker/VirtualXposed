package com.lody.virtual.client.hook.patchs.audio;

import android.content.Context;
import android.media.IAudioService;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.HookAudioBinder;

/**
 * @author Lody
 */

public class AudioManagerPatch extends PatchObject<IAudioService, HookAudioBinder> {

	@Override
	protected HookAudioBinder initHookObject() {
		return new HookAudioBinder();
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ReplaceLastPkgHook("adjustVolume"));
		addHook(new ReplaceLastPkgHook("adjustLocalOrRemoteStreamVolume"));
		addHook(new ReplaceLastPkgHook("adjustSuggestedStreamVolume"));
		addHook(new ReplaceLastPkgHook("adjustStreamVolume"));
		addHook(new ReplaceLastPkgHook("adjustMasterVolume"));
		addHook(new ReplaceLastPkgHook("setStreamVolume"));
		addHook(new ReplaceLastPkgHook("setMasterVolume"));
		addHook(new ReplaceLastPkgHook("requestAudioFocus"));
		addHook(new ReplaceLastPkgHook("registerRemoteControlClient"));
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.AUDIO_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.AUDIO_SERVICE) != getHookObject();
	}
}
