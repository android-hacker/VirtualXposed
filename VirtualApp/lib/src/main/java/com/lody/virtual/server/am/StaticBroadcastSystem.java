package com.lody.virtual.server.am;

import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.utils.collection.ArrayMap;
import com.lody.virtual.server.pm.VAppManagerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static android.content.Intent.FLAG_RECEIVER_REGISTERED_ONLY;

/**
 * @author Lody
 */

public class StaticBroadcastSystem {

	private final ArrayMap<String, List<BroadcastReceiver>> mReceivers = new ArrayMap<>();
	private final Context mContext;
	private final StaticScheduler mScheduler;
	private final VActivityManagerService mAMS;
	private final VAppManagerService mApp;

	public StaticBroadcastSystem(Context context, VActivityManagerService ams, VAppManagerService app) {
		this.mContext = context;
		this.mApp = app;
		this.mAMS = ams;
		mScheduler = new StaticScheduler();
	}

	public void startApp(PackageParser.Package p) {
		AppSetting setting = (AppSetting) p.mExtras;
		for (PackageParser.Activity receiver : p.receivers) {
			ActivityInfo info = receiver.info;
			List<? extends IntentFilter> filters = receiver.intents;
			List<BroadcastReceiver> receivers = mReceivers.get(p.packageName);
			if (receivers == null) {
				receivers = new ArrayList<>();
				mReceivers.put(p.packageName, receivers);
			}
			String componentAction = String.format("_VA_%s_%s", info.packageName, info.name);
			IntentFilter componentFilter = new IntentFilter(componentAction);
			BroadcastReceiver r = new StaticBroadcastReceiver(setting.appId, info, componentFilter);
			mContext.registerReceiver(r, componentFilter, null, mScheduler);
			receivers.add(r);
			for (IntentFilter filter : filters) {
				IntentFilter cloneFilter = new IntentFilter(filter);
				redirectFilterActions(cloneFilter);
				r = new StaticBroadcastReceiver(setting.appId, info, cloneFilter);
				mContext.registerReceiver(r, cloneFilter, null, mScheduler);
				receivers.add(r);
			}
		}
	}

	private void redirectFilterActions(IntentFilter filter) {
		List<String> actions = mirror.android.content.IntentFilter.mActions.get(filter);
		ListIterator<String> iterator = actions.listIterator();
		while (iterator.hasNext()) {
			String action = iterator.next();
			if (SpecialComponentList.isActionInBlackList(action)) {
				iterator.remove();
				continue;
			}
			String protectedAction = SpecialComponentList.protectAction(action);
			if (protectedAction != null) {
				iterator.set(protectedAction);
			}
		}
	}

	public void stopApp(String packageName) {
		List<BroadcastReceiver> receivers = mReceivers.get(packageName);
		if (receivers != null) {
			for (BroadcastReceiver r : receivers) {
				mContext.unregisterReceiver(r);
			}
		}
		mReceivers.remove(packageName);
	}

	private static final class StaticScheduler extends Handler {

	}

	private final class StaticBroadcastReceiver extends BroadcastReceiver {
		private int appId;
		private ActivityInfo info;
		@SuppressWarnings("unused")
		private IntentFilter filter;

		private StaticBroadcastReceiver(int appId, ActivityInfo info, IntentFilter filter) {
			this.appId = appId;
			this.info = info;
			this.filter = filter;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mApp.isBooting()) {
				return;
			}
			if ((intent.getFlags() & FLAG_RECEIVER_REGISTERED_ONLY) != 0) {
                return;
            }
			PendingResult result = goAsync();
			synchronized (mAMS) {
                if (!mAMS.handleStaticBroadcast(appId, info, intent, this, result)) {
                    result.finish();
                }
            }
		}
	}
}
