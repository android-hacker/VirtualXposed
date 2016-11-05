package io.virtualapp.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.helper.proto.AppSetting;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.AppModel;

/**
 * @author Lody
 */

public class LoadingActivity extends AppCompatActivity {

	private static final String MODEL_ARGUMENT = "MODEL_ARGUMENT";
	private static final String KEY_INTENT = "KEY_INTENT";
	private static final String KEY_USER = "KEY_USER";
	private AppModel appModel;

	public static void launch(Context context, AppModel model, int userId) {
		Intent intent = VirtualCore.get().getLaunchIntent(model.packageName, userId);
		if (intent != null) {
			Intent loadingPageIntent = new Intent(context, LoadingActivity.class);
			loadingPageIntent.putExtra(MODEL_ARGUMENT, model);
			loadingPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			loadingPageIntent.putExtra(KEY_INTENT, intent);
			loadingPageIntent.putExtra(KEY_USER, userId);
			context.startActivity(loadingPageIntent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);

		appModel = getIntent().getParcelableExtra(MODEL_ARGUMENT);
		int userId = getIntent().getIntExtra(KEY_USER, -1);

		VUiKit.defer().when(() -> {
			AppSetting appSetting = VirtualCore.get().findApp(appModel.packageName);
			if (appSetting != null) {
				appModel = new AppModel(this, appSetting);
			}
		}).done((res) -> {
			ImageView iconView = (ImageView) findViewById(R.id.app_icon);
			if (iconView != null) {
				iconView.setImageDrawable(appModel.icon);
			}
		});

		TextView nameView = (TextView) findViewById(R.id.app_name);
		if (nameView != null) {
			nameView.setText(appModel.name);
		}

		Intent intent = getIntent().getParcelableExtra(KEY_INTENT);
		VirtualCore.get().setLoadingPage(intent, this);
		if (intent != null) {
			VUiKit.defer().when(() -> {
				long startTime = System.currentTimeMillis();
				if (!appModel.fastOpen) {
					try {
						VirtualCore.get().preOpt(appModel.packageName);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				long spend = System.currentTimeMillis() - startTime;
				if (spend < 500) {
					try {
						Thread.sleep(500 - spend);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).done((res) ->
					VActivityManager.get().startActivity(intent, userId));
		}
	}

}
