package io.virtualapp.home.adapters;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lody.virtual.helper.utils.Reflect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.VApp;
import io.virtualapp.home.ListAppFragment;

/**
 * @author Lody
 */
public class AppPagerAdapter extends FragmentPagerAdapter {
    private List<String> titles = new ArrayList<>();
    private List<File> dirs = new ArrayList<>();

    public AppPagerAdapter(FragmentManager fm) {
        super(fm);
        titles.add(VApp.getApp().getResources().getString(R.string.clone_apps));
        dirs.add(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Context ctx = VApp.getApp();
            StorageManager storage = (StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
            for (StorageVolume volume : storage.getStorageVolumes()) {
                //Why the fuck are getPathFile and getUserLabel hidden?!
                //StorageVolume is kinda useless without those...
                File dir = Reflect.on(volume).call("getPathFile").get();
                String label = Reflect.on(volume).call("getUserLabel").get();
                if (dir.listFiles() != null) {
                    titles.add(label);
                    dirs.add(dir);
                }
            }
        } else {
            // Fallback: only support the default storage sources
            File storageFir = Environment.getExternalStorageDirectory();
            if (storageFir != null && storageFir.isDirectory()) {
                titles.add(VApp.getApp().getResources().getString(R.string.external_storage));
                dirs.add(storageFir);
            }
        }
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
