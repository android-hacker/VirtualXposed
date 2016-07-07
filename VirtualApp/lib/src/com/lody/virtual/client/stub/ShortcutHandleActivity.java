package com.lody.virtual.client.stub;

import java.net.URISyntaxException;

import com.lody.virtual.helper.ExtraConstants;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * @author Lody
 *
 */
public class ShortcutHandleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			Intent forwardIntent = getTargetIntent();
			if (forwardIntent != null) {
				forwardIntent.putExtras(intent);
				if (Build.VERSION.SDK_INT >= 15) {
					forwardIntent.setSelector(null);
					try {
						startActivity(forwardIntent);
					} catch (Throwable e) {
						e.printStackTrace();
					}
					finish();
				}
			}
		}
	}

	private Intent getTargetIntent() {
		Intent intent = getIntent();
		try {
			if (intent != null) {
				Intent targetIntent = intent.getParcelableExtra(ExtraConstants.EXTRA_TARGET_INTENT);
				String targetUri = intent.getStringExtra(ExtraConstants.EXTRA_TARGET_URI);
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
