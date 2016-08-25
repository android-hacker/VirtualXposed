package com.lody.virtual.client.hook.patchs.audio;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.AudioBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */

public class AudioManagerPatch extends PatchDelegate<AudioBinderDelegate> {

	@Override
	protected AudioBinderDelegate createHookDelegate() {
		return new AudioBinderDelegate();
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
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
		ServiceManager.sCache.get().put(Context.AUDIO_SERVICE, getHookDelegate());
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call(Context.AUDIO_SERVICE) != getHookDelegate();
	}
}
