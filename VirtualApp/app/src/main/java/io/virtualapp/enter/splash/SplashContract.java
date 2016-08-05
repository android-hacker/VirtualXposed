package io.virtualapp.enter.splash;

import io.virtualapp.abs.BasePresenter;
import io.virtualapp.abs.BaseView;

/**
 * @author Lody
 */
/* package */ class SplashContract {

	/* package */ interface SplashView extends BaseView<SplashPresenter> {
		void prepareLoading();

		void startLoading();

		void finishLoading();
	}

	/* package */ interface SplashPresenter extends BasePresenter {
		// Empty yet
	}
}
