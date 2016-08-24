package mirror.android.content;

import android.content.IContentProvider;

import mirror.ClassDef;
import mirror.FieldDef;

public class ContentProviderClient {
    public static Class Class = ClassDef.init(ContentProviderClient.class, android.content.ContentProviderClient.class);
    public static FieldDef<IContentProvider> mContentProvider;
}