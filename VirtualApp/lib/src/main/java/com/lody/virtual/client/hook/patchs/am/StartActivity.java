package com.lody.virtual.client.hook.patchs.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.TypedValue;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.ActivityClientRecord;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.stub.ChooserActivity;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.interfaces.IAppRequestListener;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class StartActivity extends Hook {

    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_PACKAGE = "package";

    @Override
    public String getName() {
        return "startActivity";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        int intentIndex = ArrayUtils.indexOfFirst(args, Intent.class);
        int resultToIndex = ArrayUtils.indexOfObject(args, IBinder.class, 2);

        String resolvedType = (String) args[intentIndex + 1];
        Intent intent = (Intent) args[intentIndex];
        intent.setDataAndType(intent.getData(), resolvedType);
        IBinder resultTo = resultToIndex >= 0 ? (IBinder) args[resultToIndex] : null;
        int userId = VUserHandle.myUserId();

        if (ComponentUtils.isStubComponent(intent)) {
            return method.invoke(who, args);
        }

        if (Intent.ACTION_INSTALL_PACKAGE.equals(intent.getAction())
                || (Intent.ACTION_VIEW.equals(intent.getAction())
                && "application/vnd.android.package-archive".equals(intent.getType()))) {
            if (handleInstallRequest(intent)) {
                return 0;
            }
        } else if ((Intent.ACTION_UNINSTALL_PACKAGE.equals(intent.getAction())
                || Intent.ACTION_DELETE.equals(intent.getAction()))
                && "package".equals(intent.getScheme())) {

            if (handleUninstallRequest(intent)) {
                return 0;
            }
        }

        String resultWho = null;
        int requestCode = 0;
        Bundle options = ArrayUtils.getFirst(args, Bundle.class);
        if (resultTo != null) {
            resultWho = (String) args[resultToIndex + 1];
            requestCode = (int) args[resultToIndex + 2];
        }
        // chooser
        if (ChooserActivity.check(intent)) {
            intent.setComponent(new ComponentName(getHostContext(), ChooserActivity.class));
            intent.putExtra(Constants.EXTRA_USER_HANDLE, userId);
            intent.putExtra(ChooserActivity.EXTRA_DATA, options);
            intent.putExtra(ChooserActivity.EXTRA_WHO, resultWho);
            intent.putExtra(ChooserActivity.EXTRA_REQUEST_CODE, requestCode);
            return method.invoke(who, args);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            args[intentIndex - 1] = getHostPkg();
        }

        ActivityInfo activityInfo = VirtualCore.get().resolveActivityInfo(intent, userId);
        if (activityInfo == null) {
            return method.invoke(who, args);
        }

        int res = VActivityManager.get().startActivity(intent, activityInfo, resultTo, options, resultWho, requestCode, VUserHandle.myUserId());
        if (res != 0 && resultTo != null && requestCode > 0) {
            VActivityManager.get().sendActivityResult(resultTo, resultWho, requestCode);
        }
        if (resultTo != null) {
            ActivityClientRecord r = VActivityManager.get().getActivityRecord(resultTo);
            if (r != null && r.activity != null) {
                try {
                    TypedValue out = new TypedValue();
                    Resources.Theme theme = r.activity.getResources().newTheme();
                    theme.applyStyle(activityInfo.getThemeResource(), true);
                    if (theme.resolveAttribute(android.R.attr.windowAnimationStyle, out, true)) {

                        TypedArray array = theme.obtainStyledAttributes(out.data,
                                new int[]{
                                        android.R.attr.activityOpenEnterAnimation,
                                        android.R.attr.activityOpenExitAnimation
                                });

                        r.activity.overridePendingTransition(array.getResourceId(0, 0), array.getResourceId(1, 0));
                        array.recycle();
                    }
                } catch (Throwable e) {
                    // Ignore
                }
            }
        }
        return res;
    }


    private boolean handleInstallRequest(Intent intent) {
        IAppRequestListener listener = VirtualCore.get().getAppRequestListener();
        if (listener != null) {
            Uri packageUri = intent.getData();
            if (SCHEME_FILE.equals(packageUri.getScheme())) {
                File sourceFile = new File(packageUri.getPath());
                try {
                    listener.onRequestInstall(sourceFile.getPath());
                    return true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;
    }

    private boolean handleUninstallRequest(Intent intent) {
        IAppRequestListener listener = VirtualCore.get().getAppRequestListener();
        if (listener != null) {
            Uri packageUri = intent.getData();
            if (SCHEME_PACKAGE.equals(packageUri.getScheme())) {
                String pkg = packageUri.getSchemeSpecificPart();
                try {
                    listener.onRequestUninstall(pkg);
                    return true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;
    }

}
