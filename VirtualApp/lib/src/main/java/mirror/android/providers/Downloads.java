package mirror.android.providers;

import android.net.Uri;

import mirror.RefClass;
import mirror.RefStaticObject;

/**
 * @author Lody
 */

public class Downloads {
    public static RefStaticObject<String> COLUMN_ALLOWED_NETWORK_TYPES;
    public static RefStaticObject<String> COLUMN_ALLOW_ROAMING;
    public static RefStaticObject<String> COLUMN_DELETED;
    public static RefStaticObject<String> COLUMN_DESCRIPTION;
    public static RefStaticObject<String> COLUMN_DESTINATION;
    public static RefStaticObject<String> COLUMN_FILE_NAME_HINT;
    public static RefStaticObject<String> COLUMN_IS_PUBLIC_API;
    public static RefStaticObject<String> COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI;
    public static RefStaticObject<String> COLUMN_MEDIA_SCANNED;
    public static RefStaticObject<String> COLUMN_MIME_TYPE;
    public static RefStaticObject<String> COLUMN_NOTIFICATION_PACKAGE;
    public static RefStaticObject<String> COLUMN_TITLE;
    public static RefStaticObject<String> COLUMN_URI;
    public static RefStaticObject<String> COLUMN_VISIBILITY;
    public static RefStaticObject<Uri> CONTENT_URI;
    public static Class<?> TYPE = RefClass.load(Downloads.class, "android.provider.Downloads$Impl");
    public static RefStaticObject<Integer> DESTINATION_CACHE_PARTITION_PURGEABLE;
    public static RefStaticObject<Integer> DESTINATION_FILE_URI;

    public static class Impl {
        public static class RequestHeaders {
            public static Class<?> TYPE = RefClass.load(RequestHeaders.class, "android.provider.Downloads$Impl$RequestHeaders");
            public static RefStaticObject<String> INSERT_KEY_PREFIX;
        }

    }
}
