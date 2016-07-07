package io.virtualapp.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.home.adapters.AppListAdapter;
import io.virtualapp.home.models.AppModel;

/**
 * @author Lody
 */
public class ListAppActivity extends VActivity implements ListAppContract.ListAppView {

    private ListAppContract.ListAppPresenter mPresenter;

    private View mLoadingView;
    private ListView mListView;
    private AppListAdapter mAdapter;

    public static void gotoListApp(Activity activity) {
        Intent intent = new Intent(activity, ListAppActivity.class);
        activity.startActivityForResult(intent, VCommends.REQUEST_SELECT_APP);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_app);
        ActionBar actionBar = getSupportActionBar();
        setupActionBar(actionBar);
        mLoadingView = findViewById(R.id.app_progress_bar);
        mListView = (ListView) findViewById(R.id.app_list);
        mAdapter = new AppListAdapter(this);
        mListView.setAdapter(mAdapter);
        new ListAppPresenterImpl(this, this);
        mPresenter.start();
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            AppModel model = (AppModel) parent.getAdapter().getItem(position);
            mPresenter.selectApp(model);
        });

    }

    @Override
    public void startLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    @Override
    public void loadFinish(List<AppModel> models) {
        mAdapter.setModels(models);
        mLoadingView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(ListAppContract.ListAppPresenter presenter) {
        this.mPresenter = presenter;
    }

    private void setupActionBar(ActionBar actionBar) {
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.add_app);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
