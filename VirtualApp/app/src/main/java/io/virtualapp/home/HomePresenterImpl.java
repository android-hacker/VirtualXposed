package io.virtualapp.home;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.lody.virtual.client.core.VirtualCore;

import io.virtualapp.VCommends;
import io.virtualapp.home.models.AppModel;
import io.virtualapp.home.models.AppRepository;
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
        dataChanged();
        if (!Once.beenDone(VCommends.TAG_SHOW_ADD_APP_GUIDE)) {
            mView.showGuide();
            Once.markDone(VCommends.TAG_SHOW_ADD_APP_GUIDE);
        }
    }

    @Override
    public void launchApp(AppModel model, int userId) {
        try {
            LoadingActivity.launch(mActivity, model, userId);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dataChanged() {
        mView.showLoading();
        mRepo.getVirtualApps().done(mView::loadFinish).fail(mView::loadError);
    }


    @Override
    public void addApp(AppModel model) {
        if (model != null) {
            try {
                mRepo.addVirtualApp(model);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            mView.addAppToLauncher(model);
        }
    }

    @Override
    public void deleteApp(AppModel model) {
        if (model != null) {
            try {
                mRepo.removeVirtualApp(model);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            mView.removeAppToLauncher(model);
        }
    }

    @Override
    public void createShortcut(AppModel model) {
        boolean res = VirtualCore.get().createShortcut(0, model.packageName, new VirtualCore.OnEmitShortcutListener() {
            @Override
            public Bitmap getIcon(Bitmap originIcon) {
                return originIcon;
            }

            @Override
            public String getName(String originName) {
                return originName + "(VA)";
            }
        });
        Toast.makeText(mActivity, "Create shortcut " + (res ? "success!" : "failed!"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addNewApp() {
        ListAppActivity.gotoListApp(mActivity);
    }

}
