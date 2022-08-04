package com.lody.virtual.client.badger;

import android.content.ComponentName;
import android.content.Intent;

import com.lody.virtual.remote.BadgerInfo;

/**
 * @author Lody
 */
public abstract class BroadcastBadger2 implements IBadger {

    public abstract String getAction();

    public abstract String getComponentKey();

    public abstract String getCountKey();

    @Override
    public BadgerInfo handleBadger(Intent intent) {
        BadgerInfo info = new BadgerInfo();
        String componentName = intent.getStringExtra(getComponentKey());
        ComponentName component = ComponentName.unflattenFromString(componentName);
        if (component != null) {
            info.packageName = component.getPackageName();
            info.className = component.getClassName();
            info.badgerCount = intent.getIntExtra(getCountKey(), 0);
            return info;
        }
        return null;
    }


    static class NewHtcHomeBadger1 extends BroadcastBadger2 {

        @Override
        public String getAction() {
            return "com.htc.launcher.action.SET_NOTIFICATION";
        }

        @Override
        public String getComponentKey() {
            return "com.htc.launcher.extra.COMPONENT";
        }


        @Override
        public String getCountKey() {
            return "com.htc.launcher.extra.COUNT";
        }
    }


}
