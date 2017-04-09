package com.lody.virtual.client.hook.proxies.content;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;

import mirror.android.content.IContentService;

/**
 * @author Lody
 *
 * @see IContentService
 */

public class ContentServiceStub extends BinderInvocationProxy {

    public ContentServiceStub() {
        super(IContentService.Stub.asInterface, "content");
    }
}
