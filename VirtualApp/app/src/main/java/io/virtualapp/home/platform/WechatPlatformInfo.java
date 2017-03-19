package io.virtualapp.home.platform;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;

/**
 * @author Lody
 */

public class WechatPlatformInfo extends PlatformInfo {

    public WechatPlatformInfo() {
        super("com.tencent.mm");
    }

    @Override
    public boolean relyOnPlatform(PackageInfo info) {
        if (info.activities == null) {
            return false;
        }
        for (ActivityInfo activityInfo : info.activities) {
            if (activityInfo.name.endsWith("WXEntryActivity")) {
                return true;
            }
        }
        return false;
    }
}
