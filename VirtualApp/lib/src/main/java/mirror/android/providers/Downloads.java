package mirror.android.providers;

import android.net.Uri;

import mirror.ClassDef;
import mirror.StaticFieldDef;

/**
 * @author Lody
 */

public class Downloads {
    public static StaticFieldDef<String> COLUMN_ALLOWED_NETWORK_TYPES;
    public static StaticFieldDef<String> COLUMN_ALLOW_ROAMING;
    public static StaticFieldDef<String> COLUMN_DELETED;
    public static StaticFieldDef<String> COLUMN_DESCRIPTION;
    public static StaticFieldDef<String> COLUMN_DESTINATION;
    public static StaticFieldDef<String> COLUMN_FILE_NAME_HINT;
    public static StaticFieldDef<String> COLUMN_IS_PUBLIC_API;
    public static StaticFieldDef<String> COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI;
    public static StaticFieldDef<String> COLUMN_MEDIA_SCANNED;
    public static StaticFieldDef<String> COLUMN_MIME_TYPE;
    public static StaticFieldDef<String> COLUMN_NOTIFICATION_PACKAGE;
    public static StaticFieldDef<String> COLUMN_TITLE;
    public static StaticFieldDef<String> COLUMN_URI;
    public static StaticFieldDef<String> COLUMN_VISIBILITY;
    public static StaticFieldDef<Uri> CONTENT_URI;
    public static Class<?> Class = ClassDef.init(Downloads.class, "android.provider.Downloads$Impl");
    public static StaticFieldDef<Integer> DESTINATION_CACHE_PARTITION_PURGEABLE;
    public static StaticFieldDef<Integer> DESTINATION_FILE_URI;

    public static class Impl {
        public static class RequestHeaders {
            public static Class<?> Class = ClassDef.init(RequestHeaders.class, "android.provider.Downloads$Impl$RequestHeaders");
            public static StaticFieldDef<String> INSERT_KEY_PREFIX;
        }

    }
}
