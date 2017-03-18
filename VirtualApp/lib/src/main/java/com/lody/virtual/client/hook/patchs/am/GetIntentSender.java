package com.lody.virtual.client.hook.patchs.am;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.client.stub.StubPendingActivity;
import com.lody.virtual.client.stub.StubPendingReceiver;
import com.lody.virtual.client.stub.StubPendingService;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class GetIntentSender extends Hook {

    @Override
    public String getName() {
        return "getIntentSender";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        String creator = (String) args[1];
        String[] resolvedTypes = (String[]) args[6];
        int type = (int) args[0];
        int flags = (int) args[7];
        if ((PendingIntent.FLAG_UPDATE_CURRENT & flags) != 0) {
            flags = (flags & ~(PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_NO_CREATE)) | PendingIntent.FLAG_CANCEL_CURRENT;
        }
        if (args[5] instanceof Intent[]) {
            Intent[] intents = (Intent[]) args[5];
            if (intents.length > 0) {
                Intent intent = intents[intents.length - 1];
                if (resolvedTypes != null && resolvedTypes.length > 0) {
                    intent.setDataAndType(intent.getData(), resolvedTypes[resolvedTypes.length - 1]);
                }
                Intent targetIntent = redirectIntentSender(type, creator, intent);
                if (targetIntent != null) {
                    args[5] = new Intent[]{targetIntent};
                }
            }
        }
        args[7] = flags;
        args[1] = getHostPkg();
        IInterface sender = (IInterface) method.invoke(who, args);
        if (sender != null && creator != null) {
            VActivityManager.get().addPendingIntent(sender.asBinder(), creator);
        }
        return sender;
    }

    private Intent redirectIntentSender(int type, String creator, Intent intent) {
        Intent newIntent = intent.cloneFilter();
        switch (type) {
            case ActivityManagerCompat.INTENT_SENDER_ACTIVITY: {
                ComponentInfo info = VirtualCore.get().resolveActivityInfo(intent, VUserHandle.myUserId());
                if (info != null) {
                    newIntent.setClass(getHostContext(), StubPendingActivity.class);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            } break;
            case ActivityManagerCompat.INTENT_SENDER_SERVICE: {
                ComponentInfo info = VirtualCore.get().resolveServiceInfo(intent, VUserHandle.myUserId());
                if (info != null) {
                    newIntent.setClass(getHostContext(), StubPendingService.class);
                }
            } break;
            case ActivityManagerCompat.INTENT_SENDER_BROADCAST: {
                newIntent.setClass(getHostContext(), StubPendingReceiver.class);
            } break;
            default:
                return null;
        }
        newIntent.putExtra(StubManifest.IDENTITY_PREFIX + "_user_id_", VUserHandle.myUserId());
        newIntent.putExtra(StubManifest.IDENTITY_PREFIX + "_intent_", intent);
        newIntent.putExtra(StubManifest.IDENTITY_PREFIX + "_creator_", creator);
        newIntent.putExtra(StubManifest.IDENTITY_PREFIX + "_from_inner_", true);
        return newIntent;
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }

}
