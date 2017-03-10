package io.virtualapp.home;


import java.util.List;

import io.virtualapp.abs.BasePresenter;
import io.virtualapp.abs.BaseView;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.home.models.PackageAppData;

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

		void addAppToLauncher(PackageAppData model);

        void showFab();

        void hideFab();

        void removeAppToLauncher(PackageAppData model);

		void refreshLauncherItem(PackageAppData model);
	}

	/* package */ interface HomePresenter extends BasePresenter {

		void launchApp(PackageAppData model, int userId);

		void dataChanged();

		void addApp(AppInfoLite info);

		void deleteApp(PackageAppData model);

        void createShortcut(PackageAppData model);
    }

}
