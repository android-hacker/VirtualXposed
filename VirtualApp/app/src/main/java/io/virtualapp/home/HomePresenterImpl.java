package io.virtualapp.home;

import android.app.Activity;
import android.graphics.Bitmap;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.AppSetting;

import org.jdeferred.DeferredManager;

import java.io.IOException;

import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.models.AppRepository;
import io.virtualapp.home.models.PackageAppData;
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
    public void launchApp(PackageAppData model, int userId) {
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
    public void addApp(PackageAppData data) {
        final VirtualCore core = VirtualCore.get();
        DeferredManager defer = VUiKit.defer();
        defer.when(() -> mRepo.addVirtualApp(data))
                .then((res) -> {
                    if (!res.isSuccess) {
                        throw new IllegalStateException();
                    }
                    AppSetting setting = core.findApp(data.packageName);
                    data.loadData(mActivity, setting.getApplicationInfo(VUserHandle.USER_OWNER));
                })
                .done(res -> {
                    data.isLoading = true;
                    mView.addAppToLauncher(data);
                    handleOptApp(data);
                });
    }

    private void handleOptApp(PackageAppData data) {
        VUiKit.defer().when(() -> {
            long time = System.currentTimeMillis();
            if (!data.fastOpen) {
                try {
                    VirtualCore.get().preOpt(data.packageName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            time = System.currentTimeMillis() - time;
            if (time < 1500L) {
                try {
                    Thread.sleep(1500L - time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).done((res) -> {
            data.isLoading = false;
            mView.refreshLauncherItem(data);
        });
    }

    @Override
    public void deleteApp(PackageAppData model) {
        try {
            mRepo.removeVirtualApp(model);
            mView.removeAppToLauncher(model);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createShortcut(PackageAppData model) {
        VirtualCore.get().createShortcut(0, model.packageName, new VirtualCore.OnEmitShortcutListener() {
            @Override
            public Bitmap getIcon(Bitmap originIcon) {
                return originIcon;
            }

            @Override
            public String getName(String originName) {
                return originName + "(VA)";
            }
        });
    }

    @Override
    public void addNewApp() {
        ListAppActivity.gotoListApp(mActivity);
    }
}
