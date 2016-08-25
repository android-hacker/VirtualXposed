package mirror.android.content;

import android.os.IInterface;

import mirror.ClassDef;
import mirror.StaticFieldDef;

/**
 * @author Lody
 */

public class ContentResolver {
    public static Class<?> Class = ClassDef.init(ContentResolver.class, android.content.ContentResolver.class);
    public static StaticFieldDef<IInterface> sContentService;
}
