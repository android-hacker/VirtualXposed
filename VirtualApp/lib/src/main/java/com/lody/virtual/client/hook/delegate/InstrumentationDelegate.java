package com.lody.virtual.client.hook.delegate;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * @author Lody
 */
public class InstrumentationDelegate extends Instrumentation {

	protected Instrumentation base;

	public InstrumentationDelegate(Instrumentation base) {
		this.base = base;
	}

	public static Application newApplication(Class<?> clazz, Context context)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return Instrumentation.newApplication(clazz, context);
	}

	@Override
	public void onCreate(Bundle arguments) {
		base.onCreate(arguments);
	}

	@Override
	public void start() {
		base.start();
	}

	@Override
	public void onStart() {
		base.onStart();
	}

	@Override
	public boolean onException(Object obj, Throwable e) {
		return base.onException(obj, e);
	}

	@Override
	public void sendStatus(int resultCode, Bundle results) {
		base.sendStatus(resultCode, results);
	}

	@Override
	public void finish(int resultCode, Bundle results) {
		base.finish(resultCode, results);
	}

	@Override
	public void setAutomaticPerformanceSnapshots() {
		base.setAutomaticPerformanceSnapshots();
	}

	@Override
	public void startPerformanceSnapshot() {
		base.startPerformanceSnapshot();
	}

	@Override
	public void endPerformanceSnapshot() {
		base.endPerformanceSnapshot();
	}

	@Override
	public void onDestroy() {
		base.onDestroy();
	}

	@Override
	public Context getContext() {
		return base.getContext();
	}

	@Override
	public ComponentName getComponentName() {
		return base.getComponentName();
	}

	@Override
	public Context getTargetContext() {
		return base.getTargetContext();
	}

	@Override
	public boolean isProfiling() {
		return base.isProfiling();
	}

	@Override
	public void startProfiling() {
		base.startProfiling();
	}

	@Override
	public void stopProfiling() {
		base.stopProfiling();
	}

	@Override
	public void setInTouchMode(boolean inTouch) {
		base.setInTouchMode(inTouch);
	}

	@Override
	public void waitForIdle(Runnable recipient) {
		base.waitForIdle(recipient);
	}

	@Override
	public void waitForIdleSync() {
		base.waitForIdleSync();
	}

	@Override
	public void runOnMainSync(Runnable runner) {
		base.runOnMainSync(runner);
	}

	@Override
	public Activity startActivitySync(Intent intent) {
		return base.startActivitySync(intent);
	}

	@Override
	public void addMonitor(ActivityMonitor monitor) {
		base.addMonitor(monitor);
	}

	@Override
	public ActivityMonitor addMonitor(IntentFilter filter, ActivityResult result, boolean block) {
		return base.addMonitor(filter, result, block);
	}

	@Override
	public ActivityMonitor addMonitor(String cls, ActivityResult result, boolean block) {
		return base.addMonitor(cls, result, block);
	}

	@Override
	public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {
		return base.checkMonitorHit(monitor, minHits);
	}

	@Override
	public Activity waitForMonitor(ActivityMonitor monitor) {
		return base.waitForMonitor(monitor);
	}

	@Override
	public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {
		return base.waitForMonitorWithTimeout(monitor, timeOut);
	}

	@Override
	public void removeMonitor(ActivityMonitor monitor) {
		base.removeMonitor(monitor);
	}

	@Override
	public boolean invokeMenuActionSync(Activity targetActivity, int id, int flag) {
		return base.invokeMenuActionSync(targetActivity, id, flag);
	}

	@Override
	public boolean invokeContextMenuAction(Activity targetActivity, int id, int flag) {
		return base.invokeContextMenuAction(targetActivity, id, flag);
	}

	@Override
	public void sendStringSync(String text) {
		base.sendStringSync(text);
	}

	@Override
	public void sendKeySync(KeyEvent event) {
		base.sendKeySync(event);
	}

	@Override
	public void sendKeyDownUpSync(int key) {
		base.sendKeyDownUpSync(key);
	}

	@Override
	public void sendCharacterSync(int keyCode) {
		base.sendCharacterSync(keyCode);
	}

	@Override
	public void sendPointerSync(MotionEvent event) {
		base.sendPointerSync(event);
	}

	@Override
	public void sendTrackballEventSync(MotionEvent event) {
		base.sendTrackballEventSync(event);
	}

	@Override
	public Application newApplication(ClassLoader cl, String className, Context context)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return base.newApplication(cl, className, context);
	}

	@Override
	public void callApplicationOnCreate(Application app) {
		base.callApplicationOnCreate(app);
	}

	@Override
	public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent,
			ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance)
			throws InstantiationException, IllegalAccessException {
		return base.newActivity(clazz, context, token, application, intent, info, title, parent, id,
				lastNonConfigurationInstance);
	}

	@Override
	public Activity newActivity(ClassLoader cl, String className, Intent intent)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return base.newActivity(cl, className, intent);
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		base.callActivityOnCreate(activity, icicle);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
		base.callActivityOnCreate(activity, icicle, persistentState);
	}

	@Override
	public void callActivityOnDestroy(Activity activity) {
		base.callActivityOnDestroy(activity);
	}

	@Override
	public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
		base.callActivityOnRestoreInstanceState(activity, savedInstanceState);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState,
			PersistableBundle persistentState) {
		base.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState);
	}

	@Override
	public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
		base.callActivityOnPostCreate(activity, icicle);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void callActivityOnPostCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
		base.callActivityOnPostCreate(activity, icicle, persistentState);
	}

	@Override
	public void callActivityOnNewIntent(Activity activity, Intent intent) {
		base.callActivityOnNewIntent(activity, intent);
	}


	@Override
	public void callActivityOnStart(Activity activity) {
		base.callActivityOnStart(activity);
	}

	@Override
	public void callActivityOnRestart(Activity activity) {
		base.callActivityOnRestart(activity);
	}

	@Override
	public void callActivityOnResume(Activity activity) {
		base.callActivityOnResume(activity);
	}

	@Override
	public void callActivityOnStop(Activity activity) {
		base.callActivityOnStop(activity);
	}

	@Override
	public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
		base.callActivityOnSaveInstanceState(activity, outState);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void callActivityOnSaveInstanceState(Activity activity, Bundle outState,
			PersistableBundle outPersistentState) {
		base.callActivityOnSaveInstanceState(activity, outState, outPersistentState);
	}

	@Override
	public void callActivityOnPause(Activity activity) {
		base.callActivityOnPause(activity);
	}

	@Override
	public void callActivityOnUserLeaving(Activity activity) {
		base.callActivityOnUserLeaving(activity);
	}

	@Override
	public Bundle getAllocCounts() {
		return base.getAllocCounts();
	}

	@Override
	public Bundle getBinderCounts() {
		return base.getBinderCounts();
	}


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Override
	public UiAutomation getUiAutomation() {
		return base.getUiAutomation();
	}


}
