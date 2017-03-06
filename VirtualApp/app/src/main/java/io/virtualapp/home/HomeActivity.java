package io.virtualapp.home;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;

import java.util.List;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.home.adapters.LaunchpadAdapter;
import io.virtualapp.home.adapters.decorations.ItemOffsetDecoration;
import io.virtualapp.home.models.AppModel;
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

    private HomeContract.HomePresenter mPresenter;
    private TwoGearsView mLoadingView;
    private RecyclerView mLauncherView;
    private FloatingActionButton mFloatingButton;
    private View bottomArea;
    private View createShortcutArea;
    private View deleteAppArea;
    private LaunchpadAdapter mLaunchpadAdapter;
    private Handler mUiHandler;


    public static void goHome(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mUiHandler = new Handler(Looper.getMainLooper());
        bindViews();
        initLaunchpad();
        initFab();
        new HomePresenterImpl(this).start();
    }

    private void bindViews() {
        mLoadingView = (TwoGearsView) findViewById(R.id.pb_loading_app);
        mLauncherView = (RecyclerView) findViewById(R.id.home_launcher);
        mFloatingButton = (FloatingActionButton) findViewById(R.id.home_fab);
        bottomArea = findViewById(R.id.bottom_area);
        createShortcutArea = findViewById(R.id.create_shortcut_area);
        deleteAppArea = findViewById(R.id.delete_app_area);
    }

    private void initLaunchpad() {
        mLauncherView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mLauncherView.setLayoutManager(layoutManager);
        mLaunchpadAdapter = new LaunchpadAdapter(this);
        mLauncherView.setAdapter(mLaunchpadAdapter);
        mLauncherView.addItemDecoration(new ItemOffsetDecoration(this, R.dimen.desktop_divider));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new LauncherTouchCallback());
        touchHelper.attachToRecyclerView(mLauncherView);
        mLaunchpadAdapter.setAppClickListener(model -> mPresenter.launchApp(model, 0)
        );
    }

    private void initFab() {
        mFloatingButton.setOnClickListener(v -> mPresenter.addNewApp());
    }

    private void deleteApp(int position) {
        AppModel model = mLaunchpadAdapter.getList().get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete app")
                .setMessage("Do you want to delete " + model.name + "?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    mPresenter.deleteApp(model);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void createShortcut(int position) {
        AppModel model = mLaunchpadAdapter.getList().get(position);
        mPresenter.createShortcut(model);
    }

    @Override
    public void setPresenter(HomeContract.HomePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showBottomAction() {
        hideFab();
        bottomArea.setTranslationY(bottomArea.getHeight());
        bottomArea.setVisibility(View.VISIBLE);
        bottomArea.animate().translationY(0).setDuration(500L).start();
    }

    @Override
    public void hideBottomAction() {
        bottomArea.setTranslationY(0);
        ObjectAnimator transAnim = ObjectAnimator.ofFloat(bottomArea, "translationY", 0, bottomArea.getHeight());
        transAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                bottomArea.setVisibility(View.GONE);
                showFab();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                bottomArea.setVisibility(View.GONE);
                showFab();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        transAnim.setDuration(500L);
        transAnim.start();
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
    public void loadFinish(List<AppModel> list) {
        mLaunchpadAdapter.setList(list);
        hideLoading();
    }

    @Override
    public void loadError(Throwable err) {
        hideLoading();
    }

    @Override
    public void showGuide() {

    }

    @Override
    public void addAppToLauncher(AppModel model) {
        mLaunchpadAdapter.add(model);
    }

    @Override
    public void removeAppToLauncher(AppModel model) {
        mLaunchpadAdapter.remove(model);
    }

    @Override
    public void showFab() {
        mFloatingButton.show();
    }

    @Override
    public void hideFab() {
        mFloatingButton.hide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // TODO
    }

    private class LauncherTouchCallback extends ItemTouchHelper.SimpleCallback {

        int[] location = new int[2];
        boolean upAtDeleteAppArea;
        boolean upAtCreateShortcutArea;
        RecyclerView.ViewHolder dragHolder;

        LauncherTouchCallback() {
            super(UP | DOWN | LEFT | RIGHT | START | END, 0);
        }

        @Override
        public int interpolateOutOfBoundsScroll(RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
            return 0;
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
                        LaunchpadAdapter.ViewHolder holder = (LaunchpadAdapter.ViewHolder) viewHolder;
                        View itemView = viewHolder.itemView;
                        ViewCompat.setBackground(itemView, holder.shadow);
                        ViewCompat.setLayerType(itemView, ViewCompat.LAYER_TYPE_SOFTWARE, null);
                        if (bottomArea.getVisibility() == View.GONE) {
                            showBottomAction();
                        }
                    }
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
            return !upAtCreateShortcutArea && !upAtDeleteAppArea;
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
                if (bottomArea.getVisibility() == View.VISIBLE) {
                    mUiHandler.postDelayed(HomeActivity.this::hideBottomAction, 500L);
                    if (upAtCreateShortcutArea) {
                        createShortcut(viewHolder.getAdapterPosition());
                    } else if (upAtDeleteAppArea) {
                        deleteApp(viewHolder.getAdapterPosition());
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

            bottomArea.getLocationInWindow(location);
            int baseLine = location[1] - bottomArea.getHeight();
            if (y >= baseLine) {
                deleteAppArea.getLocationInWindow(location);
                int deleteAppAreaStartX = location[0];
                if (x < deleteAppAreaStartX) {
                    upAtCreateShortcutArea = true;
                    upAtDeleteAppArea = false;
                    createShortcutArea.setBackgroundColor(Color.parseColor("#0099cc"));
                    deleteAppArea.setBackgroundColor(Color.TRANSPARENT);
                    createShortcutArea.setAlpha(0.7f);
                    deleteAppArea.setAlpha(1f);
                } else {
                    upAtDeleteAppArea = true;
                    upAtCreateShortcutArea = false;
                    deleteAppArea.setBackgroundColor(Color.RED);
                    createShortcutArea.setBackgroundColor(Color.TRANSPARENT);
                    deleteAppArea.setAlpha(0.7f);
                    createShortcutArea.setAlpha(1f);
                }
            } else {
                upAtDeleteAppArea = false;
                upAtDeleteAppArea = false;
                createShortcutArea.setBackgroundColor(Color.TRANSPARENT);
                deleteAppArea.setBackgroundColor(Color.TRANSPARENT);
                createShortcutArea.setAlpha(1f);
                deleteAppArea.setAlpha(1f);
            }
        }
    }
}
