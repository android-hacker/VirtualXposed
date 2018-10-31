package io.virtualapp.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.virtualapp.R;
import io.virtualapp.XApp;
import io.virtualapp.abs.ui.VFragment;
import io.virtualapp.home.adapters.CloneAppListAdapter;
import io.virtualapp.home.models.AppInfo;
import io.virtualapp.home.models.AppInfoLite;
import io.virtualapp.sys.Installd;
import io.virtualapp.widgets.DragSelectRecyclerView;


/**
 * @author Lody
 */
public class ListAppFragment extends VFragment<ListAppContract.ListAppPresenter> implements ListAppContract.ListAppView {
    private static final String KEY_SELECT_FROM = "key_select_from";
    private static final int REQUEST_GET_FILE = 1;

    private DragSelectRecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private Button mInstallButton;
    private CloneAppListAdapter mAdapter;
    private View mSelectFromExternal;

    public static ListAppFragment newInstance(File selectFrom) {
        Bundle args = new Bundle();
        if (selectFrom != null)
            args.putString(KEY_SELECT_FROM, selectFrom.getPath());
        ListAppFragment fragment = new ListAppFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private File getSelectFrom() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String selectFrom = bundle.getString(KEY_SELECT_FROM);
            if (selectFrom != null) {
                return new File(selectFrom);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_app, null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.saveInstanceState(outState);
    }

    private void whatIsTaiChi() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.what_is_exp)
                .setMessage(R.string.exp_tips)
                .setPositiveButton(R.string.exp_introduce_title, (dialog, which) -> {
                    Intent t = new Intent(Intent.ACTION_VIEW);
                    t.setData(Uri.parse("https://www.coolapk.com/apk/me.weishu.exp"));
                    startActivity(t);
                }).setNegativeButton(R.string.about_donate_title, (dialog, which) -> {
                    Intent t = new Intent(Intent.ACTION_VIEW);
                    t.setData(Uri.parse("https://vxposed.com/donate.html"));
                    startActivity(t);
                })
                .create();
        try {
            alertDialog.show();
        } catch (Throwable ignored) {
        }
    }

    private void chooseInstallWay(Runnable runnable, String path) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.install_choose_way)
                .setMessage(R.string.install_choose_content)
                .setPositiveButton(R.string.install_choose_taichi, (dialog, which) -> {
                    PackageManager packageManager = getActivity().getPackageManager();
                    try {
                        packageManager.getPackageInfo("me.weishu.exp", 0);
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("me.weishu.exp", "me.weishu.exp.ui.MainActivity"));
                        intent.putExtra("path", path);
                        startActivity(intent);
                    } catch (PackageManager.NameNotFoundException e) {
                        AlertDialog showInstallDialog = new AlertDialog.Builder(getContext())
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(R.string.install_taichi_not_exist)
                                .setPositiveButton(R.string.install_go_to_install_exp, (dialog1, which1) -> {
                                    Intent t = new Intent(Intent.ACTION_VIEW);
                                    t.setData(Uri.parse("https://www.coolapk.com/apk/me.weishu.exp"));
                                    startActivity(t);
                                })
                                .create();
                        showInstallDialog.show();
                    } catch (Throwable e) {
                        AlertDialog showInstallDialog = new AlertDialog.Builder(getContext())
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(R.string.install_taichi_while_old_version)
                                .setPositiveButton(R.string.install_go_latest_exp, (dialog1, which1) -> {
                                    Intent t = new Intent(Intent.ACTION_VIEW);
                                    t.setData(Uri.parse("https://www.coolapk.com/apk/me.weishu.exp"));
                                    startActivity(t);
                                })
                                .create();
                        showInstallDialog.show();
                    }
                    finishActivity();
                }).setNegativeButton("VirtualXposed", (dialog, which) -> {
                    if (runnable != null) {
                        runnable.run();
                    }
                    finishActivity();
                }).setNeutralButton(R.string.what_is_exp, ((dialog, which) -> {
                    whatIsTaiChi();
                }))
                .create();
        try {
            alertDialog.show();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mRecyclerView = (DragSelectRecyclerView) view.findViewById(R.id.select_app_recycler_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.select_app_progress_bar);
        mInstallButton = (Button) view.findViewById(R.id.select_app_install_btn);
        mSelectFromExternal = view.findViewById(R.id.select_app_from_external);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, OrientationHelper.VERTICAL));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(new ColorDrawable(0x1f000000));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter = new CloneAppListAdapter(getActivity(), getSelectFrom());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new CloneAppListAdapter.ItemEventListener() {
            @Override
            public void onItemClick(AppInfo info, int position) {
                int count = mAdapter.getSelectedCount();
                if (!mAdapter.isIndexSelected(position)) {
                    if (count >= 9) {
                        Toast.makeText(getContext(), R.string.install_too_much_once_time, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                mAdapter.toggleSelected(position);
            }

            @Override
            public boolean isSelectable(int position) {
                return mAdapter.isIndexSelected(position) || mAdapter.getSelectedCount() < 9;
            }
        });
        mAdapter.setSelectionListener(count -> {
            mInstallButton.setEnabled(count > 0);
            mInstallButton.setText(String.format(Locale.ENGLISH, XApp.getApp().getResources().getString(R.string.install_d), count));
        });
        mInstallButton.setOnClickListener(v -> {
            Integer[] selectedIndices = mAdapter.getSelectedIndices();
            ArrayList<AppInfoLite> dataList = new ArrayList<AppInfoLite>(selectedIndices.length);
            for (int index : selectedIndices) {
                AppInfo info = mAdapter.getItem(index);
                dataList.add(new AppInfoLite(info.packageName, info.path, info.fastOpen, info.disableMultiVersion));
            }

            if (dataList.size() > 0) {
                String path = dataList.get(0).path;
                chooseInstallWay(() -> Installd.startInstallerActivity(getActivity(), dataList), path);
            }
        });
        mSelectFromExternal.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.android.package-archive"); // apk file
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(intent, REQUEST_GET_FILE);
            } catch (Throwable ignored) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
        new ListAppPresenterImpl(getActivity(), this, getSelectFrom()).start();
    }

    @Override
    public void startLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void loadFinish(List<AppInfo> infoList) {
        mAdapter.setList(infoList);
        mRecyclerView.setDragSelectActive(false, 0);
        mAdapter.setSelected(0, false);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(ListAppContract.ListAppPresenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!(requestCode == REQUEST_GET_FILE && resultCode == Activity.RESULT_OK)) {
            return;
        }
        Activity current = getActivity();
        if (current == null) {
            return;
        }

        Uri uri = data.getData();
        String path = getPath(getActivity(), uri);
        if (path == null) {
            return;
        }

        chooseInstallWay(() -> Installd.handleRequestFromFile(getActivity(), path), path);
    }

    public static String getPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it  Or Log it.
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
}
