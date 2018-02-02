package io.virtualapp.home;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.GmsSupport;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.DeviceUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.VApp;
import io.virtualapp.VCommends;
import io.virtualapp.about.AboutActivity;
import io.virtualapp.abs.Function;
import io.virtualapp.abs.nestedadapter.SmartRecyclerAdapter;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.home.adapters.LaunchpadAdapter;
import io.virtualapp.home.adapters.decorations.ItemOffsetDecoration;
import io.virtualapp.home.models.AddAppButton;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.home.models.EmptyAppData;
import io.virtualapp.home.models.MultiplePackageAppData;
import io.virtualapp.home.models.PackageAppData;
import io.virtualapp.widgets.TwoGearsView;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_DRAG;
import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.END;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.START;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

/**
 * @author Lody
 */
public class HomeActivity extends VActivity implements HomeContract.HomeView {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private static final String SHOW_DOZE_ALERT_KEY = "SHOW_DOZE_ALERT_KEY";

    private HomeContract.HomePresenter mPresenter;
    private TwoGearsView mLoadingView;
    private RecyclerView mLauncherView;
    private View mMenuView;
    private PopupMenu mPopupMenu;
    private View mBottomArea;
    private View mLeftArea;
    private View mRightArea;
    private View mCreateShortcutBox;
    private View mClearAppBox;
    private TextView mClearAppTextView;
    private View mKillAppBox;
    private TextView mKillAppTextView;
    private TextView mCreateShortcutTextView;
    private View mDeleteAppBox;
    private TextView mDeleteAppTextView;
    private LaunchpadAdapter mLaunchpadAdapter;
    private Handler mUiHandler;

    //region ---------------package observer---------------
    private VirtualCore.PackageObserver mPackageObserver = new VirtualCore.PackageObserver() {
        @Override
        public void onPackageInstalled(String packageName) throws RemoteException {
            if (!isForground) {
                runOnUiThread(() -> mPresenter.dataChanged());
            }
        }

        @Override
        public void onPackageUninstalled(String packageName) throws RemoteException {
            if (!isForground) {
                runOnUiThread(() -> mPresenter.dataChanged());
            }
        }

        @Override
        public void onPackageInstalledAsUser(int userId, String packageName) throws RemoteException {
        }

        @Override
        public void onPackageUninstalledAsUser(int userId, String packageName) throws RemoteException {
        }
    };
    private boolean isForground = false;
    //endregion

    public static void goHome(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mUiHandler = new Handler(Looper.getMainLooper());
        bindViews();
        initLaunchpad();
        initMenu();
        new HomePresenterImpl(this).start();
        VirtualCore.get().registerObserver(mPackageObserver);
        alertForMeizu();
        alertForDoze();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForground = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForground = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VirtualCore.get().unregisterObserver(mPackageObserver);
    }

    private void initMenu() {
        mPopupMenu = new PopupMenu(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light), mMenuView);
        Menu menu = mPopupMenu.getMenu();
        setIconEnable(menu, true);

        menu.add(getResources().getString(R.string.menu_about)).setIcon(R.drawable.ic_settings).setOnMenuItemClickListener(item -> {
            startActivity(new Intent(HomeActivity.this, AboutActivity.class));
            return true;
        });

