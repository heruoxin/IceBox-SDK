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
import android.support.annotation.IntDef;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * IceBox SDK
 */
public class IceBox {

    public static final String SDK_PERMISSION = "com.catchingnow.icebox.SDK";

    // 冰箱的包名
    public static final String PACKAGE_NAME = "com.catchingnow.icebox";

    // 最低支持 SDK 的冰箱 apk 版本号
    public static final int AVAILABLE_VERSION_CODE = 703;

    private static final String SILENT_INSTALLER_SERVICE_NAME = "com.catchingnow.icebox.appSdk.InstallerService";
    private static final String ACTION_INSTALLER = "com.catchingnow.icebox.INSTALLER";
    private static final Uri PERMISSION_URI = Uri.parse("content://com.catchingnow.icebox.SDK");
    private static final Uri NO_PERMISSION_URI = Uri.parse("content://com.catchingnow.icebox.STATE");

    public enum WorkMode {
        MODE_PM_DISABLE_USER,
        MODE_PM_HIDE,
        MODE_NOT_AVAILABLE,
    }

    /**
     * 查询冰箱的工作模式
     *
     * 查询不需要权限
     *
     * @param context context
     * @return 如果是使用的 pm disable-user 模式，则返回 MODE_PM_DISABLE_USER，pm hide 则返回 MODE_PM_HIDE
     * 不可用则返回 MODE_NOT_AVAILABLE
     */
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

    public static final int FLAG_PM_DISABLE_USER = 1;
    public static final int FLAG_PM_HIDE = 2;

    @IntDef(flag = true, value = {FLAG_PM_HIDE, FLAG_PM_DISABLE_USER})
    public @interface AppState {}

    /**
     * 查询一个 App 的冻结状态
     *
     * 查询不需要权限
     *
     * @param context context
     * @param packageName 包名
     * @return 一个 flag，状态可能为 0 （未冻结），1 （被 PM_DISABLE 冻结） 2（被 PM_HIDE冻结）或 3 （被冻了又冻）
     * @throws PackageManager.NameNotFoundException 未找到该 app
     */
    @AppState
    public static int getAppEnabledSetting(Context context, String packageName) throws PackageManager.NameNotFoundException {
        return AppStateUtil.getAppEnabledSettings(context, packageName);
    }

    /**
     * 查询一个 App 的冻结状态
     *
     * 查询不需要权限
     *
     * @param applicationInfo applicationInfo
     * @return 一个 flag，状态可能为 0 （未冻结），1 （被 PM_DISABLE 冻结） 2（被 PM_HIDE冻结）或 3 （被冻了又冻）
     */
    @AppState
    public static int getAppEnabledSetting(ApplicationInfo applicationInfo) {
        return AppStateUtil.getAppEnabledSettings(applicationInfo);
    }

    /**
     * 冻结解冻 App
     *
     * PS: 冰箱并不是所有的引擎都支持多用户，所以暂时禁用掉多用户功能。
     *
     * @param context context
     * @param packageNames 包名
     * @param enable true for 解冻，false for 冻结
     */
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

    public enum SilentInstallState {
        STATE_SUPPORT,

        STATE_NOT_INSTALLED,        //未安装冰箱 IceBox;
        STATE_SYSTEM_NOT_SUPPORT,   //当前系统版本不支持静默安装;
        STATE_NOT_DEVICE_OWNER,     //冰箱 IceBox 不是设备管理员;
        STATE_UPDATE_REQUIRED,      //冰箱 IceBox 版本过低;
        STATE_PERMISSION_REQUIRED,  //当前 App 未取得权限;
    }

    /**
     *
     * 查询当前冰箱是否支持静默安装
     *
     * @param context context
     * @return 可能的枚举状态
     */
    public static SilentInstallState querySupportSilentInstall(Context context) {
        Bundle extra = new Bundle();
        extra.putParcelable("authorize", AuthorizeUtil.getAuthorizedPI(context));
        try {
            Bundle bundle = context.getContentResolver().call(NO_PERMISSION_URI, "query_silent_install_support", null, extra);
            if (bundle == null || bundle.isEmpty()) return SilentInstallState.STATE_UPDATE_REQUIRED;
            return SilentInstallState.valueOf(bundle.getString("state"));
        } catch (Exception e) {
            e.printStackTrace();
            return SilentInstallState.STATE_NOT_INSTALLED;
        }
    }

    /**
     * 静默安装 APK
     *
     * @param context context
     * @param apkUri uri
     * @return 是否安装成功
     */
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
        Intent intent = new Intent(ACTION_INSTALLER)
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

}
