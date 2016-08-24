package com.lody.virtual.client.env;

import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.Process;

import com.lody.virtual.helper.utils.VLog;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author qisen (woaitqs@gmail.com)
 */
public class CrashReporter {

  private static final SimpleDateFormat SIMPLE_DATE_FORMAT
      = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

  public static void report(String processName, Throwable ex) {
    PrintWriter writer = null;

    try {

      VLog.w(CrashReporter.class.getSimpleName(), "begin to record crash log.");

      Date e = new Date();
      String dateStr = SIMPLE_DATE_FORMAT.format(e);

      File file = new File(Environment.getExternalStorageDirectory(),
          String.format("vapp/crash/CrashLog_%s_%s.log", dateStr, Process.myPid()));

      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }

      if (file.exists()) {
        file.delete();
      }

      writer = new PrintWriter(file);
      writer.println("\n\nDate:" + SIMPLE_DATE_FORMAT.format(e));
      writer.println("\n\n-------System Information-------");

      writer.println("PName:" + processName);

      writer.println("Board:" + SystemPropertiesCompat.get("ro.product.board", "unknown"));
      writer.println("ro.bootloader:" + SystemPropertiesCompat.get("ro.bootloader", "unknown"));
      writer.println("ro.product.brand:" + SystemPropertiesCompat.get("ro.product.brand", "unknown"));
      writer.println("ro.product.cpu.abi:" + SystemPropertiesCompat.get("ro.product.cpu.abi", "unknown"));
      writer.println("ro.product.cpu.abi2:" + SystemPropertiesCompat.get("ro.product.cpu.abi2", "unknown"));
      writer.println("ro.product.device:" + SystemPropertiesCompat.get("ro.product.device", "unknown"));
      writer.println("ro.build.display.id:" + SystemPropertiesCompat.get("ro.build.display.id", "unknown"));
      writer.println("ro.build.fingerprint:" + SystemPropertiesCompat.get("ro.build.fingerprint", "unknown"));
      writer.println("ro.hardware:" + SystemPropertiesCompat.get("ro.hardware", "unknown"));
      writer.println("ro.build.host:" + SystemPropertiesCompat.get("ro.build.host", "unknown"));
      writer.println("ro.build.id:" + SystemPropertiesCompat.get("ro.build.id", "unknown"));
      writer.println("ro.product.manufacturer:" + SystemPropertiesCompat.get("ro.product.manufacturer", "unknown"));
      writer.println("ro.product.model:" + SystemPropertiesCompat.get("ro.product.model", "unknown"));
      writer.println("ro.product.name:" + SystemPropertiesCompat.get("ro.product.name", "unknown"));
      writer.println("gsm.version.baseband:" + SystemPropertiesCompat.get("gsm.version.baseband", "unknown"));
      writer.println("ro.build.tags:" + SystemPropertiesCompat.get("ro.build.tags", "unknown"));
      writer.println("ro.build.type:" + SystemPropertiesCompat.get("ro.build.type", "unknown"));
      writer.println("ro.build.user:" + SystemPropertiesCompat.get("ro.build.user", "unknown"));
      writer.println("ro.build.version.codename:" + SystemPropertiesCompat.get("ro.build.version.codename", "unknown"));
      writer.println("ro.build.version.incremental:" + SystemPropertiesCompat.get("ro.build.version.incremental", "unknown"));
      writer.println("ro.build.version.release:" + SystemPropertiesCompat.get("ro.build.version.release", "unknown"));
      writer.println("ro.build.version.sdk:" + SystemPropertiesCompat.get("ro.build.version.sdk", "unknown"));
      writer.println("\n\n-------Exception message:" + ex.getLocalizedMessage());
      writer.println("\n\n-------Exception StackTrace:");
      ex.printStackTrace(writer);

      VLog.w(CrashReporter.class.getSimpleName(), "success to record crash log.");
    } catch (Throwable throwable) {
      VLog.w(CrashReporter.class.getSimpleName(), "failed to record crash log.");
    } finally {
      try {
        if (writer != null) {
          writer.flush();
          writer.close();
        }
      } catch (Exception ignored) {
      }
    }

  }
}
