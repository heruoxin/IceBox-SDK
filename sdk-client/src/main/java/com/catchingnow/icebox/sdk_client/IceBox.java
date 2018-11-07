package com.catchingnow.icebox.sdk_client;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;

/**
 * IceBox SDK
 */
public class IceBox {

    public static final String SDK_PERMISSION = "com.catchingnow.icebox.SDK";

    // 冰箱的包名
    public static final String PACKAGE_NAME = "com.catchingnow.icebox";

    // 最低支持 SDK 的冰箱 apk 版本号
    public static final int AVAILABLE_VERSION_CODE = 703;

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
        return SdkImplement.queryWorkMode(context);
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
        return SdkImplement.getAppEnabledSetting(context, packageName);
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
        return SdkImplement.getAppEnabledSetting(applicationInfo);
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
        SdkImplement.setAppEnabledSettings(context, enable, packageNames);
    }

    public enum SilentInstallSupport {
        SUPPORTED,

        NOT_INSTALLED,          //未安装冰箱 IceBox;
        UPDATE_REQUIRED,        //冰箱 IceBox 版本过低;
        SYSTEM_NOT_SUPPORTED,   //当前系统版本不支持静默安装;
        NOT_DEVICE_OWNER,       //冰箱 IceBox 不是设备管理员;
        PERMISSION_REQUIRED,    //当前 App 未取得权限;
    }

    /**
     *
     * 查询当前冰箱是否支持静默安装
     * 仅设置为设备管理员的冰箱支持静默安装和卸载
     *
     * @param context context
     * @return 可能的枚举状态
     */
    public static SilentInstallSupport querySupportSilentInstall(Context context) {
        return SdkImplement.querySupportSilentInstall(context);
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
        return SdkImplement.installPackage(context, apkUri);
    }

    /**
     *
     * 静默卸载 App
     *
     * @param context context
     * @param packageName 包名
     * @return 是否卸载成功
     */
    public static boolean uninstallPackage(Context context, String packageName) {
        return SdkImplement.uninstallPackage(context, packageName);
    }

}
