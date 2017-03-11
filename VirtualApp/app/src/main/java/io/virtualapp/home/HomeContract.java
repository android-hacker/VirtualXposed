package io.virtualapp.home;


import java.util.List;

import io.virtualapp.abs.BasePresenter;
import io.virtualapp.abs.BaseView;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.AppInfoLite;

/**
 * @author Lody
 */
/* package */ class HomeContract {

	/* package */ interface HomeView extends BaseView<HomePresenter> {

        void showBottomAction();

        void hideBottomAction();

		void showLoading();

		void hideLoading();

		void loadFinish(List<AppData> appModels);

		void loadError(Throwable err);

		void showGuide();

		void addAppToLauncher(AppData model);

        void showFab();

        void hideFab();

        void removeAppToLauncher(AppData model);

		void refreshLauncherItem(AppData model);
	}

	/* package */ interface HomePresenter extends BasePresenter {

		void launchApp(AppData data);

		void dataChanged();

		void addApp(AppInfoLite info);

		void deleteApp(AppData model);

        void createShortcut(AppData model);
    }

}
