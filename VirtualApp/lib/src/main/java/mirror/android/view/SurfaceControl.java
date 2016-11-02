package mirror.android.view;

import android.graphics.Bitmap;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * @author Lody
 */

public class SurfaceControl {

    public static Class<?> TYPE = RefClass.load(SurfaceControl.class, "android.view.SurfaceControl");

    @MethodParams({int.class/*width*/, int.class/*height*/})
    public static RefStaticMethod<Bitmap> screnshot;
}
