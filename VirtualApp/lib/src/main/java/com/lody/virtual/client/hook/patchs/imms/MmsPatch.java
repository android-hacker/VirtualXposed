package com.lody.virtual.client.hook.patchs.imms;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceSpecPkgHook;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.binders.IMMSBinderDelegate;

import mirror.android.os.ServiceManager;


/**
 * @author Lody
 *
 */
public class MmsPatch extends PatchDelegate<IMMSBinderDelegate> {
	@Override
	protected IMMSBinderDelegate createHookDelegate() {
		return new IMMSBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService("imms");
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call("imms");
	}

	@Override
	protected void onBindHooks() {
		addHook(new ReplaceSpecPkgHook("sendMessage", 1));
		addHook(new ReplaceSpecPkgHook("downloadMessage", 1));
		addHook(new ReplaceCallingPkgHook("importTextMessage"));
		addHook(new ReplaceCallingPkgHook("importMultimediaMessage"));
		addHook(new ReplaceCallingPkgHook("deleteStoredMessage"));
		addHook(new ReplaceCallingPkgHook("deleteStoredConversation"));
		addHook(new ReplaceCallingPkgHook("updateStoredMessageStatus"));
		addHook(new ReplaceCallingPkgHook("archiveStoredConversation"));
		addHook(new ReplaceCallingPkgHook("addTextMessageDraft"));
		addHook(new ReplaceCallingPkgHook("addMultimediaMessageDraft"));
		addHook(new ReplaceSpecPkgHook("sendStoredMessage", 1));
		addHook(new ReplaceCallingPkgHook("setAutoPersisting"));
	}
}
