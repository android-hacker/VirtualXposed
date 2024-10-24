package com.lody.virtual.client.hook.proxies.mount;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.utils.MethodParameterUtils;
import com.lody.virtual.helper.utils.VLog;

import java.io.File;
import java.lang.reflect.Method;
import java.util.UUID;

import mirror.android.os.UserHandle;
import mirror.android.os.storage.StorageVolume;

/**
 * @author Lody
 */

class MethodProxies {


    // StorageManager.getVolumeList()
    static class GetVolumeList extends MethodProxy {

        static final String TAG = "GetVolumeList";

        @Override
        public String getMethodName() {
            return "getVolumeList";
        }

        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {
            Log.d(TAG, "before call who:" + who + ", method:" + method.getName());
            if (args == null || args.length == 0) {
                return super.beforeCall(who, method, args);
            }
            if (args[0] instanceof Integer) {
                args[0] = getRealUid();
            }
            MethodParameterUtils.replaceFirstAppPkg(args);
            return super.beforeCall(who, method, args);
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            return result;
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            Object result;

            // After 13,  getVolumeList throw Neither user 10467 nor current process has android.permission.INTERACT_ACROSS_USERS.
            // hook return manual StorageVolume array.
            if (Build.VERSION.SDK_INT >= 33) {
                final String id = "stub_primary";
                final File path = new File("/storage/emulated/0"); //System.getenv("EXTERNAL_STORAGE")); //Environment.getLegacyExternalStorageDirectory();
                final String description = VirtualCore.get().getContext().getString(android.R.string.unknownName);
                final boolean primary = true;
                final boolean primaryPhysical = true;
                final boolean removable = primaryPhysical;
                final boolean emulated = !primaryPhysical;
                final boolean externallyManaged = false;
                final boolean allowMassStorage = false;
                final long maxFileSize = 0L;
                int userId = (int) args[0];
                final android.os.UserHandle owner =  UserHandle.ctor.newInstance(userId);
                final String fsUuid = null;
                final UUID uuid = null;
                final String state = Environment.MEDIA_MOUNTED;
                VLog.d(TAG, "id:" + id + ", path:" + path  + ", desc:" + description + "primary:" + primary + ", userId:" + userId );

//              StorageVolume constructor
//                   public StorageVolume(String id, File path, File internalPath, String description,
//                        boolean primary, boolean removable, boolean emulated, boolean externallyManaged,
//                        boolean allowMassStorage, long maxFileSize, UserHandle owner, UUID uuid, String fsUuid,
//                        String state)
                android.os.storage.StorageVolume[] storageVolumes =  {
                        StorageVolume.ctor.newInstance(
                                id,
                                path,
                                path,
                                description,
                                primary,
                                removable,
                                emulated,
                                externallyManaged,
                                allowMassStorage, maxFileSize, owner,
                                uuid /*uuid */, id,
                                state)};
                result = storageVolumes;
            } else {
                result = super.call(who, method, args);
            }

            return result;
        }
    }

    static class Mkdirs extends MethodProxy {

        @Override
        public String getMethodName() {
            return "mkdirs";
        }

        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return super.beforeCall(who, method, args);
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return super.call(who, method, args);
            }
            String path;
            if (args.length == 1) {
                path = (String) args[0];
            } else {
                path = (String) args[1];
            }
            File file = new File(path);
            if (!file.exists() && !file.mkdirs()) {
                return -1;
            }
            return 0;
        }
    }
}
