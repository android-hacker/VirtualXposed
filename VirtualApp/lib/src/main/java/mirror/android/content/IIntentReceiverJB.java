package mirror.android.content;

import android.content.Intent;
import android.os.Bundle;

import mirror.RefClass;
import mirror.RefMethod;
import mirror.MethodParams;

/**
 * @author Lody
 */

public class IIntentReceiverJB {
    public static Class<?> TYPE = RefClass.load(IIntentReceiverJB.class, "android.content.IIntentReceiver");

    @MethodParams({Intent.class, int.class,
            String.class, Bundle.class, boolean.class, boolean.class, int.class})
    public static RefMethod<Void> performReceive;


}
