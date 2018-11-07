package com.catchingnow.icebox.sdk_client;

import android.os.Parcel;
import android.os.ResultReceiver;

class ResultReceiverUtil {

    static ResultReceiver receiverForSending(ResultReceiver actualReceiver) {
        Parcel parcel = Parcel.obtain();
        actualReceiver.writeToParcel(parcel,0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending = ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return receiverForSending;
    }

}
