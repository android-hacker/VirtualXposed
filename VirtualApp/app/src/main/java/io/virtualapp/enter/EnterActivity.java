package io.virtualapp.enter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.enter.setup.SetupFragment;
import io.virtualapp.enter.splash.SplashFragment;
import jonathanfinerty.once.Once;

public class EnterActivity extends VActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		boolean enterGuide = !Once.beenDone(Once.THIS_APP_INSTALL, VCommends.TAG_NEW_VERSION);
		if (enterGuide) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enter);
		if (enterGuide) {
			enterGuideFragment();
		} else {
			enterSplashFragment();
		}
	}

	private void enterSplashFragment() {
		Fragment exist = findFragmentById(R.id.enter_layout_root);
		if (!(exist instanceof SplashFragment)) {
			SplashFragment splashFragment = SplashFragment.newInstance();
			replaceFragment(R.id.enter_layout_root, splashFragment);
		}
	}

	private void enterGuideFragment() {
		Fragment exist = findFragmentById(R.id.enter_layout_root);
		if (!(exist instanceof SetupFragment)) {
			SetupFragment guideFragment = SetupFragment.newInstance();
			replaceFragment(R.id.enter_layout_root, guideFragment);
		}
	}

}
