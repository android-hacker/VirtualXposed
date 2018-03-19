package io.virtualapp.home;

import android.app.Activity;

import io.virtualapp.VCommends;
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.home.repo.AppRepository;
import io.virtualapp.sys.Installd;
import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
class HomePresenterImpl implements HomeContract.HomePresenter {

    private HomeContract.HomeView mView;
    private Activity mActivity;
    private AppRepository mRepo;

    HomePresenterImpl(HomeContract.HomeView view) {
        mView = view;
        mActivity = view.getActivity();
        mRepo = new AppRepository(mActivity);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        if (!Once.beenDone(VCommends.TAG_SHOW_ADD_APP_GUIDE)) {
            mView.showGuide();
            Once.markDone(VCommends.TAG_SHOW_ADD_APP_GUIDE);
        }
        /*
        Delete GMS Support, becuase it may conflict with xposed.
        if (!Once.beenDone(VCommends.TAG_ASK_INSTALL_GMS) && GmsSupport.isOutsideGoogleFrameworkExist()) {
            mView.askInstallGms();
            Once.markDone(VCommends.TAG_ASK_INSTALL_GMS);
        }*/
    }

    @Override
    public void addApp(AppInfoLite info) {
        Installd.addApp(info, model -> mView.refreshLauncherItem(model));
    }
}
