package com.catchingnow.icebox.sdk_client;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;

/**
 * IceBox SDK
 */
public class IceBox {

    public static final String PERMISSION = "com.catchingnow.icebox.SDK";

    // 冰箱的包名
    public static final String PACKAGE_NAME = "com.catchingnow.icebox";

    // 最低支持 SDK 的冰箱 apk 版本号
    public static final int AVAILABLE_VERSION_CODE = 703;

    private static final Uri SDK_URI = Uri.parse("content://com.catchingnow.icebox.SDK");
    private static final Uri STATE_URI = Uri.parse("content://com.catchingnow.icebox.STATE");

    public enum WorkMode {
        MODE_PM_DISABLE_USER,
        MODE_PM_HIDE,
        MODE_NOT_AVAILABLE;
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
            Bundle bundle = context.getContentResolver().call(STATE_URI, "query_mode", null, extra);
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
     * PS2: 这是一个同步的方法，直到解冻成功后才会 return 掉。
     *
     * @param context context
     * @param packageNames 包名
     * @param enable true for 解冻，false for 冻结
     */
    @WorkerThread
    @RequiresPermission(PERMISSION)
    public static void setAppEnabledSettings(Context context, boolean enable, String... packageNames) {
        int userHandle;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            userHandle = Process.myUserHandle().hashCode();
        } else {
            userHandle = 0;
        }
        setAppEnabledSettings(context, enable, userHandle, packageNames);
    }

    @WorkerThread
    @RequiresPermission(PERMISSION)
    private static void setAppEnabledSettings(Context context, boolean enable, int userHandle, String... packageNames) {
        Bundle extra = new Bundle();
        extra.putParcelable("authorize", AuthorizeUtil.getAuthorizedPI(context));
        extra.putStringArray("package_names", packageNames);
        extra.putBoolean("enable", enable);
        context.getContentResolver().call(SDK_URI, "set_enable", null, extra);
    }

}