        menu.add(getResources().getString(R.string.menu_reboot)).setIcon(R.drawable.ic_reboot).setOnMenuItemClickListener(item -> {
            VirtualCore.get().killAllApps();
            showRebootTips();
            return true;
        });
        mMenuView.setOnClickListener(v -> mPopupMenu.show());
    }

    long lastClickRebootTime = 0;
    int continuousClickCount = 0;
    private void showRebootTips() {
        final long INTERVAL = 2000;
        long now = SystemClock.elapsedRealtime();
        if (now - lastClickRebootTime > INTERVAL) {
            Toast.makeText(this, R.string.reboot_tips_1, Toast.LENGTH_SHORT).show();
            // valid click, reset
            continuousClickCount = 0;
        } else {
            continuousClickCount++;
            switch (continuousClickCount) {
                case 1:
                    Toast.makeText(this, R.string.reboot_tips_2, Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(this, R.string.reboot_tips_3, Toast.LENGTH_SHORT).show();
                    mUiHandler.postDelayed(() -> Process.killProcess(Process.myPid()), 1000);
                    break;
                default:
                    Toast.makeText(this, R.string.reboot_tips_1, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        lastClickRebootTime = now;
    }

    private static void setIconEnable(Menu menu, boolean enable) {
        try {
            @SuppressLint("PrivateApi")
            Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            m.invoke(menu, enable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindViews() {
        mLoadingView = (TwoGearsView) findViewById(R.id.pb_loading_app);
        mLauncherView = (RecyclerView) findViewById(R.id.home_launcher);
        mMenuView = findViewById(R.id.home_menu);
        mBottomArea = findViewById(R.id.bottom_area);
        mLeftArea = findViewById(R.id.left_area);
        mRightArea = findViewById(R.id.right_area);
        mClearAppBox = findViewById(R.id.clear_app_area);
        mClearAppTextView = (TextView) findViewById(R.id.clear_app_text);
        mKillAppBox = findViewById(R.id.kill_app_area);
        mKillAppTextView = (TextView) findViewById(R.id.kill_app_text);
        mCreateShortcutBox = findViewById(R.id.create_shortcut_area);
        mCreateShortcutTextView = (TextView) findViewById(R.id.create_shortcut_text);
        mDeleteAppBox = findViewById(R.id.delete_app_area);
        mDeleteAppTextView = (TextView) findViewById(R.id.delete_app_text);
    }

    private void initLaunchpad() {
        mLauncherView.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        mLauncherView.setLayoutManager(layoutManager);
        mLaunchpadAdapter = new LaunchpadAdapter(this);
        SmartRecyclerAdapter wrap = new SmartRecyclerAdapter(mLaunchpadAdapter);
        View footer = new View(this);
        footer.setLayoutParams(new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, VUiKit.dpToPx(this, 60)));
        wrap.setFooterView(footer);
        mLauncherView.setAdapter(wrap);
        mLauncherView.addItemDecoration(new ItemOffsetDecoration(this, R.dimen.desktop_divider));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new LauncherTouchCallback());
        touchHelper.attachToRecyclerView(mLauncherView);
        mLaunchpadAdapter.setAppClickListener((pos, data) -> {
            if (!data.isLoading()) {
                if (data instanceof AddAppButton) {
                    onAddAppButtonClick();
                }
                mLaunchpadAdapter.notifyItemChanged(pos);
                mPresenter.launchApp(data);
            }
        });
    }

    private void onAddAppButtonClick() {
        ListAppActivity.gotoListApp(this);
    }

    private void deleteApp(int position) {
        List<AppData> mLaunchpadAdapterList = mLaunchpadAdapter.getList();
        if (position >= mLaunchpadAdapterList.size() || position < 0) {
            return;
        }
        AppData data = mLaunchpadAdapterList.get(position);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.home_menu_delete_title)
                .setMessage(getResources().getString(R.string.home_menu_delete_content, data.getName()))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    mPresenter.deleteApp(data);
                })
                .setNegativeButton(android.R.string.no, null)
                .create();
        try {
            alertDialog.show();
        } catch (Throwable ignored) {
            // BadTokenException.
        }
    }

    private void clearApp(int position) {
        List<AppData> mLaunchpadAdapterList = mLaunchpadAdapter.getList();
        if (position >= mLaunchpadAdapterList.size() || position < 0) {
            return;
        }
        AppData data = mLaunchpadAdapterList.get(position);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.home_menu_clear_title)
                .setMessage(getResources().getString(R.string.home_menu_clear_content, data.getName()))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    mPresenter.clearApp(data);
                })
                .setNegativeButton(android.R.string.no, null)
                .create();
        try {
            alertDialog.show();
        } catch (Throwable ignored) {
            // BadTokenException.
        }
    }

    private void killApp(int position) {
        List<AppData> mLaunchpadAdapterList = mLaunchpadAdapter.getList();
        if (position >= mLaunchpadAdapterList.size() || position < 0) {
            return;
        }
        AppData data = mLaunchpadAdapterList.get(position);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.home_menu_kill_title)
                .setMessage(getResources().getString(R.string.home_menu_kill_content, data.getName()))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    mPresenter.killApp(data);
                })
                .setNegativeButton(android.R.string.no, null)
                .create();
        try {
            alertDialog.show();
        } catch (Throwable ignored) {
            // BadTokenException.
        }
    }

    private void createShortcut(int position) {
        if (position < 0) {
            return;
        }
        AppData model = mLaunchpadAdapter.getList().get(position);
        if (model instanceof PackageAppData || model instanceof MultiplePackageAppData) {
            mPresenter.createShortcut(model);
        }
    }

    @Override
    public void setPresenter(HomeContract.HomePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showBottomAction() {
        mBottomArea.setTranslationY(mBottomArea.getHeight());
        mBottomArea.setVisibility(View.VISIBLE);
        mBottomArea.animate().translationY(0).setDuration(500L).start();
        mLeftArea.setTranslationX(-mLeftArea.getWidth());
        mLeftArea.setVisibility(View.VISIBLE);
        mLeftArea.animate().translationX(0).setDuration(500L).start();
        mRightArea.setTranslationX(mRightArea.getWidth());
        mRightArea.setVisibility(View.VISIBLE);
        mRightArea.animate().translationX(0).setDuration(500L).start();
    }

    @Override
    public void hideBottomAction() {
        mBottomArea.setTranslationY(0);

        class HideAnimatorListener implements Animator.AnimatorListener {

            View v;
            HideAnimatorListener(View v) {
                this.v = v;
            }

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }

        ObjectAnimator transAnim = ObjectAnimator.ofFloat(mBottomArea, "translationY", 0, mBottomArea.getHeight());
        transAnim.addListener(new HideAnimatorListener(mBottomArea));
        transAnim.setDuration(500L);
        transAnim.start();

        mLeftArea.setTranslationX(0);
        ObjectAnimator transAnimLeft = ObjectAnimator.ofFloat(mLeftArea, "translationX", 0, -mLeftArea.getWidth());
        transAnim.addListener(new HideAnimatorListener(mLeftArea));
        transAnimLeft.setDuration(500L);
        transAnimLeft.start();

        mRightArea.setTranslationX(0);
        ObjectAnimator transAnimRight = ObjectAnimator.ofFloat(mRightArea, "translationX", 0, mRightArea.getWidth());
        transAnim.addListener(new HideAnimatorListener(mRightArea));
        transAnimRight.setDuration(500L);
        transAnimRight.start();
    }

    @Override
    public void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingView.startAnim();
    }

    @Override
    public void hideLoading() {
        mLoadingView.setVisibility(View.GONE);
        mLoadingView.stopAnim();
    }

    @Override
    public void loadFinish(List<AppData> list) {
        list.add(new AddAppButton(this));
        mLaunchpadAdapter.setList(list);
        hideLoading();
    }

    @Override
    public void loadError(Throwable err) {
        err.printStackTrace();
        hideLoading();
    }

    @Override
    public void showGuide() {

    }

    @Override
    public void addAppToLauncher(AppData model) {
        List<AppData> dataList = mLaunchpadAdapter.getList();
        if (dataList == null) {
            return;
        }
        boolean replaced = false;
        for (int i = 0; i < dataList.size(); i++) {
            AppData data = dataList.get(i);
            if (data instanceof EmptyAppData) {
                mLaunchpadAdapter.replace(i, model);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            mLaunchpadAdapter.add(model);
            mLauncherView.smoothScrollToPosition(mLaunchpadAdapter.getItemCount() - 1);
        }
    }


    @Override
    public void removeAppToLauncher(AppData model) {
        mLaunchpadAdapter.remove(model);
    }

    @Override
    public void refreshLauncherItem(AppData model) {
        mLaunchpadAdapter.refresh(model);
    }

    @Override
    public void askInstallGms() {
        new AlertDialog.Builder(this)
                .setTitle("Hi")
                .setMessage("We found that your device has been installed the Google service, whether you need to install them?")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    defer().when(() -> {
                        GmsSupport.installGApps(0);
                    }).done((res) -> {
                        mPresenter.dataChanged();
                    });
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) ->
                        Toast.makeText(HomeActivity.this, "You can also find it in the Settings~", Toast.LENGTH_LONG).show())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            List<AppInfoLite> appList = data.getParcelableArrayListExtra(VCommends.EXTRA_APP_INFO_LIST);
            if (appList != null) {
                boolean showTip = false;
                for (AppInfoLite info : appList) {
                    if (new File(info.path).length() > 1024 * 1024 * 24) {
                        showTip = true;
                    }
                    mPresenter.addApp(info);
                }
                if (showTip) {
                    Toast.makeText(this, R.string.large_app_install_tips, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class LauncherTouchCallback extends ItemTouchHelper.SimpleCallback {

        int[] location = new int[2];
        boolean upAtDeleteAppArea;
        boolean upAtCreateShortcutArea;
        boolean upAtClearAppArea;
        boolean upAtKillAppArea;
        RecyclerView.ViewHolder dragHolder;

        LauncherTouchCallback() {
            super(UP | DOWN | LEFT | RIGHT | START | END, 0);
        }

        @Override
        public int interpolateOutOfBoundsScroll(RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
            return 0;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            try {
                AppData data = mLaunchpadAdapter.getList().get(viewHolder.getAdapterPosition());
                if (!data.canReorder()) {
                    return makeMovementFlags(0, 0);
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            return super.getMovementFlags(recyclerView, viewHolder);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int pos = viewHolder.getAdapterPosition();
            int targetPos = target.getAdapterPosition();
            mLaunchpadAdapter.moveItem(pos, targetPos);
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (viewHolder instanceof LaunchpadAdapter.ViewHolder) {
                if (actionState == ACTION_STATE_DRAG) {
                    if (dragHolder != viewHolder) {
                        dragHolder = viewHolder;
                        viewHolder.itemView.setScaleX(1.2f);
                        viewHolder.itemView.setScaleY(1.2f);
                        if (mBottomArea.getVisibility() == View.GONE) {
                            showBottomAction();
                        }
                    }
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
            if (upAtCreateShortcutArea || upAtDeleteAppArea || upAtClearAppArea || upAtKillAppArea) {
                return false;
            }
            try {
                AppData data = mLaunchpadAdapter.getList().get(target.getAdapterPosition());
                if (data != null) {
                    return data.canReorder();
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder instanceof LaunchpadAdapter.ViewHolder) {
                LaunchpadAdapter.ViewHolder holder = (LaunchpadAdapter.ViewHolder) viewHolder;
                viewHolder.itemView.setScaleX(1f);
                viewHolder.itemView.setScaleY(1f);
                viewHolder.itemView.setBackgroundColor(holder.color);
            }
            super.clearView(recyclerView, viewHolder);
            if (dragHolder == viewHolder) {
                if (mBottomArea.getVisibility() == View.VISIBLE) {
                    mUiHandler.postDelayed(HomeActivity.this::hideBottomAction, 200L);
                    if (upAtCreateShortcutArea) {
                        createShortcut(viewHolder.getAdapterPosition());
                    } else if (upAtDeleteAppArea) {
                        deleteApp(viewHolder.getAdapterPosition());
                    } else if (upAtClearAppArea) {
                        clearApp(viewHolder.getAdapterPosition());
                    } else if (upAtKillAppArea) {
                        killApp(viewHolder.getAdapterPosition());
                    }
                }
                dragHolder = null;
            }
        }


        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (actionState != ACTION_STATE_DRAG || !isCurrentlyActive) {
                return;
            }
            View itemView = viewHolder.itemView;
            itemView.getLocationInWindow(location);
            int x = (int) (location[0] + dX);
            int y = (int) (location[1] + dY);

            mBottomArea.getLocationInWindow(location);
            // int baseLine = location[1] - mBottomArea.getHeight();
            int baseLine = location[1]; // shorten area
            if (y >= baseLine) {
                mDeleteAppBox.getLocationInWindow(location);
                int deleteAppAreaStartX = location[0];
                if (x < deleteAppAreaStartX) {
                    setMenuView(true, false, false, false);
                    return;
                } else {
                    setMenuView( false, true, false, false);
                    return;
                }
            }

            mLeftArea.getLocationInWindow(location);
            if (x <= location[0]) {
                setMenuView(false, false, true, false);
                return;
            }

            mRightArea.getLocationInWindow(location);
            if (x >= location[0]) {
                setMenuView(false, false, false, true);
                return;
            }

            setMenuView( false, false, false, false);
        }

        private void setMenuView(boolean showCreateShortcut, boolean showDelete, boolean showClear, boolean showStop) {
            upAtDeleteAppArea = showDelete;
            upAtCreateShortcutArea = showCreateShortcut;
            upAtKillAppArea = showStop;
            upAtClearAppArea = showClear;
            int color = Color.parseColor("#0099cc");
            Function<Boolean, Integer> getColor = r -> r ? color : Color.WHITE;
            mKillAppTextView.setTextColor(getColor.apply(showStop));
            mCreateShortcutTextView.setTextColor(getColor.apply(showCreateShortcut));
            mDeleteAppTextView.setTextColor(getColor.apply(showDelete));
            mClearAppTextView.setTextColor(getColor.apply(showClear));
        }
    }

    private void alertForMeizu() {
        if (!DeviceUtil.isMeizuBelowN()) {
            return;
        }
        boolean isXposedInstalled = VirtualCore.get().isAppInstalled(VApp.XPOSED_INSTALLER_PACKAGE);
        if (isXposedInstalled) {
            return;
        }
        mUiHandler.postDelayed(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.meizu_device_tips_title)
                    .setMessage(R.string.meizu_device_tips_content)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    })
                    .create();
            try {
                alertDialog.show();
            } catch (Throwable ignored) {}
        }, 2000);
    }

    private void alertForDoze() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager == null) {
            return;
        }
        boolean showAlert = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SHOW_DOZE_ALERT_KEY, true);
        if (!showAlert) {
            return;
        }
        String packageName = getPackageName();
        boolean ignoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName);
        if (!ignoringBatteryOptimizations) {

            mUiHandler.postDelayed(() -> {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.alert_for_doze_mode_title)
                        .setMessage(R.string.alert_for_doze_mode_content)
                        .setPositiveButton(R.string.alert_for_doze_mode_yes, (dialog, which) -> {
                            try {
                                startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName())));
                            } catch (ActivityNotFoundException ignored) {
                                // ActivityNotFoundException on some devices.
                                try {
                                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                                } catch (Throwable e) {
                                    PreferenceManager.getDefaultSharedPreferences(HomeActivity.this)
                                            .edit().putBoolean(SHOW_DOZE_ALERT_KEY, false).apply();
                                }
                            } catch (Throwable e) {
                                PreferenceManager.getDefaultSharedPreferences(HomeActivity.this)
                                        .edit().putBoolean(SHOW_DOZE_ALERT_KEY, false).apply();
                            }
                        })
                        .setNegativeButton(R.string.alert_for_doze_mode_no, (dialog, which) ->
                                PreferenceManager.getDefaultSharedPreferences(HomeActivity.this)
                                .edit().putBoolean(SHOW_DOZE_ALERT_KEY, false).apply())
                        .create();
                try {
                    alertDialog.show();
                } catch (Throwable ignored) {}
            }, 3000);
        }
    }
}
