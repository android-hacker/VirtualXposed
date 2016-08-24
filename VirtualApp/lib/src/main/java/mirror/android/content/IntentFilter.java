package mirror.android.content;

import java.util.List;

import mirror.ClassDef;
import mirror.FieldDef;

/**
 * @author Lody
 */

public class IntentFilter {
    public static Class Class = ClassDef.init(IntentFilter.class, android.content.IntentFilter.class);
    public static FieldDef<List<String>> mActions;
}
