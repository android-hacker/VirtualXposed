package io.virtualapp.home;

import java.util.List;

import io.virtualapp.abs.BasePresenter;
import io.virtualapp.abs.BaseView;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.PackageAppData;

/**
 * @author Lody
 * @version 1.0
 */
public class ListAppContract {
	interface ListAppView extends BaseView<ListAppPresenter> {

		void startLoading();

		void loadFinish(List<AppData> models);
	}

	interface ListAppPresenter extends BasePresenter {
		void selectApp(PackageAppData model);
	}
}
