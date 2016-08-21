package io.virtualapp.enter.splash;

import com.lody.virtual.client.core.VirtualCore;

import io.virtualapp.VApp;
import io.virtualapp.abs.ui.VUiKit;

/**
 * @author Lody
 */
/* package */ class SplashPresenterImpl implements SplashContract.SplashPresenter {

	private SplashContract.SplashView mView;

	public SplashPresenterImpl(SplashContract.SplashView view) {
		this.mView = view;
		mView.setPresenter(this);
	}

	@Override
	public void start() {
		mView.prepareLoading();
		mView.startLoading();
		VUiKit.defer().when(() -> {
			long before = System.currentTimeMillis();
			if (VApp.getApp().isNeedPreloadApps()) {
				VirtualCore.getCore().preloadAllApps();
			}
			long delta = System.currentTimeMillis() - before;
			if (delta < 500) {
				try {
					Thread.sleep(500 - delta);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).done((res) -> mView.finishLoading());
	}
}
