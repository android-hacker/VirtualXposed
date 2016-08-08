package io.virtualapp.home;

import java.util.List;

import io.virtualapp.abs.BasePresenter;
import io.virtualapp.abs.BaseView;
import io.virtualapp.home.models.AppModel;

/**
 * @author Lody
 * @version 1.0
 */
public class ListAppContract {
	public static final int SELECT_APP_FROM_SYSTEM = 0;
	public static final int SELECT_APP_FROM_SD_CARD = 1;

	interface ListAppView extends BaseView<ListAppPresenter> {

		void startLoading();

		void loadFinish(List<AppModel> models);
	}

	interface ListAppPresenter extends BasePresenter {
		void selectApp(AppModel model);
	}
}
