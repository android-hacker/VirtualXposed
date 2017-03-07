package io.virtualapp.home;

import android.app.Activity;
import android.content.Intent;

import java.io.File;

import io.virtualapp.VCommends;
import io.virtualapp.home.models.AppDataSource;
import io.virtualapp.home.models.PackageAppData;
import io.virtualapp.home.models.AppRepository;

/**
 * @author Lody
 */
class ListAppPresenterImpl implements ListAppContract.ListAppPresenter {

	private Activity mActivity;
	private ListAppContract.ListAppView mView;
	private AppDataSource mRepository;

	private File from;

	ListAppPresenterImpl(Activity activity, ListAppContract.ListAppView view, File fromWhere) {
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
		if (from == null)
			mRepository.getInstalledApps(mActivity).done(mView::loadFinish);
		else
			mRepository.getStorageApps(mActivity, from).done(mView::loadFinish);
	}

	@Override
	public void selectApp(PackageAppData model) {
		Intent data = new Intent();
		data.putExtra(VCommends.EXTRA_APP_MODEL, model);
		mActivity.setResult(Activity.RESULT_OK, data);
		mActivity.finish();
	}
}
