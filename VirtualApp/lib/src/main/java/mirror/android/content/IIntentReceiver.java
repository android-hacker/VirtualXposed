package mirror.android.content;

import android.content.Intent;
import android.os.Bundle;

import mirror.ClassDef;
import mirror.MethodDef;
import mirror.MethodInfo;

/**
 * @author Lody
 */

public class IIntentReceiver {
    public static Class<?> Class = ClassDef.init(IIntentReceiver.class, "android.content.IIntentReceiver");

    @MethodInfo({Intent.class, int.class,
            String.class, Bundle.class, boolean.class, boolean.class})
    public static MethodDef<Void> performReceive;


}
