package io.virtualapp.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppInfo;
import com.melnykov.fab.FloatingActionButton;

import java.util.List;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.effects.ExplosionField;
import io.virtualapp.home.adapters.LaunchpadAdapter;
import io.virtualapp.home.models.AppModel;
import io.virtualapp.widgets.PagerView;
import io.virtualapp.widgets.showcase.MaterialShowcaseView;

/**
 * @author Lody
 */
public class HomeActivity extends VActivity implements HomeContract.HomeView  {

    private HomeContract.HomePresenter mPresenter;

    private ProgressDialog mOpeningAppDialog;
    private ProgressBar mLoadingBar;
    private PagerView mPagerView;
    private FloatingActionButton mAppFab;
    private FloatingActionButton mCrashFab;

    private ExplosionField mExplosionField;

    private LaunchpadAdapter mAdapter;


    public static void goHome(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_app);
        mPagerView = (PagerView) findViewById(R.id.home_launcher);
        mAppFab = (FloatingActionButton) findViewById(R.id.home_fab);
        mCrashFab = (FloatingActionButton) findViewById(R.id.home_del);
        mAdapter = new LaunchpadAdapter(this);
        mPagerView.setAdapter(mAdapter);
        new HomePresenterImpl(this, this);
        mPresenter.start();
        mAppFab.setOnClickListener(v -> mPresenter.wantAddApp());
        mExplosionField = ExplosionField.attachToWindow(this);
        mPagerView.setOnDragChangeListener(mPresenter::dragChange);
        mPagerView.setOnEnterCrashListener(mPresenter::dragNearCrash);
        mPagerView.setOnCrashItemListener((position, consumer) -> {
            AppModel model = mAdapter.getItem(position);
            View v = mPagerView.getChildAt(position);
            mExplosionField.explode(v, view -> consumer.moveToCrash());
            mPresenter.deleteApp(model);
        });
        mPagerView.setOnItemClickListener((item, pos) -> {
            AppModel model = (AppModel) item;
            mPresenter.launchApp(model);
        });
        mCrashFab.post(() -> {
            int[] location = new int[2];
            mAppFab.getLocationInWindow(location);
            mPagerView.setBottomLine(location[1]);
        });
    }


    @Override
    public void setPresenter(HomeContract.HomePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {
        mLoadingBar.setVisibility(View.VISIBLE);
        mPagerView.setVisibility(View.GONE);
    }

    @Override
    public void loadFinish(List<AppModel> appModels) {
        mAdapter.setModels(appModels);
        mPagerView.refreshView();
        hideLoading();
    }

    @Override
    public void loadError(Throwable err) {
        hideLoading();
    }

    @Override
    public void showGuide() {
        new MaterialShowcaseView.Builder(this)
                .setTarget(mAppFab)
                .setDelay(700)
                .setContentText("Click this button to add an App ~")
                .setDismissText("Got it")
                .setDismissTextColor(Color.parseColor("#03a9f4"))
                .show();
    }

    @Override
    public void showFab() {
        mAppFab.show();
        mCrashFab.hide();
    }

    @Override
    public void hideFab() {
        mAppFab.hide();
        mCrashFab.setVisibility(View.VISIBLE);
        mCrashFab.show();
    }

    @Override
    public void setCrashShadow(boolean isShow) {
        mCrashFab.setShadow(isShow);
    }

    @Override
    public void waitingAppOpen() {
        mOpeningAppDialog = ProgressDialog.show(this, "Please wait", "Opening the app...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mOpeningAppDialog != null && mOpeningAppDialog.isShowing()) {
            mOpeningAppDialog.dismiss();
            mOpeningAppDialog = null;
        }
    }

    @Override
    public void refreshPagerView() {
        mPagerView.refreshView();
    }

    @Override
    public void addAppToLauncher(AppModel model) {
        mAdapter.add(model);
        mPagerView.itemAdded();
    }

    private void hideLoading() {
        mLoadingBar.setVisibility(View.GONE);
        mPagerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK
                && requestCode == VCommends.REQUEST_SELECT_APP
                && data != null) {
            AppModel model = data.getParcelableExtra(VCommends.EXTRA_APP_MODEL);
            mPresenter.addApp(model);
            AppInfo info = VirtualCore.getCore().findApp(model.packageName);
            if (info != null) {
                model.context = this;
                ProgressDialog dialog = ProgressDialog.show(this, "Please wait", "Optimizing new Virtual App...");
                VUiKit.defer().when(() -> {
                    try {
                        model.loadData(info.applicationInfo);
                        VirtualCore.getCore().preOpt(info.packageName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).done((res) -> {
                    dialog.dismiss();
                    mPresenter.dataChanged();
                });
            }
        }
    }
}
