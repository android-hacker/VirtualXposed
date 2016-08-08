package io.virtualapp.enter.splash;

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
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).done((res) -> mView.finishLoading());
	}
}
