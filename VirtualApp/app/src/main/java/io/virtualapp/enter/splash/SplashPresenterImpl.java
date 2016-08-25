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
			long before = System.currentTimeMillis();
			// Do some thing
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
