package io.virtualapp.abs.ui;

import android.app.Fragment;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;

import org.jdeferred.android.AndroidDeferredManager;

/**
 * @author Lody
 */
public class VActivity extends AppCompatActivity {

    protected AndroidDeferredManager defer() {
        return VUiKit.defer();
    }

    public Fragment findFragmentById(@IdRes int id) {
        return getFragmentManager().findFragmentById(id);
    }

    public void replaceFragment(@IdRes int id, Fragment fragment) {
        getFragmentManager().beginTransaction().replace(id, fragment).commit();
    }

    public void destroy() {
        finish();
    }
}
