package io.virtualapp.home.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import io.virtualapp.home.ListAppContract;
import io.virtualapp.home.ListAppFragment;

/**
 * Created by tangzhibin on 16/7/16.
 */

public class AppPagerAdapter extends FragmentPagerAdapter {
	private static String[] TITLES = {"系统", "SD卡"};

	public AppPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0 :
				return ListAppFragment.newInstance(ListAppContract.SELECT_APP_FROM_SYSTEM);
			case 1 :
				return ListAppFragment.newInstance(ListAppContract.SELECT_APP_FROM_SD_CARD);

		}
		return null;
	}

	@Override
	public int getCount() {
		return TITLES.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return TITLES[position];
	}
}
