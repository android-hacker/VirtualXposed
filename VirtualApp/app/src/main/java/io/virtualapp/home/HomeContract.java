package io.virtualapp.home;

import java.util.List;

import io.virtualapp.abs.BasePresenter;
import io.virtualapp.abs.BaseView;
import io.virtualapp.home.models.AppModel;

/**
 * @author Lody
 */
/* package */ class HomeContract {

	/* package */ interface HomeView extends BaseView<HomePresenter> {

		void showLoading();

		void loadFinish(List<AppModel> appModels);

		void loadError(Throwable err);

		void showGuide();

		void showFab();

		void hideFab();

		void setCrashShadow(boolean show);

		void waitingAppOpen();

		void refreshPagerView();

		void addAppToLauncher(AppModel model);
	}

	/* package */ interface HomePresenter extends BasePresenter {
		void launchApp(AppModel model);

		void dataChanged();

		void dragChange(boolean isStart);

		void dragNearCrash(boolean canDel);

		void addApp(AppModel model);

		void deleteApp(AppModel model);

		void wantAddApp();
	}

}
