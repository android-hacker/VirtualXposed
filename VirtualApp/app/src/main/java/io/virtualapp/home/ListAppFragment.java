package io.virtualapp.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.io.File;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VFragment;
import io.virtualapp.home.adapters.AppListAdapter;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.PackageAppData;

/**
 * Created by tangzhibin on 16/7/16.
 */
public class ListAppFragment extends VFragment<ListAppContract.ListAppPresenter> implements ListAppContract.ListAppView {
    private static final String KEY_SELECT_FROM = "key_select_from";
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private AppListAdapter mAdapter;

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.app_list);
        mProgressBar = (ProgressBar) view.findViewById(R.id.app_progress_bar);
        mAdapter = new AppListAdapter(getActivity());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        new ListAppPresenterImpl(getActivity(), this, getSelectFrom());
        mPresenter.start();
        mAdapter.setListener((pos) -> {
            PackageAppData model = (PackageAppData) mAdapter.getList().get(pos);
            mPresenter.selectApp(model);
        });
    }

    @Override
    public void startLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void loadFinish(List<AppData> models) {
        mAdapter.setList(models);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(ListAppContract.ListAppPresenter presenter) {
        this.mPresenter = presenter;
    }

}
