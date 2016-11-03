package io.virtualapp.home;

import android.app.Activity;

import io.virtualapp.VCommends;
import io.virtualapp.home.models.AppModel;
import io.virtualapp.home.models.AppRepository;
import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
class HomePresenterImpl implements HomeContract.HomePresenter {

	private HomeContract.HomeView mView;
	private Activity mActivity;
	private AppRepository mRepo;

	HomePresenterImpl(HomeContract.HomeView view, Activity activity) {
		mView = view;
		mActivity = activity;
		mRepo = new AppRepository(activity);
		mView.setPresenter(this);
	}

	@Override
	public void start() {
		dataChanged();
		if (!Once.beenDone(VCommends.TAG_SHOW_ADD_APP_GUIDE)) {
			mView.showGuide();
			Once.markDone(VCommends.TAG_SHOW_ADD_APP_GUIDE);
		}
	}

	@Override
	public void launchApp(AppModel model, int userId) {
		try {
			LoadingActivity.launch(mActivity, model, userId);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dataChanged() {
		mView.showLoading();
		mRepo.getVirtualApps().done(mView::loadFinish).fail(mView::loadError);
	}

	@Override
	public void dragChange(boolean isStart) {
		if (isStart) {
			mView.hideFab();
		} else {
			mView.showFab();
		}
	}

	@Override
	public void dragNearCrash(boolean intoCrash) {
		if (intoCrash) {
			mView.setCrashShadow(true);
		} else {
			mView.setCrashShadow(false);
		}
	}

	@Override
	public void addApp(AppModel model) {
		if (model != null) {
			try {
				mRepo.addVirtualApp(model);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			mView.addAppToLauncher(model);
		}
	}

	@Override
	public void deleteApp(AppModel model) {
		if (model != null) {
			try {
				mRepo.removeVirtualApp(model);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void wantAddApp() {
		ListAppActivity.gotoListApp(mActivity);
	}

}
