package com.lody.virtual.client.stub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

/**
 * @author Lody
 */

public class StubPendingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
                Intent realIntent = intent.getParcelableExtra("_VA_|_intent_");
        int userId = intent.getIntExtra("_VA_|_user_id_", VUserHandle.USER_ALL);
        if (realIntent != null) {
            VLog.d("IntentSender", "onReceive's realIntent =" + realIntent+",extra=" +VLog.toString(realIntent.getExtras()));
            Intent newIntent = ComponentUtils.redirectBroadcastIntent(realIntent, userId);
            if (newIntent != null) {
                context.sendBroadcast(newIntent);
            }
        }
    }
}
