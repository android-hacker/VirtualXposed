package mirror.android.media;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefStaticObject;
import mirror.RefStaticMethod;

public class AudioManager {
    public static Class<?> TYPE = RefClass.load(AudioManager.class, android.media.AudioManager.class);
    public static RefStaticMethod getService;
    public static RefStaticObject<IInterface> sService;
}