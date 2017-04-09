package com.lody.virtual.client.hook.proxies.imms;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.ReplaceSpecPkgMethodProxy;

import mirror.com.android.internal.telephony.IMms;


/**
 * @author Lody
 */
public class MmsStub extends BinderInvocationProxy {

	public MmsStub() {
		super(IMms.Stub.asInterface, "imms");
	}

	@Override
	protected void onBindMethods() {
		addMethodProxy(new ReplaceSpecPkgMethodProxy("sendMessage", 1));
		addMethodProxy(new ReplaceSpecPkgMethodProxy("downloadMessage", 1));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("importTextMessage"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("importMultimediaMessage"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("deleteStoredMessage"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("deleteStoredConversation"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("updateStoredMessageStatus"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("archiveStoredConversation"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("addTextMessageDraft"));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("addMultimediaMessageDraft"));
		addMethodProxy(new ReplaceSpecPkgMethodProxy("sendStoredMessage", 1));
		addMethodProxy(new ReplaceCallingPkgMethodProxy("setAutoPersisting"));
	}
}
