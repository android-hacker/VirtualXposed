package io.virtualapp.home.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.XApp;
import io.virtualapp.home.ListAppFragment;

/**
 * @author Lody
 */
public class AppPagerAdapter extends FragmentPagerAdapter {
    private List<String> titles = new ArrayList<>();
    private List<File> dirs = new ArrayList<>();

    public AppPagerAdapter(FragmentManager fm) {
        super(fm);
        titles.add(XApp.getApp().getResources().getString(R.string.clone_apps));
        dirs.add(null);
    }

    @Override
    public Fragment getItem(int position) {
        return ListAppFragment.newInstance(dirs.get(position));
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
