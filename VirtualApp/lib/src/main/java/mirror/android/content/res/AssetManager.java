package mirror.android.content.res;

import mirror.ClassDef;
import mirror.CtorDef;
import mirror.MethodDef;
import mirror.MethodInfo;

/**
 * @author Lody
 */

public class AssetManager {
    public static Class<?> Class = ClassDef.init(AssetManager.class, android.content.res.AssetManager.class);
    public static CtorDef<android.content.res.AssetManager> ctor;
    @MethodInfo(String.class)
    public static MethodDef<Integer> addAssetPath;
}
