package io.virtualapp.home;

import android.app.Activity;
import android.graphics.Bitmap;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.AppSetting;
import com.lody.virtual.remote.InstallResult;

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
    public void addApp(PackageAppData model) {
        final VirtualCore core = VirtualCore.get();
        InstallResult result = mRepo.addVirtualApp(model);
        if (result.isSuccess) {
            VUiKit.defer().when(() -> {
                AppSetting setting = core.findApp(model.packageName);
                model.loadData(mActivity, setting.getApplicationInfo(VUserHandle.USER_OWNER));
            }).done(res -> {
                model.isLoading = true;
                mView.addAppToLauncher(model);
                VUiKit.defer().when(() -> {
                    long time = System.currentTimeMillis();
                    if (!model.fastOpen) {
                        try {
                            core.preOpt(model.packageName);
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
                }).done((res_opt) -> {
                    model.isLoading = false;
                    mView.refreshLauncherItem(model);
                });
            });
        }
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
