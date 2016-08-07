// IActivityManager.aidl
package com.lody.virtual.service;

import com.lody.virtual.helper.proto.VRedirectActRequest;
import com.lody.virtual.helper.proto.VActRedirectResult;
import com.lody.virtual.helper.proto.AppTaskInfo;
import android.content.Intent;
import android.content.pm.ActivityInfo;

interface IActivityManager {

    VActRedirectResult redirectTargetActivity(in VRedirectActRequest request);

    void onActivityCreated(in IBinder token, in ActivityInfo info, in ActivityInfo caller, int taskId);

    void onActivityResumed(in IBinder token);

    void onActivityDestroyed(in IBinder token);

    ActivityInfo getCallingActivity(in IBinder token);

    AppTaskInfo getTaskInfo(int taskId);

    String getPackageForToken(in IBinder token);

    ActivityInfo getActivityInfo(in IBinder token);

}
