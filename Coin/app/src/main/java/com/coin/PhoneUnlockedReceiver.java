package com.coin;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneUnlockedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            System.out.println("--> ACTION_USER_PRESENT");
            Constant.setOnLocked(false);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            System.out.println("--> ACTION_SCREEN_OFF");
            Constant.setOnLocked(true);
        }
    }
}