package io.virtualapp.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;

import java.util.Locale;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.PackageAppData;
import io.virtualapp.home.repo.PackageAppDataStorage;
import io.virtualapp.widgets.EatBeansView;

/**
 * @author Lody
 */

public class LoadingActivity extends AppCompatActivity {

    private static final String PKG_NAME_ARGUMENT = "MODEL_ARGUMENT";
    private static final String KEY_INTENT = "KEY_INTENT";
    private static final String KEY_USER = "KEY_USER";
    private PackageAppData appModel;
    private EatBeansView loadingView;

    public static void launch(Context context, String packageName, int userId) {
        Intent intent = VirtualCore.get().getLaunchIntent(packageName, userId);
        if (intent != null) {
            Intent loadingPageIntent = new Intent(context, LoadingActivity.class);
            loadingPageIntent.putExtra(PKG_NAME_ARGUMENT, packageName);
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
        loadingView = (EatBeansView) findViewById(R.id.loading_anim);
        int userId = getIntent().getIntExtra(KEY_USER, -1);
        String pkg = getIntent().getStringExtra(PKG_NAME_ARGUMENT);
        appModel = PackageAppDataStorage.get().acquire(pkg);
        ImageView iconView = (ImageView) findViewById(R.id.app_icon);
        iconView.setImageDrawable(appModel.icon);
        TextView nameView = (TextView) findViewById(R.id.app_name);
        nameView.setText(String.format(Locale.ENGLISH, "Opening %s...", appModel.name));

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

    @Override
    protected void onResume() {
        super.onResume();
        loadingView.startAnim();
    }

    @Override
    protected void onPause() {
        super.onPause();
        loadingView.stopAnim();
    }
}
