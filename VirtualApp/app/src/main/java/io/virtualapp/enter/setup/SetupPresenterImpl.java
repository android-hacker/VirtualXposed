package io.virtualapp.enter.setup;

import static io.virtualapp.enter.setup.SetupContract.SetupView;

import android.content.Context;

import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.HomeActivity;
import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
/* package */ class SetupPresenterImpl implements SetupContract.SetupPresenter {

	private SetupView mView;
	private Context mContext;

	SetupPresenterImpl(SetupView view, Context context) {
		this.mView = view;
		this.mContext = context;
		mView.setPresenter(this);
	}

	@Override
	public void start() {
		mView.hideIcon();
		mView.hideGuideBody();
		// Animation should not be shown immediately.
		VUiKit.postDelayed(200, () -> mView.showAnim());
	}

	@Override
	public void setupComplete() {
		HomeActivity.goHome(mContext);
		mView.getActivity().finish();
		Once.markDone(VCommends.TAG_NEW_VERSION);
	}
}
