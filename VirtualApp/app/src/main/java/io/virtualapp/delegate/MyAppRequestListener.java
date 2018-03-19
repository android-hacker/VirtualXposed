package io.virtualapp.delegate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.lody.virtual.client.core.VirtualCore;

import java.io.File;

import io.virtualapp.sys.InstallerActivity;

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
        try {
            Intent t = new Intent(context, InstallerActivity.class);
            t.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
            t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(t);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestUninstall(String pkg) {
        Toast.makeText(context, "Uninstall: " + pkg, Toast.LENGTH_SHORT).show();
    }
}
