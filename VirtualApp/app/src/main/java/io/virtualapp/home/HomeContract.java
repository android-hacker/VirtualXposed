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

        void showBottomAction();

        void hideBottomAction();

		void showLoading();

		void hideLoading();

		void loadFinish(List<AppModel> appModels);

		void loadError(Throwable err);

		void showGuide();

		void addAppToLauncher(AppModel model);

        void showFab();

        void hideFab();

        void removeAppToLauncher(AppModel model);
    }

	/* package */ interface HomePresenter extends BasePresenter {

		void launchApp(AppModel model, int userId);

		void dataChanged();

		void addApp(AppModel model);

		void deleteApp(AppModel model);

        void createShortcut(AppModel model);

        void addNewApp();
    }

}
