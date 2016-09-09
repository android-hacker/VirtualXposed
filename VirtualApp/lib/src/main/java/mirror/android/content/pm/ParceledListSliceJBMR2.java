package mirror.android.content.pm;

import android.os.Parcelable;

import java.util.List;

import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefMethod;
import mirror.MethodParams;
import mirror.RefStaticObject;

/**
 * @author Lody
 */

public class ParceledListSliceJBMR2 {
    public static RefStaticObject<Parcelable.Creator> CREATOR;
    public static Class<?> TYPE = RefClass.load(ParceledListSliceJBMR2.class, "android.content.pm.ParceledListSlice");
    @MethodParams({List.class})
    public static RefConstructor<Parcelable> ctor;
    public static RefMethod<List> getList;
}
