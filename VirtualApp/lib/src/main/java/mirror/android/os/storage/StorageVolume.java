package mirror.android.os.storage;

import java.io.File;
import java.util.UUID;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefMethod;
import mirror.RefObject;
import android.os.UserHandle;

public class StorageVolume {

    public static Class<?> Class = RefClass.load(StorageVolume.class, "android.os.storage.StorageVolume");



    // StorageVolume(String id, File path, File internalPath, String description,
    //              boolean primary, boolean removable, boolean emulated, boolean externallyManaged,
    //              boolean allowMassStorage, long maxFileSize, UserHandle owner, UUID uuid, String fsUuid,
    //              String state)
    // upper android 13 (33)
    @MethodParams({String.class, File.class, File.class, String.class,
            boolean.class, boolean.class, boolean.class, boolean.class,
            boolean.class, long.class, UserHandle.class, UUID.class, String.class,
            String.class
    })
    public static RefConstructor<android.os.storage.StorageVolume> ctor;


    public static RefObject<File> mPath;

    public static RefMethod<String> getPath;
}
