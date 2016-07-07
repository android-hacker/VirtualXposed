package io.virtualapp.home;

import android.app.Activity;
import android.content.Intent;

import io.virtualapp.VCommends;
import io.virtualapp.home.models.AppDataSource;
import io.virtualapp.home.models.AppModel;
import io.virtualapp.home.models.AppRepository;

/**
 * @author Lody
 */
public class ListAppPresenterImpl implements ListAppContract.ListAppPresenter {

    private Activity mActivity;
    private ListAppContract.ListAppView mView;
    private AppDataSource mRepository;

    public ListAppPresenterImpl(Activity activity, ListAppContract.ListAppView view) {
        mActivity = activity;
        mView = view;
        mRepository = new AppRepository(activity);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        mView.setPresenter(this);
        mView.startLoading();
        mRepository
                .getInstalledApps(mActivity)
                .done(mView::loadFinish);
    }

    @Override
    public void selectApp(AppModel model) {
        Intent data = new Intent();
        data.putExtra(VCommends.EXTRA_APP_MODEL, model);
        mActivity.setResult(Activity.RESULT_OK, data);
        mActivity.finish();
    }
}
