package com.lody.virtual.client.hook.patchs.imms;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceSpecPkgHook;

import mirror.com.android.internal.telephony.IMms;


/**
 * @author Lody
 */
public class MmsPatch extends PatchBinderDelegate {

	public MmsPatch() {
		super(IMms.Stub.TYPE, "imms");
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
