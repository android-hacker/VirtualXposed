package com.lody.virtual.client.hook.patchs.am;

import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.WeakHashMap;

import mirror.android.app.LoadedApk;
import mirror.android.content.IIntentReceiverJB;

/**
 * @author Lody
 */
/* package */ class RegisterReceiver extends Hook {

    private static final int IDX_IIntentReceiver = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            ? 2
            : 1;

    private static final int IDX_RequiredPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            ? 4
            : 3;
    private static final int IDX_IntentFilter = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            ? 3
            : 2;

    private WeakHashMap<IBinder, IIntentReceiver> mProxyIIntentReceivers = new WeakHashMap<>();

    @Override
    public String getName() {
        return "registerReceiver";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        HookUtils.replaceFirstAppPkg(args);
        args[IDX_RequiredPermission] = null;
        IntentFilter filter = (IntentFilter) args[IDX_IntentFilter];
        IntentFilter backupFilter = new IntentFilter(filter);
        protectIntentFilter(filter);
        if (args.length > IDX_IIntentReceiver && IIntentReceiver.class.isInstance(args[IDX_IIntentReceiver])) {
            final IInterface old = (IInterface) args[IDX_IIntentReceiver];
            if (!IIntentReceiverProxy.class.isInstance(old)) {
                final IBinder token = old.asBinder();
                if (token != null) {
                    token.linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            token.unlinkToDeath(this, 0);
                            mProxyIIntentReceivers.remove(token);
                        }
                    }, 0);
                    IIntentReceiver proxyIIntentReceiver = mProxyIIntentReceivers.get(token);
                    if (proxyIIntentReceiver == null) {
                        proxyIIntentReceiver = new IIntentReceiverProxy(old);
                        mProxyIIntentReceivers.put(token, proxyIIntentReceiver);
                    }
                    WeakReference mDispatcher = LoadedApk.ReceiverDispatcher.InnerReceiver.mDispatcher.get(old);
                    if (mDispatcher != null) {
                        LoadedApk.ReceiverDispatcher.mIIntentReceiver.set(mDispatcher.get(), proxyIIntentReceiver);
                        args[IDX_IIntentReceiver] = proxyIIntentReceiver;
                    }
                }
            }
        }
        Object res = method.invoke(who, args);
        Intent intent = VActivityManager.get().dispatchStickyBroadcast(backupFilter);
        if (intent != null) {
            return intent;
        }
        return res;
    }

    private void protectIntentFilter(IntentFilter filter) {
        if (filter != null) {
            List<String> actions = mirror.android.content.IntentFilter.mActions.get(filter);
            ListIterator<String> iterator = actions.listIterator();
            while (iterator.hasNext()) {
                String action = iterator.next();
                if (SpecialComponentList.isActionInBlackList(action)) {
                    iterator.remove();
                    continue;
                }
                String newAction = SpecialComponentList.protectAction(action);
                if (newAction != null) {
                    iterator.set(newAction);
                }
            }
        }
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }

    private static class IIntentReceiverProxy extends IIntentReceiver.Stub {

        IInterface old;

        IIntentReceiverProxy(IInterface old) {
            this.old = old;
        }

        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered,
                                   boolean sticky, int sendingUser) throws RemoteException {
            if (!accept(intent)) {
                return;
            }
            SpecialComponentList.unprotectIntent(intent);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                IIntentReceiverJB.performReceive.call(old, intent, resultCode, data, extras, ordered, sticky, sendingUser);
            } else {
                mirror.android.content.IIntentReceiver.performReceive.call(old, intent, resultCode, data, extras, ordered, sticky);
            }
        }

        private boolean accept(Intent intent) {
            int uid = intent.getIntExtra("_VA_|_uid_", -1);
            if (uid != -1) {
                return VClientImpl.get().getVUid() == uid;
            }
            int userId = intent.getIntExtra("_VA_|_user_id_", -1);
            if (userId != -1) {
                return userId == VUserHandle.myUserId();
            }
            return true;
        }

        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered,
                                   boolean sticky) throws android.os.RemoteException {
            this.performReceive(intent, resultCode, data, extras, ordered, sticky, 0);
        }
    }
}