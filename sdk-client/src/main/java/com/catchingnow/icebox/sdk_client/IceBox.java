package com.catchingnow.icebox.sdk_client;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * IceBox SDK
 */
public class IceBox {

    public static final String PERMISSION = "com.catchingnow.icebox.SDK";

    // 冰箱的包名
    public static final String PACKAGE_NAME = "com.catchingnow.icebox";

    // 冰箱最低支持 SDK 的版本号
    public static final int AVAILABLE_VERSION_CODE = 699;

    private static final Uri SDK_URI = Uri.parse("content://com.catchingnow.icebox.SDK");

    public enum WorkMode {
        MODE_PM_DISABLE_USER,
        MODE_PM_HIDE,
        MODE_NOT_AVAILABLE;
    }

    /**
     * 查询冰箱的工作模式
     *
     * @param context context
     * @return 如果是使用的 pm disable-user 模式，则返回 MODE_PM_DISABLE_USER，pm hide 则返回 MODE_PM_HIDE
     * 不可用则返回 MODE_NOT_AVAILABLE
     */
    public static WorkMode queryWorkMode(Context context) {
        Bundle extra = new Bundle();
        extra.putParcelable("authorize", getAuthorizedPI(context));
        try {
            Bundle bundle = context.getContentResolver().call(SDK_URI, "query_mode", null, extra);
            return WorkMode.valueOf(bundle.getString("work_mode", WorkMode.MODE_NOT_AVAILABLE.name()));
        } catch (NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
            return WorkMode.MODE_NOT_AVAILABLE;
        }
    }

    /**
     *
     * @param context context
     * @param packageNames 包名
     * @param userHandle 多用户，目前冰箱的多种工作模式并非都支持多用户，所以可以直接传 Process.myUserHandle().hashCode()
     * @param enable true for 解冻，false for 冻结
     */
    public static void setAppEnabledSettings(Context context, boolean enable,int userHandle, String... packageNames) {
        Bundle extra = new Bundle();
        extra.putParcelable("authorize", getAuthorizedPI(context));
        extra.putStringArray("package_names", packageNames);
        extra.putBoolean("enable", enable);
        context.getContentResolver().call(SDK_URI, "set_enable", null, extra);
    }

    private static PendingIntent authorizePendingIntent = null;
    private static PendingIntent getAuthorizedPI(Context context) {
        if (authorizePendingIntent == null) {
            authorizePendingIntent = PendingIntent.getBroadcast(context,
                    0x333, new Intent(context, StateReceiver.class), 0);
        }
        return authorizePendingIntent;
    }


}
