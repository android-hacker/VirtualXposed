package mirror.android.content;

import java.util.List;

import mirror.RefClass;
import mirror.RefObject;

/**
 * @author Lody
 */

public class IntentFilter {
    public static Class Class = RefClass.load(IntentFilter.class, android.content.IntentFilter.class);
    public static RefObject<List<String>> mActions;
}
