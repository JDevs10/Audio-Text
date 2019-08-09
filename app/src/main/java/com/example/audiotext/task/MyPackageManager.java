package com.example.audiotext.task;

public class MyPackageManager {
    public static boolean isPackageInstalled(android.content.pm.PackageManager pm, String packageName) {
        try {
            pm.getPackageInfo(packageName, 0);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
}
