package com.lody.virtual.client.hook.proxies.audio;

import android.content.Context;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;

import mirror.android.media.IAudioService;

/**
 * @author Lody
 *
 * @see android.media.AudioManager
 */

public class AudioManagerStub extends BinderInvocationProxy {
	public AudioManagerStub() {
		super(IAudioService.Stub.asInterface, Context.AUDIO_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceLastPkgMethodProxy("adjustVolume"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("adjustLocalOrRemoteStreamVolume"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("adjustSuggestedStreamVolume"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("adjustStreamVolume"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("adjustMasterVolume"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("setStreamVolume"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("setMasterVolume"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("setMicrophoneMute"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("setRingerModeExternal"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("setRingerModeInternal"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("setMode"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("avrcpSupportsAbsoluteVolume"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("abandonAudioFocus"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("requestAudioFocus"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("setWiredDeviceConnectionState"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("setSpeakerphoneOn"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("setBluetoothScoOn"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("stopBluetoothSco"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("startBluetoothSco"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("disableSafeMediaVolume"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("registerRemoteControlClient"));
		addMethodProxy(new ReplaceLastPkgMethodProxy("unregisterAudioFocusClient"));
	}
}
