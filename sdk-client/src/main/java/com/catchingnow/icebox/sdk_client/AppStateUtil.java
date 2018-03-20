package com.catchingnow.icebox.sdk_client;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;

class AppStateUtil {
    private static final int PM_FLAGS_GET_APP_INFO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
            PackageManager.MATCH_UNINSTALLED_PACKAGES
            : PackageManager.GET_UNINSTALLED_PACKAGES;


    private static final int PRIVATE_FLAG_HIDDEN = 1;
    private static final int FLAG_HIDDEN = 1<<27;

    @Nullable
    private static Field AI_FIELD;
    static {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) throw new UnsupportedOperationException();
            AI_FIELD = ApplicationInfo.class.getDeclaredField("privateFlags");
            AI_FIELD.setAccessible(true);
        } catch (Throwable ignored) {
            AI_FIELD = null;
        }
    }

    private static boolean isAppHidden(ApplicationInfo ai) {
        if (AI_FIELD != null) {
            try {
                int flags = (int) AI_FIELD.get(ai);
                return (flags|PRIVATE_FLAG_HIDDEN) == flags;
            } catch (Throwable e) {
                return (ai.flags|FLAG_HIDDEN) == ai.flags;
            }
        } else {
            return (ai.flags|FLAG_HIDDEN) == ai.flags;
        }
    }

    @IceBox.AppState
    static int getAppEnabledSettings(Context context, String packageName) throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = context.getPackageManager()
                .getApplicationInfo(packageName, PM_FLAGS_GET_APP_INFO);
        return getAppEnabledSettings(applicationInfo);

    }

    @IceBox.AppState
    static int getAppEnabledSettings(ApplicationInfo applicationInfo) {
        @IceBox.AppState int flag = 0;
        if (isAppHidden(applicationInfo)) flag += IceBox.FLAG_PM_HIDE;
        if (!applicationInfo.enabled) flag += IceBox.FLAG_PM_DISABLE_USER;
        return flag;
    }
}
