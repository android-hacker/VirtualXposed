package com.lody.virtual.client.hook.patchs.imms;

import com.android.internal.telephony.IMms;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookIMMSBinder;

import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@Patch({Hook_AddMultimediaMessageDraft.class, Hook_AddTextMessageDraft.class, Hook_ArchiveStoredConversation.class,
		Hook_DeleteStoredConversation.class, Hook_DeleteStoredMessage.class, Hook_DownloadMessage.class,
		Hook_ImportMultimediaMessage.class, Hook_ImportTextMessage.class, Hook_SendMessage.class,
		Hook_SendStoredMessage.class, Hook_SetAutoPersisting.class, Hook_UpdateStoredMessageStatus.class})
public class MmsPatch extends PatchObject<IMms, HookIMMSBinder> {
	@Override
	protected HookIMMSBinder initHookObject() {
		return new HookIMMSBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(HookIMMSBinder.SERVICE_NAME);
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(HookIMMSBinder.SERVICE_NAME);
	}
}
