package com.example.codered.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.codered.Store;

public class AlertReceiver extends BroadcastReceiver {
    public static MediaPlayer player;
    private final static String FROM_PERSON = "+918098096854", BODY_STARTING="CODE RED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Useful Info📜","Received Broadcast");

        if(intent.getAction().equals("stopPlayer")){
            stopAlert();
            return;
        }
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            String smsSender = "";
            StringBuilder smsBody = new StringBuilder();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    smsSender = smsMessage.getDisplayOriginatingAddress();
                    smsBody.append(smsMessage.getMessageBody());
                }
            }
            else {
                Bundle smsBundle = intent.getExtras();
                if (smsBundle != null) {
                    Object[] pdus = (Object[]) smsBundle.get("pdus");
                    if (pdus == null) {
                        Log.w("Warning ⚠","SmsBundle had no pdus key");
                        return;
                    }
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < messages.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        smsBody.append(messages[i].getMessageBody());
                    }
                    smsSender = messages[0].getOriginatingAddress();
                }
            }

            Log.i("Useful Info📄","SMS Sender: "+smsSender+" SMS Body: "+smsBody);

            if (smsBody.toString().startsWith(BODY_STARTING)) {
                player = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
                player.start();
                player.setLooping(true);
                Store.setIsPlayingAlert(true);
                new CountDownTimer(60000, 2000){
                    @Override
                    public void onTick(long l) {
                        AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                    }
                    @Override
                    public void onFinish() {

                    }
                }.start();
            }
        }
    }
    public void stopAlert(){
        if(player!=null) {
            if (player.isPlaying()) {
                player.stop();
                Store.setIsPlayingAlert(false);
            }
            else
                Log.w("Warning📄","Not Playing");
        }
        else
            Log.e("Error⁉","No Player");
    }

}

