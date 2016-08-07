package io.virtualapp.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.ExtraConstants;
import com.lody.virtual.helper.proto.AppInfo;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.AppModel;

/**
 * @author Lody
 */

public class LoadingActivity extends AppCompatActivity {

	private static final String MODEL_ARGUMENT = "MODEL_ARGUMENT";

	private AppModel appModel;

	public static void launch(Context context, AppModel model) {
		Intent intent = VirtualCore.getCore().getLaunchIntent(model.packageName);
		if (intent != null) {
			Intent loadingPageIntent = new Intent(context, LoadingActivity.class);
			loadingPageIntent.putExtra(MODEL_ARGUMENT, model);
			loadingPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			loadingPageIntent.putExtra(ExtraConstants.EXTRA_INTENT, intent);
			context.startActivity(loadingPageIntent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);

		appModel = getIntent().getParcelableExtra(MODEL_ARGUMENT);
		assert (appModel != null);

		// TODO check if it is time costing...
		AppInfo appInfo = VirtualCore.getCore().findApp(appModel.packageName);
		if (appInfo != null) {
			appModel = new AppModel(this, appInfo);
		}

		ImageView iconView = (ImageView) findViewById(R.id.app_icon);
		if (iconView != null) {
			iconView.setImageDrawable(appModel.icon);
		}
		TextView nameView = (TextView) findViewById(R.id.app_name);
		if (nameView != null) {
			nameView.setText(appModel.name);
		}

		Intent intent = getIntent().getParcelableExtra(ExtraConstants.EXTRA_INTENT);
		VirtualCore.getCore().addLoadingPage(intent, this);
		if (intent != null) {
			VUiKit.postDelayed(500, () -> startActivity(intent));
		}
	}

}
