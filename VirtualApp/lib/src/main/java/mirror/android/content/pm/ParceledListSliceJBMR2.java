package mirror.android.content.pm;

import android.os.Parcelable;

import java.util.List;

import mirror.ClassDef;
import mirror.CtorDef;
import mirror.MethodDef;
import mirror.MethodInfo;
import mirror.StaticFieldDef;

/**
 * @author Lody
 */

public class ParceledListSliceJBMR2 {
    public static StaticFieldDef<Parcelable.Creator> CREATOR;
    public static Class<?> Class = ClassDef.init(ParceledListSliceJBMR2.class, "android.content.pm.ParceledListSlice");
    @MethodInfo({List.class})
    public static CtorDef<Parcelable> ctor;
    public static MethodDef<List> getList;
}
