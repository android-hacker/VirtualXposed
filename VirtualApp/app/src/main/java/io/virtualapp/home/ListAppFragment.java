package io.virtualapp.home;

import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import io.virtualapp.R;
import io.virtualapp.abs.ui.VFragment;
import io.virtualapp.home.adapters.AppListAdapter;
import io.virtualapp.home.models.AppModel;

/**
 * Created by tangzhibin on 16/7/16.
 */

public class ListAppFragment extends VFragment<ListAppContract.ListAppPresenter>
		implements
			ListAppContract.ListAppView {
	private static final String KEY_SELECT_FROM = "key_select_from";
	private ListView mListView;
	private ProgressBar mProgressBar;
	private AppListAdapter mAdapter;

	public static ListAppFragment newInstance(int selectFrom) {
		Bundle args = new Bundle();
		args.putInt(KEY_SELECT_FROM, selectFrom);
		ListAppFragment fragment = new ListAppFragment();
		fragment.setArguments(args);
		return fragment;
	}

	private int getSelectFrom() {
		int selectFrom = ListAppContract.SELECT_APP_FROM_SYSTEM;
		Bundle bundle = getArguments();
		if (bundle != null)
			selectFrom = bundle.getInt(KEY_SELECT_FROM, selectFrom);
		return selectFrom;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_app, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mListView = (ListView) view.findViewById(R.id.app_list);
		mProgressBar = (ProgressBar) view.findViewById(R.id.app_progress_bar);
		mAdapter = new AppListAdapter(getActivity());
		mListView.setAdapter(mAdapter);

		new ListAppPresenterImpl(getActivity(), this, getSelectFrom());
		mPresenter.start();
		mListView.setOnItemClickListener((parent, v, position, id) -> {
			AppModel model = (AppModel) parent.getAdapter().getItem(position);
			mPresenter.selectApp(model);
		});
	}

	@Override
	public void startLoading() {
		mProgressBar.setVisibility(View.VISIBLE);
		mListView.setVisibility(View.GONE);
	}

	@Override
	public void loadFinish(List<AppModel> models) {
		mAdapter.setModels(models);
		mProgressBar.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
	}

	@Override
	public void setPresenter(ListAppContract.ListAppPresenter presenter) {
		this.mPresenter = presenter;
	}

}
