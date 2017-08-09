package com.lody.virtual.client.hook.delegate;


import android.app.Application;
import android.content.Intent;

import android.app.Activity;

public interface ComponentDelegate {

    ComponentDelegate EMPTY = new ComponentDelegate() {

        @Override
        public void beforeActivityCreate(Activity activity) {
            // Empty
        }

        @Override
        public void beforeActivityResume(Activity activity) {
            // Empty
        }

        @Override
        public void beforeActivityPause(Activity activity) {
            // Empty
        }

        @Override
        public void beforeActivityDestroy(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityCreate(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityResume(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityPause(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityDestroy(Activity activity) {
            // Empty
        }

        @Override
        public void onSendBroadcast(Intent intent) {
            // Empty
        }

        @Override
        public void beforeApplicationCreate(Application application) {
            // Empty
        }

        @Override
        public void afterApplicationCreate(Application application) {
            // Empty
        }
    };

    void beforeApplicationCreate(Application application);

    void afterApplicationCreate(Application application);

    void beforeActivityCreate(Activity activity);

    void beforeActivityResume(Activity activity);

    void beforeActivityPause(Activity activity);

    void beforeActivityDestroy(Activity activity);

    void afterActivityCreate(Activity activity);

    void afterActivityResume(Activity activity);

    void afterActivityPause(Activity activity);

    void afterActivityDestroy(Activity activity);

    void onSendBroadcast(Intent intent);
}
