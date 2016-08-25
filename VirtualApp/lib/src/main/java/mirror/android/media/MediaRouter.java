package mirror.android.media;

import android.os.IInterface;

import mirror.ClassDef;
import mirror.FieldDef;
import mirror.StaticFieldDef;

public class MediaRouter {
    public static Class<?> Class = ClassDef.init(MediaRouter.class, android.media.MediaRouter.class);
    public static StaticFieldDef sStatic;

    public static class Static {
        public static Class<?> Class = ClassDef.init(Static.class, "android.media.MediaRouter$Static");
        public static FieldDef<IInterface> mAudioService;
    }

    public static class StaticKitkat {
        public static Class<?> Class = ClassDef.init(StaticKitkat.class, "android.media.MediaRouter$Static");
        public static FieldDef<IInterface> mMediaRouterService;
    }
}
