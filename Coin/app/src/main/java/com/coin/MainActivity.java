package com.coin;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    EditText text;
    Button button;
    Button price;
    Button loadData;
    Thread threadLoop;
    Notification notification;
    Notification notificationBTC;
    NotificationChannel mChannel;
    NotificationChannel mChannelBTC;
    Uri soundUri;
    AudioAttributes audioAttributes;
    Boolean onPause = false;
    NotificationManager notificationManager = null;
    private static final String CHANNEL_ID = "coin.price.notify";
    private static final String CHANNEL_ID_BTC = "coin.price.notify.btc";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        price = (Button) findViewById(R.id.button2);
        loadData = (Button) findViewById(R.id.button3);
        Thread schedulerGetPriceStockUs = new Thread(() -> {
            while (true){
                try {
                    try {
                        if(onPause == true && isDeviceSecured() == true) {
                            if(Constant.priceBinanceCoin.get(Constant.KEY_PERCENT_SHOW_PRICE_VALUE) == null){
                                Thread.sleep(8 * 1000);
                            }
                            String price = Constant.KEY_PERCENT_SHOW_PRICE_VALUE + ":" + Constant.priceBinanceCoin.get(Constant.KEY_PERCENT_SHOW_PRICE_VALUE);
                            System.out.println(price);
                            notifyPriceBTC(price);
                        }
                        Constant.getPriceUsStock();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(5 * 60 * 1000);
                    //Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        schedulerGetPriceStockUs.start();
        Thread schedulerSaveCount = new Thread(() -> {
            while (true){
                try {
                    Constant.resetSaveCount();
                    Thread.sleep(4 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        schedulerSaveCount.start();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.increaseSaveCount();
                if(Constant.getSaveCount() >= 2){
                    String textsss = text.getText().toString();
                    if(!textsss.contains(Constant.KEY_PERCENT)){
                        return;
                    }
                    MonitorPrice monitorPrice = new MonitorPrice();
                    monitorPrice.saveData(getApplicationContext(), textsss);
                    Constant.resetSaveCount();
                    getPrice();
                }
            }
        });
        price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause = false;
                getPrice();
            }
        });
        loadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Constant.priceBoughtCoin.put(Constant.KEY_LOOP, Constant.timeDefaultFromUser);
                    onPause = false;
                    button.setText("Save");
                    MonitorPrice monitorPrice = new MonitorPrice();
                    text.setText(Html.fromHtml(monitorPrice.loadDataToText(getApplicationContext())));
                }catch (Exception e) {

                }
            }
        });
        //init
        getPrice();
        Toast.makeText(getApplicationContext(), "Loading!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        System.out.println("onResume");
        super.onResume();
        onPause = false;
        getPrice();
    }

    @Override
    protected void onPause() {
        System.out.println("OnPause");
        super.onPause();
        onPause = true;
        Constant.priceBoughtCoin.put(Constant.KEY_LOOP, Constant.timeDefaultFromUser);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop");
        onPause = true;
        Constant.priceBoughtCoin.put(Constant.KEY_LOOP, Constant.timeDefaultFromUser);
    }

    private void getPrice() {
        if (threadLoop != null && threadLoop.isAlive()) {
            new Thread(new Runnable() {
                final StringBuilder builder = new StringBuilder();

                @Override
                public void run() {
                    MonitorPrice monitorPrice = new MonitorPrice();
                    HashMap<String, String> hd = monitorPrice.getPrice(getApplicationContext());
                    if (hd == null) {
                        return;
                    }
                    builder.append(hd.get("DA"));
                    if (hd.containsKey("NO") && onPause == true) {
                        // no thing.
                    }
                    threadLoop.interrupt();
                    Constant.priceBoughtCoin.put(Constant.KEY_LOOP, new BigDecimal("0.05"));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText("");
                            text.setText(Html.fromHtml(builder.toString()));
                            button.setText("Running");
                        }
                    });
                }
            }).start();
            return;
        }
        threadLoop = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while (true) {
                    final StringBuilder builder = new StringBuilder();
                    MonitorPrice monitorPrice = new MonitorPrice();
                    HashMap<String, String> hd = monitorPrice.getPrice(getApplicationContext());
                    if (hd == null) {
                        return;
                    }
                    builder.append(hd.get("DA"));
                    if (hd.containsKey("NO")) {
                        if (onPause == true) {
                            try {
                                notifyABC(hd.get("percent"), false);
                            } catch (InterruptedException e) {
                                //e.printStackTrace();
                            }
                        } else {

                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText("");
                            text.setText(Html.fromHtml(builder.toString()));
                            button.setText("Running");
                        }
                    });
                    try {
                        if (monitorPrice.getTime() == null) {
                            Thread.sleep(3 * 60000);
                            return;
                        }
                        Long time = monitorPrice.getTime().multiply(new BigDecimal("60000")).longValue();
                        Thread.sleep(time);
                        //Thread.sleep(30 * 1000);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            }
        });
        threadLoop.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyABC(String price, Boolean disable) throws InterruptedException {
        long[] pattern = {0, 500, 200, 500, 200, 500, 200, 500};
        if(notificationManager == null){
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        if (notification != null) {
            notification = new Notification.Builder(this)
                    .setContentTitle("MonitorPrice")
                    .setContentText("Coin increase: " + price)
                    .setAutoCancel(true)
                    .setVibrate(pattern)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setChannelId(CHANNEL_ID)
                    .setSound(soundUri, audioAttributes)
                    .build();
            if (disable == true) {
                mChannel.setVibrationPattern(new long[]{0L});
            } else {
                mChannel.setVibrationPattern(pattern);
            }
        } else {
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
            notification = new Notification.Builder(this)
                    .setContentTitle("MonitorPrice")
                    .setContentText("Coin increase: " + price)
                    .setAutoCancel(true)
                    .setVibrate(pattern)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setChannelId(CHANNEL_ID)
                    .setSound(soundUri, audioAttributes)
                    .build();

            if(notificationManager.getNotificationChannel(CHANNEL_ID) == null){
                notificationManager.createNotificationChannel(mChannel);
            }
        }
        notificationManager.notify(0, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyPriceBTC(String price) throws InterruptedException {
        if(notificationManager == null){
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        if (notificationBTC != null) {
            notificationBTC = new Notification.Builder(this)
                    .setContentTitle("Price")
                    .setContentText(price)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setChannelId(CHANNEL_ID_BTC)
                    .build();
        } else {
            mChannelBTC = new NotificationChannel(CHANNEL_ID_BTC, CHANNEL_ID_BTC, NotificationManager.IMPORTANCE_HIGH);
            mChannelBTC.enableLights(false);
            mChannelBTC.enableVibration(false);
            notificationBTC = new Notification.Builder(this)
                    .setContentTitle("Price")
                    .setContentText(price)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setChannelId(CHANNEL_ID_BTC)
                    .build();

            if(notificationManager.getNotificationChannel(CHANNEL_ID_BTC) == null){
                notificationManager.createNotificationChannel(mChannelBTC);
            }
        }
        notificationManager.notify(1, notificationBTC);
    }

    private boolean isDeviceSecured()
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            KeyguardManager manager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            return manager.isKeyguardLocked();
        }
        return false;
    }
}