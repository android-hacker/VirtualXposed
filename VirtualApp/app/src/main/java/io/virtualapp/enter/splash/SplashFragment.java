package io.virtualapp.enter.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VFragment;
import io.virtualapp.home.HomeActivity;

import static io.virtualapp.enter.splash.SplashContract.SplashView;


/**
 * @author Lody
 */
public class SplashFragment extends VFragment<SplashContract.SplashPresenter> implements SplashView {

    private View mLoadingBodyLayout;

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mLoadingBodyLayout = view.findViewById(R.id.splash_loading_body);
        new SplashPresenterImpl(this);
        mPresenter.start();
    }

    @Override
    public void prepareLoading() {
        mLoadingBodyLayout.setAlpha(0f);
        mLoadingBodyLayout.setScaleX(0f);
        mLoadingBodyLayout.setScaleY(0f);
    }

    @Override
    public void startLoading() {
        mLoadingBodyLayout.animate()
                .alpha(1f)
                .scaleX(1f).scaleY(1f)
                .start();
    }

    @Override
    public void finishLoading() {
        mLoadingBodyLayout.animate()
                .alpha(0f)
                .scaleX(0f).scaleY(0f)
                .withEndAction(() -> {
                    HomeActivity.goHome(getActivity());
                    finishActivity();
                })
                .start();
    }
}
