package io.virtualapp.enter.setup;

import io.virtualapp.abs.BasePresenter;
import io.virtualapp.abs.BaseView;

/**
 * @author Lody
 */
/*package*/ class SetupContract {

    /*package*/ interface SetupView extends BaseView<SetupPresenter> {
        void hideIcon();

        void hideGuideBody();

        void showAnim();
    }

    /*package*/ interface SetupPresenter extends BasePresenter {
        void setupComplete();
    }
}
