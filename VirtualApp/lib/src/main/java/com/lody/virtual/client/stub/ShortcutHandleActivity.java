package com.lody.virtual.client.stub;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.ipc.VActivityManager;

import java.net.URISyntaxException;

/**
 * @author Lody
 */
public class ShortcutHandleActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        int userId = intent.getIntExtra("_VA_|_user_id_", 0);
        String splashUri = intent.getStringExtra("_VA_|_splash_");
        String targetUri = intent.getStringExtra("_VA_|_uri_");
        Intent splashIntent = null;
        Intent targetIntent = null;
        if (splashUri != null) {
            try {
                splashIntent = Intent.parseUri(splashUri, 0);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if (targetUri != null) {
            try {
                targetIntent = Intent.parseUri(targetUri, 0);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if (targetIntent == null) {
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras != null) {
            Bundle targetBundle = new Bundle(extras);
            for (String key : extras.keySet()) {
                if (key.startsWith("_VA_")) {
                    targetBundle.remove(key);
                }
            }
            targetIntent.putExtras(targetBundle);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            targetIntent.setSelector(null);
        }

        if (splashIntent == null) {
            try {
                VActivityManager.get().startActivity(targetIntent, userId);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            splashIntent.putExtra(Constants.PASS_KEY_INTENT, targetIntent);
            splashIntent.putExtra(Constants.PASS_KEY_USER, userId);
            String pkg = targetIntent.getPackage();
            if (pkg == null) {
                ComponentName component = targetIntent.getComponent();
                if (component != null) {
                    pkg = component.getPackageName();
                }
            }
            splashIntent.putExtra(Constants.PASS_PKG_NAME_ARGUMENT, pkg);
            startActivity(splashIntent);
        }

    }

}
