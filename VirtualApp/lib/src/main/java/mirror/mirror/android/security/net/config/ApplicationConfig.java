package mirror.android.security.net.config;

import mirror.RefClass;
import mirror.RefStaticMethod;

public class ApplicationConfig {
    public static Class<?> TYPE = RefClass.load(ApplicationConfig.class, "android.security.net.config.ApplicationConfig");

    public static RefStaticMethod setDefaultInstance;
}
