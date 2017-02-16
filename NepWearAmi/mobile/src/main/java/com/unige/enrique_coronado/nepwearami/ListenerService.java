package com.unige.enrique_coronado.nepwearami;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Listener activity
 */
public class ListenerService extends WearableListenerService {

    String message;
    Intent messageIntent;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        message = new String(messageEvent.getData());
        messageIntent = new Intent();
        messageIntent.setAction(Intent.ACTION_SEND);
        messageIntent.putExtra("message", message);

        //Acceleration
        if(messageEvent.getPath().equals("/motion1")) {
            messageIntent.putExtra("type", "1");
        }

        //Gyroscope
        if(messageEvent.getPath().equals("/motion2")) {
            messageIntent.putExtra("type", "2");
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
    }
}
