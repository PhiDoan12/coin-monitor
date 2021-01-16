package com.pedromassango.jobschedulersample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.gson.reflect.TypeToken;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static com.pedromassango.jobschedulersample.Constant.COUNT_TIME_OUT;

/**
 * Created by Pedro Massango on 5/16/18.
 */
public class MyJobService extends JobService {

    // JobService thread
    private JobThread jobThread;

    private void log(String msg) {
        Log.e("MyJobService", msg);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate();");

        jobThread = new JobThread();
        jobThread.start(); // start the thread when Service is created
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand();");

        // since onStartCommand is called only if the thread is already running
        // we just ignore this method, since we already started the thread in onCreate()
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        log("onStartJob();");

        // we just ignore this method,
        // since we already started the thread in onCreate()
        return true; // return true to let the service do the job
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        log("onStopJob();");

        // change the thread state to cancel the execution
        jobThread.stopThread = true;
        return false;
    }

    /**
     * Thread to run on this JobService
     */
    class JobThread extends Thread{

        boolean stopThread = false;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            NotificationChannel mChannel;
            Uri soundUri;
            AudioAttributes audioAttributes;
            NotificationManager notificationManager = null;
            String CHANNEL_ID = "coin.price.notify.alert";
            long[] pattern = {0, 500, 200, 500, 200, 500, 200, 500};
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + getApplicationContext().getPackageName() + "/" + R.raw.lovingyou);
            mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);

            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mChannel.setSound(soundUri, audioAttributes);
            mChannel.setVibrationPattern(pattern);
            mChannel.setLightColor(Color.YELLOW);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);

            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("MonitorPrice")
                    .setContentText("*****Coin Alert****")
                    .setAutoCancel(true)
                    .setVibrate(pattern)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setChannelId(CHANNEL_ID)
                    .setSound(soundUri, audioAttributes)
                    .build();
            mChannel.setVibrationPattern(pattern);
            while (!stopThread){
                try {
                    if(callPrice() == true){
                        notificationManager.notify((int) (System.currentTimeMillis() / 10000), notification);
                    }
                    Thread.sleep(Constant.loop * 60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log("Thread interrupted");
                }
            }
        }

        private boolean callPrice(){
            HashMap<String, String> priceMap = Constant.getPriceBinance();
            BigDecimal range = new BigDecimal(priceMap.get(Constant.coinName))
                    .subtract(Constant.markPrice);
            int CurrentPercent = range.divide(Constant.markPrice, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).intValue();
            System.out.println("CurrentPercent:" + CurrentPercent);
            if(Constant.percent > 0 && CurrentPercent >= Constant.percent){
                return true;
            }else return Constant.percent < 0 && CurrentPercent <= Constant.percent;
        }
    }
}
