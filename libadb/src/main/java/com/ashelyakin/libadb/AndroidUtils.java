package com.ashelyakin.libadb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AndroidUtils {
    private static final String GOLDFISH = "goldfish";
    private static final String RANCHU = "ranchu";
    private static final String SDK = "sdk";

    public static boolean isEmulator(@NonNull Context context) {
        if (Build.PRODUCT.contains(SDK)) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
                && (Build.HARDWARE.contains(GOLDFISH) || Build.HARDWARE.contains(RANCHU))) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            @SuppressLint("HardwareIds")
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return androidId == null;
        }
        return false;
    }


    @NonNull
    public static String getHostIpAddress(@NonNull Context context) {
        if (AndroidUtils.isEmulator(context)) {
            return "10.0.2.2";
        }
        String ipAddress;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ipAddress = InetAddress.getLoopbackAddress().getHostAddress();
        } else {
            try {
                ipAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                ipAddress = null;
            }
        }
        if (ipAddress == null || ipAddress.equals("::1")) {
            return "127.0.0.1";
        }
        return ipAddress;
    }
}
