package io.virtualapp.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.home.adapters.AppPagerAdapter;

/**
 * @author Lody
 */
public class ListAppActivity extends VActivity {

    private Toolbar mToolBar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    public static void gotoListApp(Activity activity) {
        Intent intent = new Intent(activity, ListAppActivity.class);
        activity.startActivityForResult(intent, VCommends.REQUEST_SELECT_APP);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clone_app);
        mToolBar = (Toolbar) findViewById(R.id.clone_app_tool_bar);
        mTabLayout = (TabLayout) mToolBar.findViewById(R.id.clone_app_tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.clone_app_view_pager);
        mViewPager.setAdapter(new AppPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);

        // Request permission to access external storage
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
                    .setMessage(R.string.list_app_access_external_storage)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                    })
                    .create();
            try {
                alertDialog.show();
            } catch (Throwable ignored) {
                // BadTokenException.
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                mViewPager.setAdapter(new AppPagerAdapter(getSupportFragmentManager()));
                break;
            }
        }
    }
}
