package mirror.android.media;

import android.os.IInterface;

import mirror.ClassDef;
import mirror.StaticFieldDef;
import mirror.StaticMethodDef;

public class AudioManager {
    public static Class<?> Class = ClassDef.init(AudioManager.class, android.media.AudioManager.class);
    public static StaticMethodDef getService;
    public static StaticFieldDef<IInterface> sService;
}