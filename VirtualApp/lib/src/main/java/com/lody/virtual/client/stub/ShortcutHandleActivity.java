package com.lody.virtual.client.stub;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.lody.virtual.client.ipc.VActivityManager;

import java.net.URISyntaxException;

/**
 * @author Lody
 *
 */
public class ShortcutHandleActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		finish();
		Intent intent = getIntent();
		if (intent != null) {
			int userId = intent.getIntExtra("_VA_|_user_id_", 0);
			Intent forwardIntent = getTargetIntent();
			if (forwardIntent != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
					forwardIntent.setSelector(null);
				}
				try {
					VActivityManager.get().startActivity(forwardIntent, userId);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Intent getTargetIntent() {
		Intent intent = getIntent();
		try {
			if (intent != null) {
				Intent targetIntent = intent.getParcelableExtra("_VA_|_intent_");
				String targetUri = intent.getStringExtra("_VA_|_uri_");
				if (targetUri != null) {
					try {
						return Intent.parseUri(targetUri, 0);
					} catch (URISyntaxException e) {
						// Ignore
					}
				} else if (targetIntent != null) {
					return targetIntent;
				}
			}
		} catch (Exception e) {
			// Ignore
		}
		return null;
	}
}
