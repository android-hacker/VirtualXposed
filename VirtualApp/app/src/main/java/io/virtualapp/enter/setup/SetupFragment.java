package io.virtualapp.enter.setup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VFragment;

import static io.virtualapp.enter.setup.SetupContract.SetupView;

/**
 * @author Lody
 */
public class SetupFragment extends VFragment<SetupContract.SetupPresenter> implements SetupView {

    private ImageView mIconView;
    private View mGuideBodyLayout;

    public static SetupFragment newInstance() {
        return new SetupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mIconView = (ImageView) view.findViewById(R.id.setup_ic);
        mGuideBodyLayout = view.findViewById(R.id.setup_layout_body);
        new SetupPresenterImpl(this, view.getContext());
        mPresenter.start();
        view.findViewById(R.id.btn_go_home).setOnClickListener(v -> {
                    mPresenter.setupComplete();
        }
        );
    }


    @Override
    public void hideIcon() {
        mIconView.setAlpha(0f);
        mIconView.setScaleX(0.8f);
        mIconView.setScaleY(0.8f);
    }

    @Override
    public void hideGuideBody() {
        mGuideBodyLayout.setScaleX(0f);
        mGuideBodyLayout.setScaleY(0f);
    }

    @Override
    public void showAnim() {
        mIconView.animate()
                .alpha(1.0f)
                .scaleX(1.2f).scaleY(1.2f)
                .withEndAction(() ->
                        mIconView.animate()
                                .scaleX(1.0f).scaleY(1.0f)
                                .withEndAction(() ->
                                        mGuideBodyLayout.animate()
                                                .scaleX(1.0f).scaleY(1.0f)
                                                .start())
                )
                .start();
    }
}
