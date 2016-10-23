package com.lody.virtual.client.hook.delegate;


import android.content.Intent;

import android.app.Activity;

public interface ActivityDelegate {
    void onActivityCreate(Activity activity);

    void onActivityResumed(Activity activity);

    void onActivityPaused(Activity activity);

    void onActivityDestroy(Activity activity);

    void onSendBroadcast(Intent intent);
}
