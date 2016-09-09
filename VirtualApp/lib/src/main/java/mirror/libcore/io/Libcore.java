package mirror.libcore.io;

import mirror.RefClass;
import mirror.RefStaticObject;

/**
 * @author Lody
 */

public class Libcore {
    public static Class<?> TYPE = RefClass.load(Libcore.class, "libcore.io.Libcore");

    public static RefStaticObject<Object> os;
}
