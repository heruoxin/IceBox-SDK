package com.catchingnow.icebox.sdk_client;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

class AuthorizeUtil {

    private static PendingIntent authorizePendingIntent = null;
    static PendingIntent getAuthorizedPI(Context context) {
        if (authorizePendingIntent == null) {
            authorizePendingIntent = PendingIntent.getBroadcast(context,
                    0x333, new Intent(context, StateReceiver.class), 0);
        }
        return authorizePendingIntent;
    }

}
