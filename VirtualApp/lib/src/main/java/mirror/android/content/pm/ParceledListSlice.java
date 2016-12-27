package mirror.android.content.pm;

import android.os.Parcelable;

import java.util.List;

import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefMethod;
import mirror.RefStaticObject;

/**
 * @author Lody
 */

public class ParceledListSlice {
    public static RefStaticObject<Parcelable.Creator> CREATOR;
    public static Class<?> TYPE = RefClass.load(ParceledListSlice.class, "android.content.pm.ParceledListSlice");
    public static RefMethod<Boolean> append;
    public static RefConstructor<Parcelable> ctor;
    public static RefMethod<Boolean> isLastSlice;
    public static RefMethod<Parcelable> populateList;
    public static RefMethod<Void> setLastSlice;
    public static RefMethod<List<?>> getList;
}
