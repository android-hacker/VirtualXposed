package com.lody.virtual.client.stub;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.lody.virtual.client.core.VirtualCore;

import java.net.URISyntaxException;

/**
 * @author Lody
 *
 */
public class ShortcutHandleActivity extends Activity {

	private static boolean needPreloadApp = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			if (needPreloadApp) {
				// Ensure the all apps loaded.
				VirtualCore.getCore().preloadAllApps();
				needPreloadApp = false;
			}
			Intent forwardIntent = getTargetIntent();
			if (forwardIntent != null) {
				forwardIntent.putExtras(intent);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
					forwardIntent.setSelector(null);
				}
				try {
					startActivity(forwardIntent);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				finish();
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
