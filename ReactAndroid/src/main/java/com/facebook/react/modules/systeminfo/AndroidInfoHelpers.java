// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.react.modules.systeminfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import android.os.Build;

import com.facebook.common.logging.FLog;

public class AndroidInfoHelpers {

  public static final String EMULATOR_LOCALHOST = "10.0.2.2";
  public static final String GENYMOTION_LOCALHOST = "10.0.3.2";
  public static final String DEVICE_LOCALHOST = "localhost";

  public static final String METRO_HOST_PROP_NAME = "metro.host";

  private static final int DEBUG_SERVER_HOST_PORT = 8081;
  private static final int INSPECTOR_PROXY_PORT = 8082;

  private static final String TAG = AndroidInfoHelpers.class.getSimpleName();

  private static boolean isRunningOnGenymotion() {
    return Build.FINGERPRINT.contains("vbox");
  }

  private static boolean isRunningOnStockEmulator() {
    return Build.FINGERPRINT.contains("generic");
  }

  public static String getServerHost() {
    return getServerIpAddress(DEBUG_SERVER_HOST_PORT);
  }

  public static String getInspectorProxyHost() {
    return getServerIpAddress(INSPECTOR_PROXY_PORT);
  }

  // WARNING(festevezga): This RN helper method has been copied to another FB-only target. Any changes should be applied to both.
  public static String getFriendlyDeviceName() {
    if (isRunningOnGenymotion()) {
      // Genymotion already has a friendly name by default
      return Build.MODEL;
    } else {
      return Build.MODEL + " - " + Build.VERSION.RELEASE + " - API " + Build.VERSION.SDK_INT;
    }
  }

  private static String getServerIpAddress(int port) {
    // Since genymotion runs in vbox it use different hostname to refer to adb host.
    // We detect whether app runs on genymotion and replace js bundle server hostname accordingly

    String ipAddress;
    String metroHostProp = getMetroHostPropValue();
    if (!metroHostProp.equals("")) {
      ipAddress = metroHostProp;
    } else if (isRunningOnGenymotion()) {
      ipAddress = GENYMOTION_LOCALHOST;
    } else if (isRunningOnStockEmulator()) {
      ipAddress = EMULATOR_LOCALHOST;
    } else {
      ipAddress = DEVICE_LOCALHOST;
    }

    return String.format(Locale.US, "%s:%d", ipAddress, port);
  }

  private static String metroHostPropValue = null;
  private static synchronized String getMetroHostPropValue() {
    if (metroHostPropValue != null) {
      return metroHostPropValue;
    }
    Process process = null;
    BufferedReader reader = null;
    try {
      process =
          Runtime.getRuntime().exec(new String[] {"/system/bin/getprop", METRO_HOST_PROP_NAME});
      reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

      String lastLine = "";
      String line;
      while ((line = reader.readLine()) != null) {
        lastLine = line;
      }
      metroHostPropValue = lastLine;
    } catch (Exception e) {
      FLog.w(TAG, "Failed to query for metro.host prop:", e);
      metroHostPropValue = "";
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (Exception exc) {
      }
      if (process != null) {
        process.destroy();
      }
    }
    return metroHostPropValue;
  }
}
