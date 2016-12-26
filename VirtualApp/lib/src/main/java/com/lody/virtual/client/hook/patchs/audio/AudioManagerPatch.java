package com.lody.virtual.client.hook.patchs.audio;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;

import mirror.android.media.IAudioService;

/**
 * @author Lody
 *
 * @see android.media.AudioManager
 */

public class AudioManagerPatch extends PatchBinderDelegate {
	public AudioManagerPatch() {
		super(IAudioService.Stub.TYPE, Context.AUDIO_SERVICE);
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
}
