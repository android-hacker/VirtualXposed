package io.virtualapp;

import android.app.ActivityManager;
import android.content.Context;

import com.lody.virtual.client.env.CrashReporter;

import java.util.List;

/**
 * @author qisen (woaitqs@gmail.com)
 */
public class VAppCrashHandler implements Thread.UncaughtExceptionHandler {

  private static Thread.UncaughtExceptionHandler defaultCrashHandler;
  private static VAppCrashHandler crashHandler;

  static {
    crashHandler = new VAppCrashHandler();
  }

  public static VAppCrashHandler getInstance() {
    return crashHandler;
  }

  public void register() {
    if (defaultCrashHandler == null) {
      defaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    Thread.setDefaultUncaughtExceptionHandler(this);
  }

  @Override
  public void uncaughtException(Thread thread, Throwable ex) {
    CrashReporter.report(getProcessName(), ex);
    ex.printStackTrace();
    defaultCrashHandler.uncaughtException(thread, ex);
  }

  public String getProcessName() {
    ActivityManager am = (ActivityManager) VApp.getApp().getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
    for (ActivityManager.RunningAppProcessInfo info : infos) {
      if (info.pid == android.os.Process.myPid()) {
        return info.processName;
      }
    }
    return null;
  }

}