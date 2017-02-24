package io.virtualapp;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.github.moduth.blockcanary.BlockCanary;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.proto.InstallResult;

import java.io.IOException;

import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
public class VApp extends Application {


    private static VApp gDefault;

    public static VApp getApp() {
        return gDefault;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            StubManifest.ENABLE_IO_REDIRECT = true;
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        gDefault = this;
        super.onCreate();
        if (VirtualCore.get().isServerProcess()) {
            VirtualCore.get().setAppRequestListener(new VirtualCore.AppRequestListener() {
                @Override
                public void onRequestInstall(String path) {
                    Toast.makeText(VApp.this, "Installing: " + path, Toast.LENGTH_SHORT).show();
                    InstallResult res = VirtualCore.get().installApp(path, InstallStrategy.UPDATE_IF_EXIST);
                    if (res.isSuccess) {
                        try {
                            VirtualCore.get().preOpt(res.packageName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (res.isUpdate) {
                            Toast.makeText(VApp.this, "Update: " + res.packageName + " success!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VApp.this, "Install: " + res.packageName + " success!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(VApp.this, "Install failed: " + res.error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onRequestUninstall(String pkg) {
                    Toast.makeText(VApp.this, "Uninstall: " + pkg, Toast.LENGTH_SHORT).show();

                }
            });
        }else if (VirtualCore.get().isMainProcess()) {
            Once.initialise(this);
        } else if (VirtualCore.get().isVAppProcess()) {
            BlockCanary.install(this, new AppBlockCanaryContext());
            //组件监听
            VirtualCore.get().setComponentDelegate(new MyComponentDelegate());
            //虚拟手机信息，imei，蓝牙地址，mac
            VirtualCore.get().setPhoneInfoDelegate(new MyPhoneInfoDelegate());
            //最近任务的图标和标题
            VirtualCore.get().setTaskDescriptionDelegate(new MyTaskDescriptionDelegate());
        }
    }

}
