package com.lody.virtual.client.stub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.lody.virtual.client.local.VActivityManager;

/**
 * @author Lody
 */

public class StubPendingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        Intent intent = getIntent();
        Intent realIntent = intent.getParcelableExtra("_VA_|_intent_");
        int userId = intent.getIntExtra("_VA_|_user_id_", -1);
        realIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            VActivityManager.get().startActivity(intent, userId);
            startActivity(realIntent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
