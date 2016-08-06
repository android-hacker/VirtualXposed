package io.virtualapp.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.ExtraConstants;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;

/**
 * @author Lody
 */

public class LoadingActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		Intent intent = getIntent().getParcelableExtra(ExtraConstants.EXTRA_INTENT);
		VirtualCore.getCore().addLoadingPage(intent, this);
		if (intent != null) {
			VUiKit.postDelayed(500, () -> startActivity(intent));
		}
	}

}
