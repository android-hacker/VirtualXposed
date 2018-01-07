package io.virtualapp.delegate;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstallResult;

import java.io.IOException;

import io.virtualapp.R;

/**
 * @author Lody
 */

public class MyAppRequestListener implements VirtualCore.AppRequestListener {

    private final Context context;

    public MyAppRequestListener(Context context) {
        this.context = context;
    }

    @Override
    public void onRequestInstall(String path) {
        Resources resources = VirtualCore.get().getContext().getResources();
        Toast.makeText(context, resources.getString(R.string.installing_tips, path), Toast.LENGTH_SHORT).show();
        InstallResult res = VirtualCore.get().installPackage(path, InstallStrategy.UPDATE_IF_EXIST);
        if (res.isSuccess) {
            try {
                VirtualCore.get().preOpt(res.packageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (res.isUpdate) {
                Toast.makeText(context, resources.getString(R.string.update_success_tips, res.packageName),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, resources.getString(R.string.install_success_tips),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, resources.getString(R.string.install_fail_tips, res.packageName, res.error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestUninstall(String pkg) {
        Toast.makeText(context, "Uninstall: " + pkg, Toast.LENGTH_SHORT).show();

    }
}
