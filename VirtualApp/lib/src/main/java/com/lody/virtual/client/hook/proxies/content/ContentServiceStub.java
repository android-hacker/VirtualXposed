package com.lody.virtual.client.hook.proxies.content;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.Inject;

import mirror.android.content.IContentService;

/**
 * @author Lody
 * @see IContentService
 */
@Inject(MethodProxies.class)
public class ContentServiceStub extends BinderInvocationProxy {

    public ContentServiceStub() {
        super(IContentService.Stub.asInterface, "content");
    }
}
