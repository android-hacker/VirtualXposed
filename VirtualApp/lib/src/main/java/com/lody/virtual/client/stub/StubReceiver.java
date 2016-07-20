package com.lody.virtual.client.stub;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.lody.virtual.client.core.VirtualCore;

public class StubReceiver extends BroadcastReceiver {
    BroadcastReceiver parent;
    Context proxy;
    String packageName;

    public StubReceiver(BroadcastReceiver parent, String packageName) {
        this.parent = parent;
        this.packageName = packageName;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                ComponentName oldComponent = VirtualCore.getOriginComponentName(action);
                if (oldComponent != null) {
                    intent.setComponent(oldComponent);
                    intent.setAction(null);
                }
            }
        }
        if (parent != null) {
            parent.onReceive(context, intent);
        }
    }
}
