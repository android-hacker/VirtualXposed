package mirror.com.android.internal.os.health;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefObject;

/**
 * @author weishu
 * @date 2020/11/24.
 */

public class SystemHealthManager {
    public static Class<?> TYPE = RefClass.load(SystemHealthManager.class, "android.os.health.SystemHealthManager");

    public static RefObject<IInterface> mBatteryStats;
}
