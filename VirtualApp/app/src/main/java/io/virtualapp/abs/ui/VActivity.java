package io.virtualapp.abs.ui;

import org.jdeferred.android.AndroidDeferredManager;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Lody
 */
public class VActivity extends AppCompatActivity {

	protected AndroidDeferredManager defer() {
		return VUiKit.defer();
	}

	public Fragment findFragmentById(@IdRes int id) {
		return getSupportFragmentManager().findFragmentById(id);
	}

	public void replaceFragment(@IdRes int id, Fragment fragment) {
		getSupportFragmentManager().beginTransaction().replace(id, fragment).commit();
	}

	public void destroy() {
		finish();
	}
}
