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

	private int from;

	public ListAppPresenterImpl(Activity activity, ListAppContract.ListAppView view, int fromWhere) {
		mActivity = activity;
		mView = view;
		mRepository = new AppRepository(activity);
		mView.setPresenter(this);
		this.from = fromWhere;
	}

	@Override
	public void start() {
		mView.setPresenter(this);
		mView.startLoading();
		if (from == ListAppContract.SELECT_APP_FROM_SYSTEM)
			mRepository.getInstalledApps(mActivity).done(mView::loadFinish);
		else
			mRepository.getSdCardApps(mActivity).done(mView::loadFinish);
	}

	@Override
	public void selectApp(AppModel model) {
		Intent data = new Intent();
		data.putExtra(VCommends.EXTRA_APP_MODEL, model);
		mActivity.setResult(Activity.RESULT_OK, data);
		mActivity.finish();
	}
}
