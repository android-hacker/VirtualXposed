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
		addHook(new ReplaceLastPkgHook("setMicrophoneMute"));
		addHook(new ReplaceLastPkgHook("setRingerModeExternal"));
		addHook(new ReplaceLastPkgHook("setRingerModeInternal"));
		addHook(new ReplaceLastPkgHook("setMode"));
		addHook(new ReplaceLastPkgHook("avrcpSupportsAbsoluteVolume"));
		addHook(new ReplaceLastPkgHook("abandonAudioFocus"));
		addHook(new ReplaceLastPkgHook("requestAudioFocus"));
		addHook(new ReplaceLastPkgHook("setWiredDeviceConnectionState"));
		addHook(new ReplaceLastPkgHook("setSpeakerphoneOn"));
		addHook(new ReplaceLastPkgHook("setBluetoothScoOn"));
		addHook(new ReplaceLastPkgHook("stopBluetoothSco"));
		addHook(new ReplaceLastPkgHook("startBluetoothSco"));
		addHook(new ReplaceLastPkgHook("disableSafeMediaVolume"));
		addHook(new ReplaceLastPkgHook("registerRemoteControlClient"));
		addHook(new ReplaceLastPkgHook("unregisterAudioFocusClient"));
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
