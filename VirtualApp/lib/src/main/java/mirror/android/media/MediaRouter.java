package mirror.android.media;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefObject;
import mirror.RefStaticObject;

public class MediaRouter {
    public static Class<?> TYPE = RefClass.load(MediaRouter.class, android.media.MediaRouter.class);
    public static RefStaticObject sStatic;

    public static class Static {
        public static Class<?> TYPE = RefClass.load(Static.class, "android.media.MediaRouter$Static");
        public static RefObject<IInterface> mAudioService;
    }

    public static class StaticKitkat {
        public static Class<?> TYPE = RefClass.load(StaticKitkat.class, "android.media.MediaRouter$Static");
        public static RefObject<IInterface> mMediaRouterService;
    }
}
