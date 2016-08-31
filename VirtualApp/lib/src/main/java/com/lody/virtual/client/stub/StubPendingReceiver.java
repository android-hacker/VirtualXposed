package com.lody.virtual.client.stub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lody.virtual.helper.utils.ComponentUtils;

/**
 * @author Lody
 */

public class StubPendingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent realIntent = intent.getParcelableExtra("_VA_|_intent_");
        if (realIntent != null) {
            Intent newIntent = ComponentUtils.redirectBroadcastIntent(realIntent);
            if (newIntent != null) {
                context.sendBroadcast(newIntent);
            }
        }
    }
}
