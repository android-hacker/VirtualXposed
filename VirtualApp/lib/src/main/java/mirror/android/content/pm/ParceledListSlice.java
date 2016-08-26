package mirror.android.content.pm;

import android.os.Parcelable;

import mirror.ClassDef;
import mirror.CtorDef;
import mirror.MethodDef;
import mirror.StaticFieldDef;

/**
 * @author Lody
 */

public class ParceledListSlice {
    public static StaticFieldDef<Parcelable.Creator> CREATOR;
    public static Class<?> Class = ClassDef.init(ParceledListSlice.class, "android.content.pm.ParceledListSlice");
    public static MethodDef<Boolean> append;
    public static CtorDef<Parcelable> ctor;
    public static MethodDef<Boolean> isLastSlice;
    public static MethodDef<Parcelable> populateList;
    public static MethodDef<Void> setLastSlice;
}
