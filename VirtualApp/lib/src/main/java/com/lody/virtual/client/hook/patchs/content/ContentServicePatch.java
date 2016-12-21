package com.lody.virtual.client.hook.patchs.content;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;

import mirror.android.content.IContentService;

/**
 * @author Lody
 *
 * @see IContentService
 */

public class ContentServicePatch extends PatchBinderDelegate {

    public ContentServicePatch() {
        super(IContentService.Stub.TYPE, "content");
    }
}
