// IContentManager.aidl
package com.lody.virtual.service;
import android.app.IActivityManager.ContentProviderHolder;

interface IContentManager {

    void publishContentProviders(in List<ContentProviderHolder> holderList);

    ContentProviderHolder getContentProvider(String auth);

}
