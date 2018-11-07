package com.catchingnow.icebox.sdk_client;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.ResultReceiver;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * IceBox SDK
 */
class SdkImplement extends IceBox {

    private static final String SILENT_INSTALLER_SERVICE_NAME = "com.catchingnow.icebox.appSdk.InstallerService";
    private static final Uri PERMISSION_URI = Uri.parse("content://com.catchingnow.icebox.SDK");
    private static final Uri NO_PERMISSION_URI = Uri.parse("content://com.catchingnow.icebox.STATE");

    public static WorkMode queryWorkMode(Context context) {
        Bundle extra = new Bundle();
        extra.putParcelable("authorize", AuthorizeUtil.getAuthorizedPI(context));
        try {
            Bundle bundle = context.getContentResolver().call(NO_PERMISSION_URI, "query_mode", null, extra);
            return WorkMode.valueOf(bundle.getString("work_mode", WorkMode.MODE_NOT_AVAILABLE.name()));
        } catch (Exception e) {
            e.printStackTrace();
            return WorkMode.MODE_NOT_AVAILABLE;
        }
    }

    @AppState
    public static int getAppEnabledSetting(Context context, String packageName) throws PackageManager.NameNotFoundException {
        return AppStateUtil.getAppEnabledSettings(context, packageName);
    }

    @AppState
    public static int getAppEnabledSetting(ApplicationInfo applicationInfo) {
        return AppStateUtil.getAppEnabledSettings(applicationInfo);
    }

    @WorkerThread
    @RequiresPermission(SDK_PERMISSION)
    public static void setAppEnabledSettings(Context context, boolean enable, String... packageNames) {
        int userHandle;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            userHandle = Process.myUserHandle().hashCode();
        } else {
            userHandle = 0;
        }
        setAppEnabledSettings(context, enable, userHandle, packageNames);
    }

    private static void setAppEnabledSettings(Context context, boolean enable, int userHandle, String... packageNames) {
        Bundle extra = new Bundle();
        extra.putParcelable("authorize", AuthorizeUtil.getAuthorizedPI(context));
        extra.putStringArray("package_names", packageNames);
        extra.putInt("user_handle", userHandle);
        extra.putBoolean("enable", enable);
        context.getContentResolver().call(PERMISSION_URI, "set_enable", null, extra);
    }

    public static SilentInstallSupport querySupportSilentInstall(Context context) {
        if (Build.VERSION.SDK_INT < 23) return SilentInstallSupport.SYSTEM_NOT_SUPPORTED;

        Bundle extra = new Bundle();
        extra.putParcelable("authorize", AuthorizeUtil.getAuthorizedPI(context));
        try {
            Bundle bundle = context.getContentResolver().call(NO_PERMISSION_URI, "query_silent_install_support", null, extra);
            if (bundle == null || bundle.isEmpty()) return SilentInstallSupport.UPDATE_REQUIRED;
            return SilentInstallSupport.valueOf(bundle.getString("state"));
        } catch (Exception e) {
            e.printStackTrace();
            return SilentInstallSupport.NOT_INSTALLED;
        }
    }

    @WorkerThread
    @RequiresPermission(SDK_PERMISSION)
    public static boolean installPackage(Context context, Uri apkUri) {
        final AtomicBoolean o = new AtomicBoolean();
        ResultReceiver resultReceiver = ResultReceiverUtil.receiverForSending(new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                Exception error = (Exception) resultData.getSerializable("error");
                if (error != null) error.printStackTrace();
                o.set(resultCode == 1);
                synchronized (o) {o.notify();}
            }
        });
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE)
                .setPackage(PACKAGE_NAME)
                .setClassName(PACKAGE_NAME, SILENT_INSTALLER_SERVICE_NAME)
                .putExtra("authorize", AuthorizeUtil.getAuthorizedPI(context))
                .putExtra("callback", resultReceiver)
                .setData(apkUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startService(intent);
        synchronized (o) {
            try {
                o.wait();
                return o.get();
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

    @WorkerThread
    @RequiresPermission(SDK_PERMISSION)
    public static boolean uninstallPackage(Context context, String packageName) {
        final AtomicBoolean o = new AtomicBoolean();
        ResultReceiver resultReceiver = ResultReceiverUtil.receiverForSending(new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                Exception error = (Exception) resultData.getSerializable("error");
                if (error != null) error.printStackTrace();
                o.set(resultCode == 1);
                synchronized (o) {o.notify();}
            }
        });
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                .setPackage(PACKAGE_NAME)
                .setClassName(PACKAGE_NAME, SILENT_INSTALLER_SERVICE_NAME)
                .putExtra("authorize", AuthorizeUtil.getAuthorizedPI(context))
                .putExtra("callback", resultReceiver)
                .putExtra("uninstall_package_name", packageName)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startService(intent);
        synchronized (o) {
            try {
                o.wait();
                return o.get();
            } catch (InterruptedException e) {
                return false;
            }
        }
    }
}
